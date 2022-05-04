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
import static org.junit.jupiter.api.Assertions.assertNull;

class SpeakerDeckCollectorTest {

    private Item getItem() {
        var item = new Item();
        item.title = "Active Recordから考える次の10年を見据えた技術選定 / Architecture decision for the next 10 years at PIXTA";
        item.link = "https://speakerdeck.com/yasaichi/architecture-decision-for-the-next-10-years-at-pixta";
        item.description = "Transcript Active Record から考える 次の 10 年を見据えた技術選定 Yuichi Goto (@_yasaichi) September 15，2021 @ iCARE Dev Meetup #25 自己紹介 Yuichi Goto（@_yasaichi） ピクスタ株式会社 執行役員 CTO 兼 開発部長 2020 年 7 月発売の「パーフェクト Ruby on Rails 【増補改訂版】」の 共著者（Part 5 担当） ...";
        item.date = "2021-09-16T09:56:20Z";
        item.subject = new String[]{"テクノロジー"};

        return item;
    }

    @Test
    public void collect() throws IOException, JAXBException {
        var item = getItem();
        var collector = new SpeakerDeckCollector(() -> Stream.of(item));

        List<Slide> slides = collector.collect().collect(Collectors.toList());
        assertEquals(1, slides.size());

        Slide slide = slides.get(0);
        assertEquals(item.title, slide.getTitle());
        assertEquals(item.link, slide.getLink());
        assertEquals("yasaichi", slide.getAuthor());
        assertNull(slide.getTwitter());
        assertEquals(ZonedDateTime.of(2021, 9, 16, 18, 56, 20, 0, ZoneId.of("Asia/Tokyo")), slide.getDate());
    }

    @Test
    public void presentation以外を除外() throws IOException, JAXBException {
        var item = new Item();
        item.title = "yasaichi (@yasaichi) on Speaker Deck";
        item.link = "https://speakerdeck.com/yasaichi";

        var collector = new SlideShareCollector(() -> Stream.of(item));

        List<Slide> slides = collector.collect().collect(Collectors.toList());
        assertEquals(0, slides.size());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://speakerdeck.com/player/c8556affd0f3401388af6d664d320c42",
            "https://speakerdeck.com/yasaichi/architecture-decision-for-the-next-10-years-at-pixta?slide=2",
    })
    public void 正規化(String link) throws IOException, JAXBException {
        var item = getItem();
        item.link = link;

        var collector = new SpeakerDeckCollector(() -> Stream.of(item));

        List<Slide> slides = collector.collect().collect(Collectors.toList());
        assertEquals(1, slides.size());

        Slide slide = slides.get(0);
        assertEquals("https://speakerdeck.com/yasaichi/architecture-decision-for-the-next-10-years-at-pixta", slide.getLink());
    }
}