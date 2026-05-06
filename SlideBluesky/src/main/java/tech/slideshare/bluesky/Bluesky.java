package tech.slideshare.bluesky;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.slideshare.bluesky.utils.JsonArrayBuilder;
import tech.slideshare.bluesky.utils.JsonObjectBuilder;

import java.io.IOException;
import java.net.URI;
import java.time.Clock;

public class Bluesky {
    private static final Logger logger = LoggerFactory.getLogger(Bluesky.class);

    public static final int MAX_CHARACTERS = 300;

    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private final Gson gson = new Gson();

    private final BlueskyClient client;
    private final Clock clock;

    public Bluesky(BlueskyClient client) {
        this.client = client;
        this.clock = Clock.systemUTC();
    }

    Bluesky(BlueskyClient client, Clock clock) {
        this.client = client;
        this.clock = clock;
    }

    /**
     * Blueskyにポストを作成
     * 認証済みユーザーのポストを作成し、ポストのURIを出力します。
     * テキスト内のURLを自動的にリンクに変換し、OGP情報を取得してembedを追加します。
     *
     * @param text ポストするテキスト（最大300文字）
     * @param url  埋め込みたいURL（null可）
     * @return ポストの URL
     */
    public URI createRecord(String text, String url) throws IOException {
        // リクエストボディを作成
        JsonObject requestBody = new JsonObjectBuilder()
                .add("repo", client.getDid())
                .add("collection", "app.bsky.feed.post")
                .add("record", makeRecord(text, url))
                .build();

        String json = gson.toJson(requestBody);
        logger.debug("Sending: {}", json);

        JsonObject responseBody = client.post(
                "/com.atproto.repo.createRecord",
                new StringEntity(json, ContentType.APPLICATION_JSON)
        );
        return URI.create(responseBody.get("uri").getAsString());
    }

    private JsonObject uploadBlob(byte[] image, String contentType) throws IOException {
        JsonObject responseBody = client.post(
                "/com.atproto.repo.uploadBlob",
                new ByteArrayEntity(image, ContentType.parse(contentType))
        );
        return responseBody.getAsJsonObject("blob");
    }

    JsonObject makeRecord(String text, String url) throws IOException {
        OGP.OGPInfo ogpInfo = OGP.extract(url);
        JsonObject embed = createExternalEmbed(ogpInfo);

        // Facetsを作成（URLリンク）
        int textLength = UTF8.length(text);
        int crlfLength = 1;
        int urlLength = UTF8.length(url);

        // Links, mentions, and rich text
        // https://docs.bsky.app/docs/advanced-guides/post-richtext
        return new JsonObjectBuilder()
                .add("text", text + "\n" + url)
                .add("createdAt", clock.instant().toString())
                .add("facets", new JsonArrayBuilder()
                        .add(new JsonObjectBuilder()
                                .add("index", new JsonObjectBuilder()
                                        .add("byteStart", textLength + crlfLength)
                                        .add("byteEnd", textLength + crlfLength + urlLength)
                                        .build()
                                )
                                .add("features", new JsonArrayBuilder()
                                        .add(new JsonObjectBuilder()
                                                .add("$type", "app.bsky.richtext.facet#link")
                                                .add("uri", url)
                                                .build()
                                        )
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
                .add("embed", embed)
                .build();
    }

    /**
     * OGP情報からBlueskyのexternal embedを作成
     *
     * @param ogpInfo OGP情報
     * @return external embedオブジェクト、作成できない場合はnull
     */
    private JsonObject createExternalEmbed(OGP.OGPInfo ogpInfo) throws IOException {
        JsonObject externalObj = new JsonObjectBuilder()
                .add("uri", ogpInfo.url())
                .add("title", ogpInfo.title())
                .add("description", ogpInfo.description())
                .build();

        if (ogpInfo.imageUrl() != null && !ogpInfo.imageUrl().isEmpty()) {
            HttpGet getImage = new HttpGet(ogpInfo.imageUrl());
            httpClient.execute(getImage, response -> {
                if (response.getCode() != 200) {
                    logger.warn("Failed to fetch OGP image: {} (status code: {})", ogpInfo.imageUrl(), response.getCode());
                    return null;
                }

                String contentType = response.getHeader("content-type").getValue();

                byte[] image = EntityUtils.toByteArray(response.getEntity());
                JsonObject thumb = uploadBlob(image, contentType);
                externalObj.add("thumb", thumb);

                return null;
            });
        }
        return new JsonObjectBuilder()
                .add("$type", "app.bsky.embed.external")
                .add("external", externalObj)
                .build();
    }
}
