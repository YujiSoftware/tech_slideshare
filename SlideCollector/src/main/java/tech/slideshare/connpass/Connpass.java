package tech.slideshare.connpass;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.slideshare.collector.ConnpassCollector;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
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
     * @param id               イベントID
     * @param title            タイトル
     * @param catchText        キャッチ
     * @param description      概要(HTML形式)
     * @param url              connpass.com上のURL
     * @param hashTag          Twitterのハッシュタグ
     * @param startedAt        イベント開催日時 (ISO-8601形式)
     * @param endedAt          イベント終了日時 (ISO-8601形式)
     * @param limit            定員
     * @param eventType        イベント参加タイプ
     * @param openStatus       イベントの開催状態(preopen: 開催前, open: 開催中, close: 終了, cancelled: 中止)
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
    @JsonIgnoreProperties({"event_id", "event_url"})
    public record Event(
            @JsonProperty("id") int id,
            @JsonProperty("title") String title,
            @JsonProperty("catch") String catchText,
            @JsonProperty("description") String description,
            @JsonProperty("url") String url,
            @JsonProperty("hash_tag") String hashTag,
            @JsonProperty("started_at") ZonedDateTime startedAt,
            @JsonProperty("ended_at") ZonedDateTime endedAt,
            @JsonProperty("limit") long limit,
            @JsonProperty("event_type") String eventType,
            @JsonProperty("open_status") String openStatus,
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
         * @param id        グループID
         * @param subdomain サブドメイン
         * @param title     グループタイトル
         * @param url       グループのconnpass.com 上のURL
         */
        public record Series(
                @JsonProperty("id") int id,
                @JsonProperty("subdomain") String subdomain,
                @JsonProperty("title") String title,
                @JsonProperty("url") String url
        ) {
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(ConnpassCollector.class);

    static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public static List<Event> getEvents(Instant instant) throws IOException {
        int count = 100;
        int loop = 10;
        List<Event> events = new ArrayList<>(count * loop);

        OUTER:
        for (int i = 0; i < loop; i++) {
            URL url = new URL("https://connpass.com/api/v1/event/?count=" + count + "&start=" + (i * count));
            logger.debug("Request: {}", url);

            Connpass connpass = mapper.readValue(url, Connpass.class);

            for (Event event : connpass.events()) {
                if (event.updatedAt().toInstant().isBefore(instant)) {
                    break OUTER;
                }
                events.add(event);
            }
        }

        logger.debug("Collected count: {}", events.size());

        return events;
    }

}
