package tech.slideshare.connpass;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * connpass API レスポンス
 *
 * @param resultsReturned  含まれる検索結果の件数
 * @param resultsAvailable 検索結果の総件数
 * @param resultsStart     検索の開始位置
 * @param events           検索結果のイベントリスト
 */
public record Connpass(
        @JsonProperty("results_returned") int resultsReturned,
        @JsonProperty("results_available") int resultsAvailable,
        @JsonProperty("results_start") int resultsStart,
        @JsonProperty("events") List<Event> events
) {
    /**
     * イベント情報
     *
     * @param eventId          イベントID
     * @param title            タイトル
     * @param catchText        キャッチ
     * @param description      概要(HTML形式)
     * @param eventUrl         connpass.com 上のURL
     * @param hashTag          Twitterのハッシュタグ
     * @param startedAt        イベント開催日時 (ISO-8601形式)
     * @param endedAt          イベント終了日時 (ISO-8601形式)
     * @param limit            定員
     * @param eventType        イベント参加タイプ
     * @param series           グループ
     * @param address          開催場所
     * @param place            開催会場
     * @param lat              開催会場の緯度
     * @param lon              開催会場の経度
     * @param ownerId          管理者のID
     * @param ownerNickname    管理者のニックネーム
     * @param ownerDisplayName 管理者の表示名
     * @param accepted         参加者数
     * @param waiting          補欠者数
     * @param updatedAt        更新日時 (ISO-8601形式)
     */
    public record Event(
            @JsonProperty("event_id") int eventId,
            @JsonProperty("title") String title,
            @JsonProperty("catch") String catchText,
            @JsonProperty("description") String description,
            @JsonProperty("event_url") String eventUrl,
            @JsonProperty("hash_tag") String hashTag,
            @JsonProperty("started_at") ZonedDateTime startedAt,
            @JsonProperty("ended_at") ZonedDateTime endedAt,
            @JsonProperty("limit") int limit,
            @JsonProperty("event_type") String eventType,
            @JsonProperty("series") Series series,
            @JsonProperty("address") String address,
            @JsonProperty("place") String place,
            @JsonProperty("lat") double lat,
            @JsonProperty("lon") double lon,
            @JsonProperty("owner_id") int ownerId,
            @JsonProperty("owner_nickname") String ownerNickname,
            @JsonProperty("owner_display_name") String ownerDisplayName,
            @JsonProperty("accepted") int accepted,
            @JsonProperty("waiting") int waiting,
            @JsonProperty("updated_at") ZonedDateTime updatedAt
    ) {
        /**
         * グループ
         *
         * @param id    グループID
         * @param title グループタイトル
         * @param url   グループのconnpass.com 上のURL
         */
        public record Series(
                @JsonProperty("id") int id,
                @JsonProperty("title") String title,
                @JsonProperty("url") String url
        ) {
        }
    }

    public static List<Event> getEvents() throws IOException {
        int count = 100;
        int loop = 10;
        List<Event> events = new ArrayList<>(count * loop);

        for (int i = 0; i < loop; i++) {
            URL url = new URL("https://connpass.com/api/v1/event/?count=" + count + "&start=" + (i + 1) * count);

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            Connpass connpass = mapper.readValue(url, Connpass.class);

            events.addAll(connpass.events);
        }

        return events;
    }
}
