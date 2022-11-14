package tech.slideshare.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tech.slideshare.collector.Slide;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static tech.slideshare.Assertions.assertSlide;

public class SlideShareParserTest {

    private final SlideShareParser parser = new SlideShareParser();

    @Test
    public void parse() {
        Slide expected = new Slide(
                "Cmdstanr入門とreduce_sum()解説",
                "https://www.slideshare.net/simizu706/cmdstanrreducesum",
                "Hiroshi Shimizu",
                "simizu706",
                "Cmdstanrとreduce_sum()の使い方を解説します",
                "https://cdn.slidesharecdn.com/ss_thumbnails/cmdstanrintroduction-210913075930-thumbnail.jpg?width=640&amp;height=640&amp;fit=bounds"
        );

        Optional<Slide> actual = parser.parse(expected.getLink());
        assertTrue(actual.isPresent());

        Slide slide = actual.get();
        assertSlide(expected, slide);
    }

    @Test
    public void twitterリンクなし() {
        String link = "https://www.slideshare.net/yarakawa/ss-250079982";

        Optional<Slide> actual = parser.parse(link);
        assertTrue(actual.isPresent());

        Slide slide = actual.get();
        assertEquals("Yuma Ohgami", slide.getAuthor());
        assertNull(slide.getTwitter());
    }

    @Test
    public void presentation以外を除外() {
        String link = "https://www.slideshare.net/YujiSoftware";

        Optional<Slide> actual = parser.parse(link);
        assertTrue(actual.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            // タイトルに 4KTUBE-HD を含む
            "https://www.slideshare.net/imoneyjon/4ktubehd-ben-is-back-stream-german-ben-is-back-stream-122889509",
            // タイトルに {{!VAR4} を含む
            "https://www.slideshare.net/desirait1988ixx1/sehen-complete-stream-deutsch-hd-aquaman-2018-siegen-cinestar-kinoprogramm-und-var4-123099787",
            // 1ページしかない
            "https://www.slideshare.net/ebookreviewpro/the-role-of-knowledge-in-human-life",
    })
    public void スパムを除外(String link) {
        Optional<Slide> actual = parser.parse(link);
        assertTrue(actual.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://www.slideshare.net/slideshow/embed_code/key/7gOoDv2qMSirPN",
            "https://www.slideshare.net/mobile/YujiSoftware/jep280-java-9",
    })
    public void 正規化(String link) {
        Optional<Slide> actual = parser.parse(link);
        assertTrue(actual.isPresent());

        Slide slide = actual.get();
        assertEquals("https://www.slideshare.net/YujiSoftware/jep280-java-9", slide.getLink());
    }
}
