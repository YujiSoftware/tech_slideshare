package tech.slideshare.parser;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.slideshare.collector.Slide;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

public class SpeakerDeckParser implements Parser {

    private static final Logger logger = LoggerFactory.getLogger(SpeakerDeckParser.class);

    public Optional<Slide> parse(String link) {
        try {
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

            // 4ページ以下は、スパムと判定して除外
            long pageCount = doc.getElementsByClass("slide-transcript").size();
            if (pageCount <= 4) {
                logger.debug("TotalSlides = {}: {}", pageCount, link);
                return Optional.empty();
            }

            String title = doc.select("meta[property~=og:title]").attr("content");
            String author = getAuthor(link);
            String description = doc.select("meta[property~=og:description]").attr("content");
            String image = doc.select("meta[property~=og:image]").attr("content");

            return Optional.of(new Slide(title, link, author, null, description, image));
        } catch (HttpStatusException e) {
            logger.warn("Can't get SpeakerDeck document. [url={}, statusCode={}]", e.getUrl(), e.getStatusCode(), e);
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
