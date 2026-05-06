package tech.slideshare.bluesky;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
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

import java.io.IOException;
import java.net.URI;
import java.time.Instant;

public class Bluesky {
    private static final Logger logger = LoggerFactory.getLogger(Bluesky.class);

    public static final int MAX_CHARACTERS = 300;

    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private final Gson gson = new Gson();

    private final BlueskyClient client;

    public Bluesky(BlueskyClient client) {
        this.client = client;
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
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("repo", client.getDid());
        requestBody.addProperty("collection", "app.bsky.feed.post");
        requestBody.add("record", makeRecord(text, url));

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

    private JsonObject makeRecord(String text, String url) throws IOException {
        // Links, mentions, and rich text
        // https://docs.bsky.app/docs/advanced-guides/post-richtext
        JsonObject record = new JsonObject();
        record.addProperty("text", text + "\n" + url);
        record.addProperty("createdAt", Instant.now().toString());

        // Facetsを作成（URLリンク）
        int textLength = UTF8.length(text);
        int crlfLength = 1;
        int urlLength = UTF8.length(url);

        JsonObject index = new JsonObject();
        index.addProperty("byteStart", textLength + crlfLength);
        index.addProperty("byteEnd", textLength + crlfLength + urlLength);

        JsonObject feature = new JsonObject();
        feature.addProperty("$type", "app.bsky.richtext.facet#link");
        feature.addProperty("uri", url);

        JsonArray features = new JsonArray();
        features.add(feature);

        JsonObject facet = new JsonObject();
        facet.add("index", index);
        facet.add("features", features);

        JsonArray facets = new JsonArray();
        facets.add(facet);

        record.add("facets", facets);

        OGP.OGPInfo ogpInfo = OGP.extract(url);
        JsonObject embed = createExternalEmbed(ogpInfo);
        record.add("embed", embed);

        return record;
    }

    /**
     * OGP情報からBlueskyのexternal embedを作成
     *
     * @param ogpInfo OGP情報
     * @return external embedオブジェクト、作成できない場合はnull
     */
    private JsonObject createExternalEmbed(OGP.OGPInfo ogpInfo) throws IOException {
        JsonObject external = new JsonObject();
        external.addProperty("$type", "app.bsky.embed.external");

        JsonObject externalObj = new JsonObject();
        externalObj.addProperty("uri", ogpInfo.url());
        externalObj.addProperty("title", ogpInfo.title());
        externalObj.addProperty("description", ogpInfo.description());

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

        external.add("external", externalObj);

        return external;
    }
}
