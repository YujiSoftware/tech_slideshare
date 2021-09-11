package tech.slideshare.collector;

import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.Test;
import tech.slideshare.rss.Item;

import java.net.MalformedURLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SlideShareCollectorTest {

    @Test
    public void collect() throws MalformedURLException, JAXBException {
        var item = new Item();
        item.title = "JEP280: Java 9 で文字列結合の処理が変わるぞ！準備はいいか！？ #jjug_ccc";
        item.link = "https://www.slideshare.net/YujiSoftware/jep280-java-9";
        item.description = "JEP280: Java 9 で文字列結合の処理が変わるぞ！準備はいいか！？ #jjug_ccc 1. Java 9 で 文字列結合の 処理が変わるぞ！ 準備はいいか！？ @YujiSoftware 2. 問題 • ＋演算子による文字列結合は最終的に どのような処理になる？ private static String test(String str, int value) { return \"ABC” + str + value; } 3...";
        item.date = "2017-11-18T12:44:04Z";
        item.subject = "テクノロジー";    // TODO: 複数あるみたい

        var collector = new SlideShareCollector(() -> Stream.of(item));

        List<Slide> slides = collector.collect().collect(Collectors.toList());
        assertEquals(1, slides.size());

        Slide slide = slides.get(0);
        assertEquals(item.title, slide.getTitle());
        assertEquals(item.link, slide.getLink());
        assertEquals(Optional.of("YujiSoftware"), slide.getAuthor());
        assertEquals(ZonedDateTime.of(2017, 11, 18, 21, 44, 4, 0, ZoneId.of("Asia/Tokyo")), slide.getDate());
    }

    @Test
    public void twitterリンクなし() throws MalformedURLException, JAXBException {
        var item = new Item();
        item.title = "セガサターンマシン語プログラミングの紹介";
        item.link = "https://www.slideshare.net/yarakawa/ss-250079982";
        item.description = "× You’ve unlocked unlimited downloads on SlideShare! Your download should start automatically, if not click here to download You also get free access to Scribd! Instant access to millions of ebooks, audiobooks, magazines, podcasts, and more. Read and listen offline with any device. Free access to...";
        item.date = "2021-09-04T06:53:05Z";
        item.subject = "テクノロジー";

        var collector = new SlideShareCollector(() -> Stream.of(item));

        List<Slide> slides = collector.collect().collect(Collectors.toList());
        assertEquals(1, slides.size());

        Slide slide = slides.get(0);
        assertEquals(Optional.empty(), slide.getAuthor());
    }
}