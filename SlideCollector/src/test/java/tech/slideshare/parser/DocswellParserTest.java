package tech.slideshare.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tech.slideshare.collector.Slide;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tech.slideshare.Assertions.assertSlide;

public class DocswellParserTest {

    private final DocswellParser parser = new DocswellParser();

    @Test
    public void parse() {
        Slide expected = new Slide(
                "主体性を失わせるアンチパターンをやってしまっていた話",
                "https://www.docswell.com/s/kojimadev/534M2Z-2022-04-27-002038",
                "小島 優介",
                "kojimadev",
                "Engineering Manager Meetup #10 での発表資料です。\r\nhttps://engineering-manager-meetup.connpass.com/event/239203/",
                "https://bcdn.docswell.com/page/NPJX1D85JX.jpg?width=480"
        );

        Optional<Slide> actual = parser.parse(expected.getLink());
        assertTrue(actual.isPresent());

        Slide slide = actual.get();
        assertSlide(expected, slide);
    }

    @Test
    public void 正規化() {
        String canonical = "https://www.docswell.com/s/ockeghem/K2PPNK-phpconf2022";
        String link = canonical + "?s=09";

        Optional<Slide> actual = parser.parse(link);
        assertTrue(actual.isPresent());

        Slide slide = actual.get();
        assertEquals(canonical, slide.getLink());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            // トップページ
            "https://www.docswell.com/",
            // ユーザページ
            "https://www.docswell.com/user/EpicGamesJapan",
    })
    public void ignore(String link) {
        Optional<Slide> actual = parser.parse(link);
        assertTrue(actual.isEmpty());
    }

    @Test
    public void redirect() {
        String url = "https://www.docswell.com/s/integrated1453/5DER7R-o11y/1";
        Slide expected = new Slide(
                "プロダクト開発エンジニア全員で取り組むオブザーバビリティ",
                "https://www.docswell.com/s/integrated1453/5DER7R-o11y",
                "Yuki Ando",
                "integrated1453",
                "TechBrew in 東京〜オブザーバビリティのベストプラクティス〜の発表資料です\r\nhttps://findy.connpass.com/event/312930/",
                "https://bcdn.docswell.com/page/L73RK8YPJ5.jpg?width=480"
        );

        Optional<Slide> actual = parser.parse(url);
        assertTrue(actual.isPresent());

        Slide slide = actual.get();
        assertSlide(expected, slide);
    }
}
