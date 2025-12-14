package tech.slideshare;

import tech.slideshare.collector.Slide;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Assertions {
    public static void assertSlide(Slide expected, Slide actual) {
        assertEquals(expected.getTitle(), actual.getTitle());
        assertEquals(expected.getLink(), actual.getLink());
        assertEquals(expected.getAuthor(), actual.getAuthor());
        assertEquals(expected.getTwitter(), actual.getTwitter());
        assertEquals(expected.getHashTag(), actual.getHashTag());
        assertEquals(expected.getDescription(), actual.getDescription());

        // maybe null
        if (!Objects.equals(expected.getImage(), actual.getImage())) {
            // パスの最後の部分は無視して比較（頻繁に変わるため）
            // IN:  https://lh7-us.googleusercontent.com/docs/AHkbwyLwuNZGNYytiZ4VwzBu9g9SEp8r_Z46TrP2ztYKXJDM9zepXHT6zmYtoABbdSrAnKfoU9BvARHvby6TDKJb-XaF9bzyRUeVDLDgvPSfbj4FxXRAW0Z-=w1200-h630-p
            // OUT: https://lh7-us.googleusercontent.com/docs/
            assertEquals(
                    expected.getImage().replaceFirst("(.*/)[^/]+", "$1")
                    , actual.getImage().replaceFirst("(.*/)[^/]+", "$1")
            );
        }
    }
}
