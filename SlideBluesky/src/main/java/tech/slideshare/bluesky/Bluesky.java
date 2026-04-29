package tech.slideshare.bluesky;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class Bluesky implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(Bluesky.class);

    public static final int MAX_CHARACTERS = 300;

    private static final String BLUESKY_API_URL = "https://bsky.social/xrpc";
    private static final Gson gson = new Gson();
    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    private String accessToken;
    private String refreshToken;
    private String did;

    /**
     * Blueskyに認証してアクセストークンを取得
     * アプリパスワードを使用してセキュアに認証します。
     *
     * @param username    Blueskyのユーザー名またはメールアドレス
     * @param appPassword Blueskyのアプリパスワード（Settings > App passwords で生成）
     */
    public Bluesky(String username, String appPassword) throws IOException {
        HttpPost post = new HttpPost(BLUESKY_API_URL + "/com.atproto.server.createSession");

        // リクエストボディを作成
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("identifier", username);
        requestBody.addProperty("password", appPassword);

        post.setEntity(new StringEntity(
                gson.toJson(requestBody),
                ContentType.APPLICATION_JSON
        ));

        // ResponseHandlerを使用してレスポンスを処理
        httpClient.execute(post, response -> {
            String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            if (response.getCode() != 200) {
                throw new IOException("Status code: " + response.getCode() + ", body: " + body);
            }

            JsonObject responseBody = gson.fromJson(body, JsonObject.class);
            this.accessToken = responseBody.get("accessJwt").getAsString();
            this.refreshToken = responseBody.get("refreshJwt").getAsString();
            this.did = responseBody.get("did").getAsString();

            return true;
        });
    }

    @Override
    public void close() throws IOException {
        httpClient.close();
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
    public URI createPost(String text, String url) throws IOException {
        HttpPost post = new HttpPost(BLUESKY_API_URL + "/com.atproto.repo.createRecord");

        // 認証ヘッダーを追加
        post.setHeader("Authorization", "Bearer " + this.accessToken);

        // リクエストボディを作成
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("repo", this.did);
        requestBody.addProperty("collection", "app.bsky.feed.post");
        requestBody.add("record", makeRecord(text, url));

        post.setEntity(new StringEntity(
                gson.toJson(requestBody),
                ContentType.APPLICATION_JSON
        ));

        // ResponseHandlerを使用してレスポンスを処理
        return httpClient.execute(post, response -> {
            String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            if (response.getCode() != 200) {
                throw new IOException("Status code: " + response.getCode() + ", body: " + body);
            }

            JsonObject responseBody = gson.fromJson(body, JsonObject.class);
            return URI.create(responseBody.get("uri").getAsString());
        });
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

        // 説明文がある場合のみ追加
        if (ogpInfo.description() != null && !ogpInfo.description().isEmpty()) {
            externalObj.addProperty("description", ogpInfo.description());
        }

        if (ogpInfo.imageUrl() != null && !ogpInfo.imageUrl().isEmpty()) {
            HttpGet getImage = new HttpGet(ogpInfo.imageUrl());
            httpClient.execute(getImage, response -> {
                if (response.getCode() != 200) {
                    logger.warn("Failed to fetch OGP image: {} (status code: {})", ogpInfo.imageUrl(), response.getCode());
                    return null;
                }

                String contentType = response.getHeader("content-type").getValue();

                byte[] image = EntityUtils.toByteArray(response.getEntity());
                HttpPost post = new HttpPost(BLUESKY_API_URL + "/com.atproto.repo.uploadBlob");
                post.setHeader("Authorization", "Bearer " + this.accessToken);
                post.setEntity(new ByteArrayEntity(image, ContentType.parse(contentType)));
                JsonObject thumb = httpClient.execute(post, response1 -> {
                    if (response1.getCode() != 200) {
                        throw new IOException("Failed to upload image to Bluesky: " + response1.getCode());
                    }

                    String body = EntityUtils.toString(response1.getEntity(), StandardCharsets.UTF_8);
                    return gson.fromJson(body, JsonObject.class).getAsJsonObject("blob");
                });
                externalObj.add("thumb", thumb);

                return null;
            });
        }

        external.add("external", externalObj);

        System.out.println(external);

        return external;
    }
}
