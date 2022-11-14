package tech.slideshare.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tech.slideshare.collector.Slide;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tech.slideshare.Assertions.assertSlide;

public class Backpaper0ParserTest {

    private final Backpaper0Parser parser = new Backpaper0Parser();

    @Test
    public void parse() {
        Slide expected = new Slide(
                "Spring WebFluxの話",
                "https://backpaper0.github.io/ghosts/reactive/",
                "うらがみ",
                "backpaper0",
                null,
                null
        );

        Optional<Slide> actual = parser.parse(expected.getLink());
        assertTrue(actual.isPresent());

        Slide slide = actual.get();
        assertSlide(expected, slide);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://backpaper0.github.io/ghosts/reactive/#1",
            "https://backpaper0.github.io/ghosts/reactive/index.html",
            "https://backpaper0.github.io/ghosts/reactive/index.html#1"
    })
    void 正規化(String link) {
        Optional<Slide> actual = parser.parse(link);
        assertTrue(actual.isPresent());

        Slide slide = actual.get();
        assertEquals("https://backpaper0.github.io/ghosts/reactive/", slide.getLink());

    }
}
