package tech.slideshare.connpass;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class Connpass {
    private static final Duration INTERVAL = Duration.ofSeconds(3);

    private static final ObjectMapper mapper = new ObjectMapper();

    private static final HttpClient client = HttpClient.newHttpClient();

    static <T> T get(URI uri, Class<T> clazz) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("X-API-Key", System.getenv("CONNPASS_API_KEY"))
                .build();

        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        return mapper.readValue(response.body(), clazz);
    }

    /**
     * connpass API レスポンス
     *
     * @param resultsReturned  含まれる検索結果の件数
     * @param resultsAvailable 検索結果の総件数
     * @param resultsStart     検索の開始位置
     * @param events           検索結果のイベントリスト
     */
    public record Events(
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
         * @param imageUrl         イベント画像URL
         * @param startedAt        イベント開催日時 (ISO-8601形式)
         * @param endedAt          イベント終了日時 (ISO-8601形式)
         * @param limit            定員
         * @param eventType        イベント参加タイプ
         * @param openStatus       イベントの開催状態(preopen: 開催前, open: 開催中, close: 終了, cancelled: 中止)
         * @param group            グループ
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
                @JsonProperty("id") int id,
                @JsonProperty("title") String title,
                @JsonProperty("catch") String catchText,
                @JsonProperty("description") String description,
                @JsonProperty("url") String url,
                @JsonProperty("image_url") String imageUrl,
                @JsonProperty("hash_tag") String hashTag,
                @JsonProperty("started_at") ZonedDateTime startedAt,
                @JsonProperty("ended_at") ZonedDateTime endedAt,
                @JsonProperty("limit") Long limit,
                @JsonProperty("event_type") String eventType,
                @JsonProperty("open_status") String openStatus,
                @JsonProperty("group") Group group,
                @JsonProperty("address") String address,
                @JsonProperty("place") String place,
                @JsonProperty("lat") Double lat,
                @JsonProperty("lon") Double lon,
                @JsonProperty("owner_id") Integer ownerId,
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
            public record Group(
                    @JsonProperty("id") int id,
                    @JsonProperty("subdomain") String subdomain,
                    @JsonProperty("title") String title,
                    @JsonProperty("url") String url
            ) {
            }
        }

        private static final Logger logger = LoggerFactory.getLogger(Events.class);

        public static List<Event> get(Instant instant) throws IOException, InterruptedException {
            int count = 100;
            int loop = 10;
            List<Event> events = new ArrayList<>(count * loop);

            OUTER:
            for (int i = 0; i < loop; i++) {
                URI uri = URI.create("https://connpass.com/api/v2/events/?count=" + count + "&start=" + (i * count));
                logger.debug("Request: {}", uri);

                Events response = Connpass.get(uri, Events.class);

                for (Event event : response.events()) {
                    if (event.updatedAt().toInstant().isBefore(instant)) {
                        break OUTER;
                    }
                    events.add(event);
                }

                // Connpass API は、1秒間に1リクエストまでの制限 (スロットリング) がある。
                // そのため、余裕を見込んで間隔を開けてリクエストする。
                Thread.sleep(INTERVAL);
            }

            logger.debug("Collected count: {}", events.size());

            return events;
        }
    }

    /**
     * connpass API レスポンス
     *
     * @param resultsReturned  含まれる検索結果の件数
     * @param resultsAvailable 検索結果の総件数
     * @param resultsStart     検索の開始位置
     * @param presentations    資料一覧
     */
    public record Presentations(
            @JsonProperty("results_returned") int resultsReturned,
            @JsonProperty("results_available") int resultsAvailable,
            @JsonProperty("results_start") int resultsStart,
            @JsonProperty("presentations") List<Presentation> presentations
    ) {
        /**
         * 資料情報
         *
         * @param user             投稿者
         * @param url              資料URL
         * @param name             資料タイトル
         * @param presenter        資料を発表したユーザー
         * @param presentationType 資料タイプ
         * @param createdAt        登録日時 (ISO-8601形式)
         */
        public record Presentation(
                @JsonProperty("user") User user,
                @JsonProperty("url") String url,
                @JsonProperty("name") String name,
                @JsonProperty("presenter") User presenter,
                @JsonProperty("presentation_type") PresentationType presentationType,
                @JsonProperty("created_at") ZonedDateTime createdAt
        ) {
        }

        /**
         * ユーザー情報
         *
         * @param id       ユーザーID
         * @param nickname ユーザーニックネーム
         */
        public record User(
                @JsonProperty("id") int id,
                @JsonProperty("nickname") String nickname
        ) {
        }

        public enum PresentationType {
            @JsonProperty("slide") SLIDE,
            @JsonProperty("movie") MOVIE,
            @JsonProperty("blog") BLOG,
            @JsonEnumDefaultValue UNKNOWN
        }

        private static final Logger logger = LoggerFactory.getLogger(Presentations.class);

        public static List<Presentations.Presentation> get(int eventId) throws IOException, InterruptedException {
            int count = 100;
            int returned = 0;
            int available = Integer.MAX_VALUE;
            List<Presentations.Presentation> presentations = new ArrayList<>();

            while (returned < available) {
                URI uri = URI.create("https://connpass.com/api/v2/events/" + eventId + "/presentations/?count=" + count + "&start=" + (returned + 1));
                logger.debug("Request: {}", uri);

                Presentations response = Connpass.get(uri, Presentations.class);
                available = response.resultsAvailable();
                returned += response.resultsReturned();

                presentations.addAll(response.presentations());

                // Connpass API は、1秒間に1リクエストまでの制限 (スロットリング) がある。
                // そのため、余裕を見込んで間隔を開けてリクエストする。
                Thread.sleep(INTERVAL);
            }

            logger.debug("Collected count: {}", presentations.size());

            return presentations;
        }
    }
}
