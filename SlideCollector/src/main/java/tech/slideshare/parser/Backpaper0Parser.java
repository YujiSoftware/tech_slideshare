package tech.slideshare.parser;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.slideshare.collector.Slide;

import java.io.IOException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.Optional;

public class Backpaper0Parser implements Parser {

    private static final Logger logger = LoggerFactory.getLogger(Backpaper0Parser.class);

    @Override
    public Optional<Slide> parse(String link, ZonedDateTime date) {
        try {
            URL url = new URL(link);
            String index = new URL(url.getProtocol(), url.getHost(), url.getPath().replaceFirst("index.html", "")).toString();

            Document doc = Jsoup.connect(index).get();
            Elements title = doc.getElementsByTag("title");

            return Optional.of(new Slide(title.text(), index, date, "うらがみ", "backpaper0", null, null));
        } catch (HttpStatusException e) {
            logger.warn(String.format("Can't get Backpaper0 document. [url=%s, statusCode=%d]", e.getUrl(), e.getStatusCode()), e);
            return Optional.empty();
        } catch (IOException e) {
            logger.warn("Can't get Backpaper0 document.", e);
            return Optional.empty();
        }
    }
}
