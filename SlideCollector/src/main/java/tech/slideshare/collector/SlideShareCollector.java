package tech.slideshare.collector;

import jakarta.xml.bind.JAXBException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.slideshare.rss.Bookmark;
import tech.slideshare.rss.HatenaBookmark;
import tech.slideshare.rss.Item;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class SlideShareCollector implements SlideCollector {

    private static final Logger logger = LoggerFactory.getLogger(SlideShareCollector.class);

    public static final String USER_AGENT
            = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:91.0) Gecko/20100101 Firefox/91.0";

    private final Bookmark bookmark;

    public SlideShareCollector() {
        bookmark = new HatenaBookmark("https://b.hatena.ne.jp/entrylist?url=http%3A%2F%2Fwww.slideshare.net%2F&mode=rss");
    }

    public SlideShareCollector(Bookmark bookmark) {
        this.bookmark = bookmark;
    }

    @Override
    public Stream<Slide> collect() throws JAXBException, MalformedURLException {
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

            String title = doc.title();
            Optional<String> author = getAuthor(doc);

            return Optional.of(new Slide(title, link, item.date, author));
        } catch (IOException e) {
            logger.warn("Can't get slideshare document.", e);
            return Optional.empty();
        }
    }

    private static Optional<String> getAuthor(Document doc) {
        try {
            Optional<String> author = doc
                    .getElementsByTag("meta")
                    .stream()
                    .filter(e -> e.attr("name").equals("slideshow_author"))
                    .findFirst()
                    .map(e -> e.attr("content"));
            if (author.isEmpty()) {
                return Optional.empty();
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
                    });
        } catch (IOException e) {
            logger.warn("Can't get slideshare author.", e);
            return Optional.empty();
        }
    }
}
