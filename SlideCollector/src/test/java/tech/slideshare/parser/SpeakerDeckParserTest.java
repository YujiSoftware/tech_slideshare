package tech.slideshare.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tech.slideshare.collector.Slide;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tech.slideshare.Assertions.assertSlide;

public class SpeakerDeckParserTest {

    private final ZonedDateTime NOW = ZonedDateTime.now();

    private final SpeakerDeckParser parser = new SpeakerDeckParser();

    @Test
    public void parse() {
        Slide expected = new Slide(
                "Active Recordから考える次の10年を見据えた技術選定 / Architecture decision for the next 10 years at PIXTA",
                "https://speakerdeck.com/yasaichi/architecture-decision-for-the-next-10-years-at-pixta",
                NOW,
                "yasaichi",
                null,
                "September 15, 2021 @ iCARE Dev Meetup #25",
                "https://files.speakerdeck.com/presentations/c8556affd0f3401388af6d664d320c42/slide_0.jpg?19034361"
        );

        Optional<Slide> actual = parser.parse(expected.getLink(), expected.getDate());
        assertTrue(actual.isPresent());

        Slide slide = actual.get();
        assertSlide(expected, slide);
    }

    @Test
    public void presentation以外を除外() {
        String link = "https://speakerdeck.com/yasaichi";

        Optional<Slide> actual = parser.parse(link, NOW);
        assertTrue(actual.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://speakerdeck.com/player/c8556affd0f3401388af6d664d320c42",
            "https://speakerdeck.com/yasaichi/architecture-decision-for-the-next-10-years-at-pixta?slide=2",
    })
    public void 正規化(String link) {
        Optional<Slide> actual = parser.parse(link, NOW);
        assertTrue(actual.isPresent());

        Slide slide = actual.get();
        assertEquals("https://speakerdeck.com/yasaichi/architecture-decision-for-the-next-10-years-at-pixta", slide.getLink());
    }
}
