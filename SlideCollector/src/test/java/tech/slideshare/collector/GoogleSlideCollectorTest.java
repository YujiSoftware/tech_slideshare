package tech.slideshare.collector;

import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tech.slideshare.rss.Item;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GoogleSlideCollectorTest {

    private Item getItem() {
        var item = new Item();
        item.title = "YAVIの紹介";
        item.link = "https://docs.google.com/presentation/u/0/d/1aw5cXqTxvMbBvTUU4TiF2oTUkG8orDujA5IRQGtIDiw/preview";
        item.description = "YAVIの紹介 Toshiaki Maki (@making), https://ik.am JSUG勉強会 2021-07-02 質問はTwitter(#jsug)にお願いします";
        item.date = "2021-09-09T13:53:13Z";
        item.subject = "テクノロジー";    // TODO: 複数あるみたい

        return item;
    }

    @Test
    public void collect() throws IOException, JAXBException {
        var item = getItem();
        var collector = new GoogleSlideCollector(() -> Stream.of(item));

        List<Slide> slides = collector.collect().collect(Collectors.toList());
        assertEquals(1, slides.size());

        Slide slide = slides.get(0);
        assertEquals(item.title, slide.getTitle());
        assertEquals(item.link, slide.getLink());
        assertNull(slide.getAuthor());
        assertNull(slide.getTwitter());
        assertEquals(ZonedDateTime.of(2021, 9, 9, 22, 53, 13, 0, ZoneId.of("Asia/Tokyo")), slide.getDate());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://docs.google.com/presentation/u/0/d/1aw5cXqTxvMbBvTUU4TiF2oTUkG8orDujA5IRQGtIDiw/edit",
            "https://docs.google.com/presentation/u/0/d/1aw5cXqTxvMbBvTUU4TiF2oTUkG8orDujA5IRQGtIDiw/edit?usp=embed_googleplus",
            "https://docs.google.com/presentation/u/0/d/1aw5cXqTxvMbBvTUU4TiF2oTUkG8orDujA5IRQGtIDiw/edit#slide=id.p",
            "https://docs.google.com/presentation/u/0/d/1aw5cXqTxvMbBvTUU4TiF2oTUkG8orDujA5IRQGtIDiw/mobilepresent",
    })
    public void 正規化(String link) throws IOException, JAXBException {
        var item = getItem();
        item.link = link;
        var collector = new GoogleSlideCollector(() -> Stream.of(item));

        List<Slide> slides = collector.collect().collect(Collectors.toList());
        assertEquals(1, slides.size());

        Slide slide = slides.get(0);
        assertEquals("https://docs.google.com/presentation/u/0/d/1aw5cXqTxvMbBvTUU4TiF2oTUkG8orDujA5IRQGtIDiw/preview", slide.getLink());
    }

    @Test
    public void publicを正規化() throws IOException, JAXBException {
        var item = getItem();
        item.title = "The newsletter of RBS updates - RubyKaigi Takeout 2021 - Google スライド";
        item.link = "https://docs.google.com/presentation/d/e/2PACX-1vREU6ZguqLxGk_k1l3zvKbRo_TbMTKN3yEgfzrjA85foVXrmeYvWnOTefsaBycsb9m6H924VsZw_YKt/pub?start=false&loop=false&delayms=3000&slide=id.p";
        item.description = "The newsletter of RBS updates RubyKaigi Takeout 2021 Sep. 10th それでは、The newsletter of RBS updatesというタイトルで話させていただきたいと思います。 ----- 25 mins https://rubykaigi.org/2021-takeout Proposal";
        item.date = "2021-09-12T06:24:05Z";
        item.subject = "テクノロジー";
        var collector = new GoogleSlideCollector(() -> Stream.of(item));

        List<Slide> slides = collector.collect().collect(Collectors.toList());
        assertEquals(1, slides.size());

        Slide slide = slides.get(0);
        assertEquals("https://docs.google.com/presentation/d/e/2PACX-1vREU6ZguqLxGk_k1l3zvKbRo_TbMTKN3yEgfzrjA85foVXrmeYvWnOTefsaBycsb9m6H924VsZw_YKt/pub", slide.getLink());
    }

    @Test
    public void accessDenied() throws IOException, JAXBException {
        var item = getItem();
        item.title = "Google スライド - オンラインでプレゼンテーションを作成/編集できる無料サービスです";
        item.link = "https://docs.google.com/presentation/d/1PjqrNO4r0-lRcJrNls-iT2CJ-kubNY31c9eJVo_eFSk/edit?resourcekey=0-pyJknY9TXGs9BUh0F0UjpA";
        item.description = "パソコン、携帯電話、タブレットで新しいプレゼンテーションを作成し、他のユーザーと同時に共同編集できます。インターネット接続の有無に関係なく作業できます。Google スライドを使用して、PowerPoint ファイルを編集できます。本サービスは Google から無料で提供されています。";
        item.date = "2021-09-09T13:53:13Z";
        item.subject = "テクノロジー";

        var collector = new GoogleSlideCollector(() -> Stream.of(item));

        List<Slide> slides = collector.collect().collect(Collectors.toList());
        assertEquals(0, slides.size());
    }

}