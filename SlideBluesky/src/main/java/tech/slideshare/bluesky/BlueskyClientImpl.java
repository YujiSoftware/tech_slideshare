package tech.slideshare.bluesky;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BlueskyClientImpl implements BlueskyClient {
    private static final String BLUESKY_API_URL = "https://bsky.social/xrpc";

    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private final Gson gson = new Gson();

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
    public BlueskyClientImpl(String username, String appPassword) throws IOException {
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

    @Override
    public String getDid() {
        return did;
    }

    /**
     * 認証付きPOSTリクエストを実行し、JsonObjectレスポンスを取得
     *
     * @param endpoint エンドポイント（/com.atproto.xxx形式）
     * @param entity   リクエストエンティティ
     * @return レスポンスボディの JsonObject
     */
    @Override
    public JsonObject post(String endpoint, HttpEntity entity) throws IOException {
        HttpPost post = new HttpPost(BLUESKY_API_URL + endpoint);
        post.setHeader("Authorization", "Bearer " + this.accessToken);
        post.setEntity(entity);

        return httpClient.execute(post, response -> {
            String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            if (response.getCode() != 200) {
                throw new IOException("POST failed. Endpoint: " + endpoint + ", Status code: " + response.getCode() + ", body: " + body);
            }

            return gson.fromJson(body, JsonObject.class);
        });
    }
}
