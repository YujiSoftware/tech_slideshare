package tech.slideshare.collector;

import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
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

public class HatenaSlideShareCollectorTest {

    @Test
    public void collect() throws IOException, JAXBException {
        var item = new Item();
        item.title = "Cmdstanr入門とreduce_sum()解説";
        item.link = "https://www.slideshare.net/simizu706/cmdstanrreducesum";
        item.description = "Cmdstanrとreduce_sum()の使い方を解説します";
        item.date = "2021-09-14T12:18:46Z";
        item.subject = new String[]{"テクノロジー"};

        var collector = new HatenaSlideShareCollector(() -> Stream.of(item));

        List<Slide> slides = collector.collect().collect(Collectors.toList());
        assertEquals(1, slides.size());

        Slide slide = slides.get(0);
        assertEquals(item.title, slide.getTitle());
        assertEquals(item.link, slide.getLink());
        assertEquals("Hiroshi Shimizu", slide.getAuthor());
        assertEquals("simizu706", slide.getTwitter());
        assertEquals(ZonedDateTime.of(2021, 9, 14, 21, 18, 46, 0, ZoneId.of("Asia/Tokyo")), slide.getDate());
        assertEquals("Cmdstanrとreduce_sum()の使い方を解説します", slide.getDescription());
        assertEquals("https://cdn.slidesharecdn.com/ss_thumbnails/cmdstanrintroduction-210913075930-thumbnail-4.jpg?cb=1631520361", slide.getImage());
    }

    @Test
    public void twitterリンクなし() throws IOException, JAXBException {
        var item = new Item();
        item.title = "セガサターンマシン語プログラミングの紹介";
        item.link = "https://www.slideshare.net/yarakawa/ss-250079982";
        item.description = "× You’ve unlocked unlimited downloads on SlideShare! Your download should start automatically, if not click here to download You also get free access to Scribd! Instant access to millions of ebooks, audiobooks, magazines, podcasts, and more. Read and listen offline with any device. Free access to...";
        item.date = "2021-09-04T06:53:05Z";
        item.subject = new String[]{"テクノロジー"};

        var collector = new HatenaSlideShareCollector(() -> Stream.of(item));

        List<Slide> slides = collector.collect().collect(Collectors.toList());
        assertEquals(1, slides.size());

        Slide slide = slides.get(0);
        assertEquals("Yuma Ohgami", slide.getAuthor());
        assertNull(slide.getTwitter());
    }

    @Test
    public void presentation以外を除外() throws IOException, JAXBException {
        var item = new Item();
        item.title = "YujiSoftware | SlideShare";
        item.link = "https://www.slideshare.net/YujiSoftware";

        var collector = new HatenaSlideShareCollector(() -> Stream.of(item));

        List<Slide> slides = collector.collect().collect(Collectors.toList());
        assertEquals(0, slides.size());
    }

    static List<Object[]> spam() {
        return List.of(
                new Object[]{
                        "[4KTUBE-HD]™ Ben Is Back Stream German Ben Is Back Stream",
                        "https://www.slideshare.net/imoneyjon/4ktubehd-ben-is-back-stream-german-ben-is-back-stream-122889509"
                },
                new Object[]{
                        "Sehen Complete Stream Deutsch HD Aquaman 2018 - Siegen CineStar - Kin…",
                        "https://www.slideshare.net/desirait1988ixx1/sehen-complete-stream-deutsch-hd-aquaman-2018-siegen-cinestar-kinoprogramm-und-var4-123099787"
                },
                new Object[]{
                        // 1ページしかない
                        "The Role of Knowledge in Human Life",
                        "https://www.slideshare.net/ebookreviewpro/the-role-of-knowledge-in-human-life"
                }
        );
    }

    @ParameterizedTest
    @MethodSource("spam")
    public void スパムを除外(String title, String link) throws IOException, JAXBException {
        var item = new Item();
        item.title = title;
        item.link = link;

        var collector = new HatenaSlideShareCollector(() -> Stream.of(item));

        List<Slide> slides = collector.collect().collect(Collectors.toList());
        assertEquals(0, slides.size());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://www.slideshare.net/slideshow/embed_code/key/7gOoDv2qMSirPN",
            "https://www.slideshare.net/mobile/YujiSoftware/jep280-java-9",
    })
    public void 正規化(String link) throws IOException, JAXBException {
        var item = new Item();
        item.title = "JEP280: Java 9 で文字列結合の処理が変わるぞ！準備はいいか！？ #jjug_ccc";
        item.link = link;
        item.description = "JEP280: Java 9 で文字列結合の処理が変わるぞ！準備はいいか！？ #jjug_ccc 1. Java 9 で 文字列結合の 処理が変わるぞ！ 準備はいいか！？ @YujiSoftware 2. 問題 • ＋演算子による文字列結合は最終的に どのような処理になる？ private static String test(String str, int value) { return \"ABC” + str + value; } 3...";
        item.date = "2017-11-18T12:44:04Z";
        item.subject = new String[]{"テクノロジー"};

        var collector = new HatenaSlideShareCollector(() -> Stream.of(item));

        List<Slide> slides = collector.collect().collect(Collectors.toList());
        assertEquals(1, slides.size());

        Slide slide = slides.get(0);
        assertEquals("https://www.slideshare.net/YujiSoftware/jep280-java-9", slide.getLink());
    }
}