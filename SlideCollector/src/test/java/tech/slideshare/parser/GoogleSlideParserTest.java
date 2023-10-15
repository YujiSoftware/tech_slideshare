package tech.slideshare.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tech.slideshare.collector.Slide;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tech.slideshare.Assertions.assertSlide;

public class GoogleSlideParserTest {

    private final GoogleSlideParser parser = new GoogleSlideParser();

    @Test
    public void parse() {
        Slide expected = new Slide(
                "スケールするGo",
                "https://docs.google.com/presentation/d/1ROqjuCrr39OirOaz3XlvDhKjwmUkzR9zf_i9JMaBIPQ/preview",
                null,
                null,
                "スケールするGo 2022年7月1日（金） @Qiita Night 資料URL：https://tenn.in/goscaling",
                "https://lh3.googleusercontent.com/docs/AHkbwyKEaT_XXYHu0NbEnRPFZcUVs907nv7IVIxIEsYXtjRACsMOfnZak4nU1OcyyT6XMXrP8znBTOadUZxcR6OqG9Yp0-JKHYN3RdUXlC_rg29g=w1200-h630-p"
        );

        Optional<Slide> actual = parser.parse(expected.getLink());
        assertTrue(actual.isPresent());

        Slide slide = actual.get();
        assertSlide(expected, slide);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://docs.google.com/presentation/u/0/d/1ROqjuCrr39OirOaz3XlvDhKjwmUkzR9zf_i9JMaBIPQ/edit",
            "https://docs.google.com/presentation/u/0/d/1ROqjuCrr39OirOaz3XlvDhKjwmUkzR9zf_i9JMaBIPQ/edit?usp=embed_googleplus",
            "https://docs.google.com/presentation/u/0/d/1ROqjuCrr39OirOaz3XlvDhKjwmUkzR9zf_i9JMaBIPQ/edit#slide=id.p",
            "https://docs.google.com/presentation/u/0/d/1ROqjuCrr39OirOaz3XlvDhKjwmUkzR9zf_i9JMaBIPQ/mobilepresent",
    })
    public void 正規化(String link) {
        Optional<Slide> actual = parser.parse(link);
        assertTrue(actual.isPresent());

        Slide slide = actual.get();
        assertEquals("https://docs.google.com/presentation/u/0/d/1ROqjuCrr39OirOaz3XlvDhKjwmUkzR9zf_i9JMaBIPQ/preview", slide.getLink());
    }

    @Test
    public void publicを正規化() {
        String link = "https://docs.google.com/presentation/d/e/2PACX-1vREU6ZguqLxGk_k1l3zvKbRo_TbMTKN3yEgfzrjA85foVXrmeYvWnOTefsaBycsb9m6H924VsZw_YKt/pub?start=false&loop=false&delayms=3000&slide=id.p";

        Optional<Slide> actual = parser.parse(link);
        assertTrue(actual.isPresent());

        Slide slide = actual.get();
        assertEquals("https://docs.google.com/presentation/d/e/2PACX-1vREU6ZguqLxGk_k1l3zvKbRo_TbMTKN3yEgfzrjA85foVXrmeYvWnOTefsaBycsb9m6H924VsZw_YKt/pub", slide.getLink());
    }

    @Test
    public void accessDenied() {
        String link = "https://docs.google.com/presentation/d/1PjqrNO4r0-lRcJrNls-iT2CJ-kubNY31c9eJVo_eFSk/edit?resourcekey=0-pyJknY9TXGs9BUh0F0UjpA";

        Optional<Slide> actual = parser.parse(link);
        assertTrue(actual.isEmpty());
    }
}
