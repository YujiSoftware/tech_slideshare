package tech.slideshare.crawler;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpeakerDeckCrawlerTest {
    @Test
    void crawl() throws IOException {
        SpeakerDeckCrawler crawler = new SpeakerDeckCrawler();
        List<String> actual = crawler.crawl("https://speakerdeck.com/smiyawaki0820/2023-dot-08-dot-07-geography-and-language-mian-qiang-hui-number-4");

        assertEquals(80, actual.size());
        assertEquals("""
                LLM によるプログラムベース推論
                Shumpei Miyawaki
                keywalker,inc. / Tohoku Univ.
                @catshun_
                2023.08.07 Geography&Language 勉強会 #4
                https://sites.google.com/view/geography-and-language/studygroup
                • LLM 活⽤事例の⼀つとして プログラムベース推論の事例 を紹介
                • 勉強会を通してプログラムベース推論の⻑所・短所を議論し、
                LLM 活⽤の選択肢を増やす ことに議論を終着させる""", actual.get(0));
        assertEquals("""
                P.1
                • 勉強会⽤に突貫的に作成しており、内容に誤りがある可能性が⼤いにあります...
                • 内容の誤りや引⽤漏れ等がありましたらお伝えください
                • 発⾔は confabulation を含みます
                • 本勉強会は LLM 応⽤に関⼼のある⽅向けの発表を想定しています
                • 著者は地理空間情報の初学者であり ”学びたい” というスタンスで発表いたします
                留意事項""", actual.get(1));
    }

    @Test
    void fallback() throws IOException {
        SpeakerDeckCrawler crawler = new SpeakerDeckCrawler();
        List<String> actual = crawler.crawl("https://speakerdeck.com/01rabbit/mr-dot-rabbit-wen-itashi-haarukedo-shi-ji-nihajian-tashi-ganaihatukingugazietuto");

        assertEquals("""
                聞いた事はあるけど、実際には見た事がない
                ハッキングガジェット
                2017/12/2
                元祖 濱せっく #1
                @01ra66it""", actual.get(1));
    }
}