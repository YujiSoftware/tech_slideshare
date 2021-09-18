package tech.slideshare.collector;

import jakarta.xml.bind.JAXBException;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.slideshare.rss.Bookmark;
import tech.slideshare.rss.HatenaBookmark;
import tech.slideshare.rss.Item;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class GoogleSlideCollector implements SlideCollector {

    private static final Logger logger = LoggerFactory.getLogger(GoogleSlideCollector.class);

    public static final int TIMEOUT = (int) TimeUnit.MILLISECONDS.toMinutes(1);

    private final Bookmark bookmark;

    public GoogleSlideCollector() {
        this.bookmark = new HatenaBookmark("https://b.hatena.ne.jp/entrylist?url=docs.google.com/presentation&mode=rss");
    }

    public GoogleSlideCollector(Bookmark bookmark) {
        this.bookmark = bookmark;
    }

    @Override
    public Stream<Slide> collect() throws JAXBException, IOException {
        return bookmark.get()
                .map(i -> getSlide(i).orElse(null))
                .filter(Objects::nonNull);
    }

    private static Optional<Slide> getSlide(Item item) {
        try {
            String link = item.link;
            Document doc = Jsoup.connect(link).timeout(TIMEOUT).get();

            // 権限が必要なページの場合、docs.google.com から accounts.google.com にリダイレクトされる。
            // その場合、プレゼンテーションは取得できないので処理をスキップする。
            URL location = new URL(doc.location());
            if (!location.getHost().equals("docs.google.com")) {
                return Optional.empty();
            }

            // /pub の場合、embedURL は取得できない
            if (!location.getPath().endsWith("/pub")) {
                // URL を正規化
                // (GoogleDocs の場合、embedURL から /preview のページを正とする)
                Optional<String> embed = doc.getElementsByTag("meta")
                        .stream()
                        .filter(e -> e.attr("itemprop").equals("embedURL"))
                        .findFirst()
                        .map(e -> e.attr("content"));

                // embedURL を取得できない場合、プレゼンテーションページではないと判断
                if (embed.isEmpty()) {
                    return Optional.empty();
                }

                if (!link.equals(embed.get())) {
                    link = embed.get();
                    doc = Jsoup.connect(link).timeout(TIMEOUT).get();
                }
            }

            // アンカーやクエリーを取り除いたURLを生成
            // (これらの有無によって、コンテンツは変わらないため)
            URL url = new URL(link);
            link = new URL(url.getProtocol(), url.getHost(), url.getPath()).toString();

            String title = doc.getElementsByTag("meta")
                    .stream()
                    .filter(e -> e.attr("property").equals("og:title"))
                    .findFirst()
                    .map(e -> e.attr("content"))
                    .orElse(doc.title().replaceAll(" - Google スライド", ""));
            Optional<String> author = getAuthor(link);

            return Optional.of(new Slide(title, link, item.date, author));
        } catch (HttpStatusException e) {
            logger.warn(String.format("Can't get GoogleSlide document. [url=%s, statusCode=%d]", e.getUrl(), e.getStatusCode()), e);
            return Optional.empty();
        } catch (IOException e) {
            logger.warn("Can't get GoogleSlide document.", e);
            return Optional.empty();
        }
    }

    private static Optional<String> getAuthor(String link) {
        // TODO; 取得方法がなさそう
        return Optional.empty();
    }
}
