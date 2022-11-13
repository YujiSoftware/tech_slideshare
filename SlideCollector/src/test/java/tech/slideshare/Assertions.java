package tech.slideshare;

import tech.slideshare.collector.Slide;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Assertions {
    public static void assertSlide(Slide expected, Slide actual) {
        assertEquals(expected.getTitle(), actual.getTitle());
        assertEquals(expected.getLink(), actual.getLink());
        assertEquals(expected.getAuthor(), actual.getAuthor());
        assertEquals(expected.getTwitter(), actual.getTwitter());
        assertEquals(expected.getDescription(), actual.getDescription());
        assertEquals(expected.getImage(), actual.getImage());
    }
}
