package tech.slideshare.collector;

import jakarta.xml.bind.JAXBException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import tech.slideshare.rss.HatenaBookmark;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.stream.Stream;

public class SpeakerDeckCollector implements SlideCollector {

    private final HatenaBookmark collector
            = new HatenaBookmark("https://b.hatena.ne.jp/entrylist?url=http%3A%2F%2Fspeakerdeck.com%2F&mode=rss");

    @Override
    public Stream<Slide> collect() throws JAXBException, MalformedURLException {
        return collector.get()
                .filter(i -> !i.link.contains("://speakerdeck.com/player/"))
                .filter(i -> i.link.split("/").length > 4)  // https://speakerdeck.com/katzmanncatarina71 のようなユーザページを含まないための対応
                .peek(i -> i.link = i.link.replaceAll("\\?slide=\\d+", ""))
                .peek(i -> i.title = i.title.replaceAll(" - Speaker Deck", ""))
                .map(i -> new Slide(i, getAuthor(i.link)));
    }

    private static Optional<String> getAuthor(String link) {
        try {
            Document doc = Jsoup.connect(new URI(link + "/../").normalize().toASCIIString()).get();
            Elements header = doc.getElementsByTag("h1");

            return (header.size() > 0) ? Optional.of(header.get(0).text()) : Optional.empty();
        } catch (IOException | URISyntaxException e) {
            return Optional.empty();
        }
    }
}
