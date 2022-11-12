package tech.slideshare.collector;

import jakarta.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.slideshare.rss.Bookmark;
import tech.slideshare.rss.HatenaBookmark;
import tech.slideshare.rss.Item;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class HatenaBackpaper0Collector implements SlideCollector {
    private static final Logger logger = LoggerFactory.getLogger(HatenaGoogleSlideCollector.class);

    private final Bookmark bookmark;

    public HatenaBackpaper0Collector() {
        this.bookmark = new HatenaBookmark("https://b.hatena.ne.jp/entrylist?url=http%3A%2F%2Fbackpaper0.github.io%2Fghosts%2F&mode=rss");
    }

    public HatenaBackpaper0Collector(Bookmark bookmark) {
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
            URL url = new URL(item.link);
            String link = new URL(url.getProtocol(), url.getHost(), url.getPath().replaceFirst("index.html", "")).toString();

            return Optional.of(new Slide(item.title, link, item.date, "うらがみ", "backpaper0", null, null));
        } catch (MalformedURLException e) {
            return Optional.empty();
        }
    }
}
