package tech.slideshare.collector;

import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.slideshare.rss.Item;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HatenaBookmarkCollectorTest {

    private Slide dummySlide;
    private Item dummyItem;

    @BeforeEach
    public void beforeEach() {
        this.dummySlide = new Slide(
                "title", "link", ZonedDateTime.now(), "author", "twitter", "description", "image"
        );

        var item = new Item();
        item.link = "http://example.com/";
        item.date = "2021-09-16T09:56:20Z";
        this.dummyItem = item;
    }

    @Test
    public void collect() throws JAXBException, IOException {
        var collector = new HatenaBookmarkCollector(
                (link, date) -> Optional.of(dummySlide),
                () -> Stream.of(dummyItem)
        );

        var slides = collector.collect().toList();
        assertEquals(1, slides.size());
    }

    @Test
    public void ignoreEmpty() throws JAXBException, IOException {
        var collector = new HatenaBookmarkCollector(
                (link, date) -> Optional.empty(),
                () -> Stream.of(dummyItem)
        );

        var slides = collector.collect().toList();
        assertEquals(0, slides.size());
    }
}