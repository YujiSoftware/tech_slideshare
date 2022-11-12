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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class HatenaSlideShareCollector implements SlideCollector {

    private static final Logger logger = LoggerFactory.getLogger(HatenaSlideShareCollector.class);

    public static final String USER_AGENT
            = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:91.0) Gecko/20100101 Firefox/91.0";

    private final Bookmark bookmark;

    public HatenaSlideShareCollector() {
        bookmark = new HatenaBookmark("https://b.hatena.ne.jp/entrylist?url=http%3A%2F%2Fwww.slideshare.net%2F&mode=rss");
    }

    public HatenaSlideShareCollector(Bookmark bookmark) {
        this.bookmark = bookmark;
    }

    @Override
    public Stream<Slide> collect() throws JAXBException, IOException {
        return bookmark.get()
                .filter(i -> !i.title.contains("film"))
                .filter(i -> !i.title.contains("Film"))
                .filter(i -> !i.link.contains("-var4-"))
                .filter(i -> !i.title.contains("4KTUBE-HD"))
                .map(i -> getSlide(i).orElse(null))
                .filter(Objects::nonNull);
    }

    private static Optional<Slide> getSlide(Item item) {
        try {
            String link = item.link;
            if (link.contains("://www.slideshare.net/secret/")) {
                return Optional.empty();
            }

            // SlideShare はデフォルトでモバイル用のページを返してくるので、
            // 明示的にPC用のユーザエージェントを設定する必要がある
            Document doc = Jsoup.connect(link).userAgent(USER_AGENT).get();

            // URL を正規化
            Optional<String> canonical = doc.getElementsByTag("link")
                    .stream()
                    .filter(e -> e.attr("rel").equals("canonical"))
                    .findFirst()
                    .map(e -> e.attr("href"));
            if (canonical.isPresent() && !link.equals(canonical.get())) {
                link = canonical.get();
                doc = Jsoup.connect(link).userAgent(USER_AGENT).get();
            }

            // スライドページかどうかの判定
            boolean isPresentation = doc.getElementsByTag("meta")
                    .stream()
                    .filter(e -> e.attr("property").equals("og:type"))
                    .anyMatch(e -> e.attr("content").equals("slideshare:presentation"));
            if (!isPresentation) {
                return Optional.empty();
            }

            // 1ページしかないものは、スパムと判定して除外
            long pageCount = doc.getElementsByTag("ol")
                    .stream()
                    .filter(e -> e.classNames().contains("transcripts"))
                    .mapToLong(e -> e.getElementsByTag("li").size())
                    .sum();
            if (pageCount <= 1) {
                return Optional.empty();
            }

            String title = doc.select("meta[property~=og:title]").attr("content");
            String author = getAuthor(doc);
            String twitter = getTwitter(doc);
            String description = doc.select("meta[property~=og:description]").attr("content");
            String image = doc.select("meta[property~=og:image]").attr("content");

            return Optional.of(new Slide(title, link, item.date, author, twitter, description, image));
        } catch (HttpStatusException e) {
            logger.warn(String.format("Can't get SlideShare document. [url=%s, statusCode=%d]", e.getUrl(), e.getStatusCode()), e);
            return Optional.empty();
        } catch (IOException e) {
            logger.warn("Can't get SlideShare document.", e);
            return Optional.empty();
        }
    }

    private static String getAuthor(Document doc) {
        return doc.getElementsByTag("a")
                .stream()
                .filter(e -> e.attr("rel").equals("author"))
                .findFirst()
                .map(e -> e.text().trim())
                .orElse(null);
    }

    private static String getTwitter(Document doc) {
        try {
            Optional<String> author = doc
                    .getElementsByTag("meta")
                    .stream()
                    .filter(e -> e.attr("name").equals("slideshow_author"))
                    .findFirst()
                    .map(e -> e.attr("content"));
            if (author.isEmpty()) {
                return null;
            }

            return Jsoup.connect(author.get()).userAgent(USER_AGENT).get()
                    .getElementsByTag("a")
                    .stream()
                    .filter(e -> e.classNames().contains("twitter"))
                    .findFirst()
                    .map(e -> e.attr("href"))
                    .map(t -> {
                        String[] paths = t.split("/");

                        return paths[paths.length - 1];
                    })
                    .orElse(null);
        } catch (HttpStatusException e) {
            logger.warn(String.format("Can't get SlideShare author. [url=%s, statusCode=%d]", e.getUrl(), e.getStatusCode()), e);
            return null;
        } catch (IOException e) {
            logger.warn("Can't get SlideShare author.", e);
            return null;
        }
    }
}
