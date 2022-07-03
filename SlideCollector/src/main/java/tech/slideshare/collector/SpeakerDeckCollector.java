package tech.slideshare.collector;

import jakarta.xml.bind.JAXBException;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.slideshare.rss.Bookmark;
import tech.slideshare.rss.HatenaBookmark;
import tech.slideshare.rss.Item;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class SpeakerDeckCollector implements SlideCollector {

    private static final Logger logger = LoggerFactory.getLogger(SpeakerDeckCollector.class);

    private final Bookmark bookmark;

    public SpeakerDeckCollector() {
        this.bookmark = new HatenaBookmark("https://b.hatena.ne.jp/entrylist?url=http%3A%2F%2Fspeakerdeck.com%2F&mode=rss");
    }

    public SpeakerDeckCollector(Bookmark bookmark) {
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
            Document doc = Jsoup.connect(link).get();

            // URL を正規化
            Optional<String> canonical = doc.getElementsByTag("link")
                    .stream()
                    .filter(e -> e.attr("rel").equals("canonical"))
                    .findFirst()
                    .map(e -> e.attr("href"));
            if (canonical.isPresent() && !link.equals(canonical.get())) {
                link = canonical.get();
                doc = Jsoup.connect(link).get();
            }

            // スライドページかどうかの判定
            boolean isPresentation = doc.getElementsByTag("meta")
                    .stream()
                    .filter(e -> e.attr("name").equals("stats-view_type"))
                    .anyMatch(e -> e.attr("content").equals("talk"));
            if (!isPresentation) {
                return Optional.empty();
            }

            String title = doc.select("meta[property~=og:title]").attr("content");
            String author = getAuthor(link);
            String description = doc.select("meta[property~=og:description]").attr("content");
            String image = doc.select("meta[property~=og:image]").attr("content");

            return Optional.of(new Slide(title, link, item.date, author, null, description, image));
        } catch (HttpStatusException e) {
            logger.warn(String.format("Can't get SpeakerDeck document. [url=%s, statusCode=%d]", e.getUrl(), e.getStatusCode()), e);
            return Optional.empty();
        } catch (IOException e) {
            logger.warn("Can't get SpeakerDeck document.", e);
            return Optional.empty();
        }
    }

    private static String getAuthor(String link) {
        try {
            return Jsoup.connect(new URI(link + "/../").normalize().toASCIIString()).get()
                    .getElementsByTag("h1")
                    .stream()
                    .findFirst()
                    .map(Element::text)
                    .orElse(null);
        } catch (HttpStatusException e) {
            logger.warn(String.format("Can't get SpeakerDeck author. [url=%s, statusCode=%d]", e.getUrl(), e.getStatusCode()), e);
            return null;
        } catch (IOException | URISyntaxException e) {
            logger.warn("Can't get SpeakerDeck author.", e);
            return null;
        }
    }
}
