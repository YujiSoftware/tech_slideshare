package tech.slideshare.collector;

import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.Test;
import tech.slideshare.rss.Item;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DocswellCollectorTest {

    private Item getItem() {
        var item = new Item();
        item.title = "主体性を失わせるアンチパターンをやってしまっていた話 | ドクセル";
        item.link = "https://www.docswell.com/s/kojimadev/534M2Z-2022-04-27-002038";
        item.description = "作者について: 30代後半から発信活動を始めて人生が楽しくなりました。 主にC#/設計技法/マネジメント/チームビルディングの情報を発信します。 デブサミ2020関西ベストスピーカー賞1位。 ITエンジニア向けの月刊誌「Software Design」4月号より連載記事を執筆中。 デンソークリエイト所属。発言は個人の見解。 スライド...";
        item.date = "2022-05-02T05:51:00Z";
        item.subject = new String[]{"テクノロジー"};

        return item;
    }

    @Test
    public void collect() throws IOException, JAXBException {
        var item = getItem();
        var collector = new DocswellCollector(() -> Stream.of(item));

        List<Slide> slides = collector.collect().collect(Collectors.toList());
        assertEquals(1, slides.size());

        Slide slide = slides.get(0);
        assertEquals("主体性を失わせるアンチパターンをやってしまっていた話", slide.getTitle());
        assertEquals("https://www.docswell.com/s/kojimadev/534M2Z-2022-04-27-002038", slide.getLink());
        assertEquals("小島 優介", slide.getAuthor());
        assertEquals("kojimadev", slide.getTwitter());
        assertEquals(ZonedDateTime.of(2022, 5, 2, 14, 51, 0, 0, ZoneId.of("Asia/Tokyo")), slide.getDate());
        assertEquals("Engineering Manager Meetup #10 での発表資料です。\r\nhttps://engineering-manager-meetup.connpass.com/event/239203/", slide.getDescription());
        assertEquals("https://www.docswell.com/thumbnail/NPJX1D85JX.jpg", slide.getImage());
    }

}