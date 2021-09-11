package tech.slideshare.collector;

import tech.slideshare.rss.HatenaBookmark;

import javax.xml.bind.JAXBException;
import java.net.MalformedURLException;
import java.util.Optional;
import java.util.stream.Stream;

public class Backpaper0Collector implements SlideCollector {

    private final HatenaBookmark collector
            = new HatenaBookmark("https://b.hatena.ne.jp/entrylist?url=http%3A%2F%2Fbackpaper0.github.io%2Fghosts%2F&mode=rss");

    @Override
    public Stream<Slide> collect() throws JAXBException, MalformedURLException {
        return collector.getTechnology()
                .peek(i -> i.link = i.link.replaceAll("#.*$", ""))
                .map(i -> new Slide(i, () -> Optional.of("うらがみ, @\u200Bbackpaper0")));
    }
}
