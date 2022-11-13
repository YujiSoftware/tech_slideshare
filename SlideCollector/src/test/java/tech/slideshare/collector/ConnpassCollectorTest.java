package tech.slideshare.collector;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static tech.slideshare.Assertions.assertSlide;

class ConnpassCollectorTest {

    private final ConnpassCollector collector = new ConnpassCollector();

    @Test
    public void collectSlideShare() throws IOException {
        Slide expected = new Slide(
                "220217 RPAコミュニティ様向け",
                "https://www.slideshare.net/ssuser6e5c8d/220217-rpa",
                ZonedDateTime.now(),
                "ラリオス 川口",
                null,
                "初心者・エンジニアじゃなくてもクラウドを学んで業務に活用できるチャンス！",
                "https://cdn.slidesharecdn.com/ss_thumbnails/220217rpa-220225024130-thumbnail.jpg?width=640&amp;height=640&amp;fit=bounds"
        );

        Stream<Slide> stream = collector.collectSlide("https://rpacommunity.connpass.com/event/238601/");
        List<Slide> actual = stream.toList();

        assertEquals(1, actual.size());

        Slide slide = actual.get(0);
        assertSlide(expected, slide);
    }

    @Test
    public void collectSpeakerDeck() throws IOException {
        Slide expected = new Slide(
                "221019 “活きたマニュアル”作成・整備のコツと業務の棚卸_シイエム・シイ",
                "https://speakerdeck.com/comucal/221019-manual-creation",
                ZonedDateTime.now(),
                "comucal",
                null,
                "221019_“活きたマニュアル”作成・整備のコツと業務の棚卸_CMC",
                "https://files.speakerdeck.com/presentations/028e6aea8e734be4ba04c4938d328c93/slide_0.jpg?23136989"
        );

        Stream<Slide> stream = collector.collectSlide("https://rpacommunity.connpass.com/event/263117/");
        List<Slide> actual = stream.toList();

        assertEquals(1, actual.size());

        Slide slide = actual.get(0);
        assertSlide(expected, slide);
    }

    @Test
    public void collectGoogleSlide() throws IOException {
        Slide expected = new Slide(
                "第1回手順書Night Q&A 集",
                "https://docs.google.com/presentation/d/1MK6esbKewj3Z1ubs-MWuVHdEci2so7qjkJdunk8FWEs/preview",
                ZonedDateTime.now(),
                null,
                null,
                "第1回手順書Night Q&A 集 https://apcommunications.connpass.com/event/238403/ セッション2・LT 1",
                "https://lh3.googleusercontent.com/lu47KSxcXBGpuQfZbuPOr5IckYGnxBYgQDgCls0rA3qtl7oJzXZuiAemGiJthcBhAQCVg78mTOQ2cA=w1200-h630-p"
        );

        Stream<Slide> stream = collector.collectSlide("https://apcommunications.connpass.com/event/238403/");
        List<Slide> actual = stream.toList();

        assertEquals(3, actual.size());

        Slide slide = actual.stream().filter(s -> s.getLink().contains("docs.google.com")).findFirst().orElseThrow();
        assertSlide(expected, slide);
    }


    @Test
    public void collectDocswell() throws IOException {
        Slide expected = new Slide(
                "Power Automate for desktop 教室 vol.1～きぬあさ先生と生徒あきイカ～",
                "https://www.docswell.com/s/kinuasa/518PPZ-20220817-RPALT",
                ZonedDateTime.now(),
                "きぬあさ",
                "kinuasa",
                "「Power Automate for desktop 教室 vol.1」(RPACommunity主催)の登壇資料です。\r\n(2022年8月17日オンライン開催)",
                "https://www.docswell.com/thumbnail/LE1V3MK27G.jpg"
        );

        Stream<Slide> stream = collector.collectSlide("https://rpacommunity.connpass.com/event/257292/");
        List<Slide> actual = stream.toList();

        assertEquals(1, actual.size());

        Slide slide = actual.get(0);
        assertSlide(expected, slide);
    }
}