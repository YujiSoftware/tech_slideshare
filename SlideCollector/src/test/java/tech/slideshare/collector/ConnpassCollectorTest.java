package tech.slideshare.collector;

import org.junit.jupiter.api.Test;
import tech.slideshare.cache.NullCache;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static tech.slideshare.Assertions.assertSlide;

class ConnpassCollectorTest {

    private final ConnpassCollector collector = new ConnpassCollector();

    @Test
    public void collectSlideShare() throws IOException, InterruptedException {
        Slide expected = new Slide(
                "220217 RPAコミュニティ様向け",
                "https://www.slideshare.net/slideshow/220217-rpa/251242672",
                "ラリオス 川口",
                null,
                "初心者・エンジニアじゃなくてもクラウドを学んで業務に活用できるチャンス！",
                "https://cdn.slidesharecdn.com/ss_thumbnails/220217rpa-220225024130-thumbnail.jpg?width=640&height=640&fit=bounds"
        );

        List<Slide> actual = collector.collectSlide(new NullCache(), 238601);
        assertEquals(1, actual.size());

        Slide slide = actual.get(0);
        assertSlide(expected, slide);
    }

    @Test
    public void collectSpeakerDeck() throws IOException, InterruptedException {
        Slide expected = new Slide(
                "221019 “活きたマニュアル”作成・整備のコツと業務の棚卸_シイエム・シイ",
                "https://speakerdeck.com/comucal/221019-manual-creation",
                "comucal",
                null,
                "221019_“活きたマニュアル”作成・整備のコツと業務の棚卸_CMC",
                "https://files.speakerdeck.com/presentations/028e6aea8e734be4ba04c4938d328c93/slide_0.jpg?23136989"
        );

        List<Slide> actual = collector.collectSlide(new NullCache(), 263117);
        assertEquals(1, actual.size());

        Slide slide = actual.get(0);
        assertSlide(expected, slide);
    }

    @Test
    public void collectGoogleSlide() throws IOException, InterruptedException {
        Slide expected = new Slide(
                "第1回手順書Night Q&A 集",
                "https://docs.google.com/presentation/d/1MK6esbKewj3Z1ubs-MWuVHdEci2so7qjkJdunk8FWEs/preview",
                null,
                null,
                "第1回手順書Night Q&A 集 https://apcommunications.connpass.com/event/238403/ セッション2・LT 1",
                "https://lh7-us.googleusercontent.com/docs/AHkbwyI4Nt0eC-KbwhyPlkpjYT3P6T8eT1fDi4s22T2Wy8iWZKzQVH-Zk2OMWys1x0xQr4XrxP-dnHsnpE0WLoETUxVzOoQS_QOYEa2_Ws9DuWRd_gh9BSs=w1200-h630-p"
        );

        List<Slide> actual = collector.collectSlide(new NullCache(), 238403);
        assertEquals(3, actual.size());

        Slide slide = actual.stream().filter(s -> s.getLink().contains("docs.google.com")).findFirst().orElseThrow();
        assertSlide(expected, slide);
    }


    @Test
    public void collectDocswell() throws IOException, InterruptedException {
        Slide expected = new Slide(
                "Power Automate for desktop 教室 vol.1～きぬあさ先生と生徒あきイカ～",
                "https://www.docswell.com/s/kinuasa/518PPZ-20220817-RPALT",
                "きぬあさ",
                "kinuasa",
                "「Power Automate for desktop 教室 vol.1」(RPACommunity主催)の登壇資料です。\r\n(2022年8月17日オンライン開催)",
                "https://bcdn.docswell.com/page/LE1V3MK27G.jpg?width=480"
        );

        List<Slide> actual = collector.collectSlide(new NullCache(), 257292);
        assertEquals(1, actual.size());

        Slide slide = actual.get(0);
        assertSlide(expected, slide);
    }
}