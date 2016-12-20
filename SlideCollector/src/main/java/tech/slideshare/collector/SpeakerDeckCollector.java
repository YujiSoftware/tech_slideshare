package tech.slideshare.collector;

import javax.xml.bind.JAXBException;
import java.net.MalformedURLException;
import java.util.stream.Stream;

public class SpeakerDeckCollector extends HatenaBookmarkCollector {

    public SpeakerDeckCollector() {
        super("http://b.hatena.ne.jp/entrylist?url=http%3A%2F%2Fspeakerdeck.com%2F&mode=rss");
    }

    @Override
    public Stream<Slide> getSlides() throws JAXBException, MalformedURLException {
        return super.getSlides().peek(s -> s.setTitle(s.getTitle().replace(" // Speaker Deck", "")));
    }
}
