package tech.slideshare.collector;

import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tech.slideshare.rss.Item;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Backpaper0CollectorTest {

    private Item getItem() {
        var item = new Item();
        item.title = "Spring WebFluxの話";
        item.link = "https://backpaper0.github.io/ghosts/reactive/";
        item.description = "";
        item.date = "2021-08-14T15:12:29Z";
        item.subject = "テクノロジー";    // TODO: 複数あるみたい

        return item;
    }

    @Test
    public void collect() throws IOException, JAXBException {
        var item = getItem();
        var collector = new Backpaper0Collector(() -> Stream.of(item));

        List<Slide> slides = collector.collect().collect(Collectors.toList());
        assertEquals(1, slides.size());

        Slide slide = slides.get(0);
        assertEquals(item.title, slide.getTitle());
        assertEquals(item.link, slide.getLink());
        assertEquals("うらがみ", slide.getAuthor());
        assertEquals("backpaper0", slide.getTwitter());
        assertEquals(ZonedDateTime.of(2021, 8, 15, 0, 12, 29, 0, ZoneId.of("Asia/Tokyo")), slide.getDate());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://backpaper0.github.io/ghosts/reactive/#1",
            "https://backpaper0.github.io/ghosts/reactive/index.html",
            "https://backpaper0.github.io/ghosts/reactive/index.html#1"
    })
    void 正規化(String link) throws IOException, JAXBException {
        var item = getItem();
        item.link = link;
        var collector = new Backpaper0Collector(() -> Stream.of(item));

        List<Slide> slides = collector.collect().collect(Collectors.toList());
        assertEquals(1, slides.size());

        Slide slide = slides.get(0);
        assertEquals("https://backpaper0.github.io/ghosts/reactive/", slide.getLink());

    }
}