package tech.slideshare.bluesky;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.hc.core5.http.HttpEntity;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BlueskyTest {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final BlueskyClient client = new BlueskyClient() {
        @Override
        public void close() {
        }

        @Override
        public String getDid() {
            return "plc:u5cwb2mwiv2bfq53cjufe6yn";
        }

        @Override
        public JsonObject post(String endpoint, HttpEntity entity) {
            return switch (endpoint) {
                case "/com.atproto.repo.createRecord" -> gson.fromJson("""
                        {
                            "uri": "at://did:plc:u5cwb2mwiv2bfq53cjufe6yn/app.bsky.feed.post/3k4duaz5vfs2b",
                            "cid": "bafyreibjifzpqj6o6wcq3hejh7y4z4z2vmiklkvykc57tw3pcbx3kxifpm"
                        }
                        """, JsonObject.class);
                case "/com.atproto.repo.uploadBlob" -> gson.fromJson("""
                        {
                            "$type":"blob",
                            "ref":{
                                "$link":"bafkreiagdvy3zgn4fss6ysl2y4pz5grtm5k35bjms6vtf3nyyw5x22pitm"
                            },
                            "mimeType":"image/png",
                            "size":127998
                        }
                        """, JsonObject.class);
                default -> throw new IllegalArgumentException("Unknown endpoint");
            };
        }
    };

    @Test
    void makeRecord() throws IOException {
        Clock clock = Clock.fixed(Instant.parse("2026-05-06T10:00:22.698797638Z"), TimeZone.getDefault().toZoneId());
        Bluesky bluesky = new Bluesky(client, clock);
        JsonObject obj = bluesky.makeRecord("Test", "https://speakerdeck.com/yujisoftware/bainaribiyuawoshi-tutekurasuhuairuwodu-ndemiyou-number-jjug-ccc");

        assertEquals("""
                {
                  "text": "Test\\nhttps://speakerdeck.com/yujisoftware/bainaribiyuawoshi-tutekurasuhuairuwodu-ndemiyou-number-jjug-ccc",
                  "createdAt": "2026-05-06T10:00:22.698797638Z",
                  "facets": [
                    {
                      "index": {
                        "byteStart": 5,
                        "byteEnd": 105
                      },
                      "features": [
                        {
                          "$type": "app.bsky.richtext.facet#link",
                          "uri": "https://speakerdeck.com/yujisoftware/bainaribiyuawoshi-tutekurasuhuairuwodu-ndemiyou-number-jjug-ccc"
                        }
                      ]
                    }
                  ],
                  "embed": {
                    "$type": "app.bsky.embed.external",
                    "external": {
                      "uri": "https://speakerdeck.com/yujisoftware/bainaribiyuawoshi-tutekurasuhuairuwodu-ndemiyou-number-jjug-ccc",
                      "title": "バイナリビューアを使ってクラスファイルを読んでみよう！ #jjug_ccc",
                      "description": "11月11日(土)に開催された、JJUG CCC 2023 Fall のセッション資料です。\\r\\n関連資料： https://github.com/YujiSoftware/binary"
                    }
                  }
                }""", gson.toJson(obj));
        System.out.println();
    }
}