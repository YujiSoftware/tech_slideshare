package tech.slideshare.crawler;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SlideShareCrawlerTest {

    private final SlideShareCrawler crawler = new SlideShareCrawler();

    @Test
    void crawl() throws IOException {
        String link = "https://www.slideshare.net/YujiSoftware/jep280-java-9";
        List<String> actual = crawler.crawl(link);

        assertEquals(23, actual.size());
        assertEquals("""
                Java 9 で
                文字列結合の
                処理が変わるぞ！
                準備はいいか！？
                @YujiSoftware""", actual.get(0));
        assertEquals("""
                問題
                • ＋演算子による文字列結合は最終的に
                どのような処理になる？
                private static String test(String str, int value) {
                return "ABC” + str + value;
                }""", actual.get(1));
    }

    @Test
    public void プライベート() throws IOException {
        String link = "https://www.slideshare.net/akiranakagawa3/20231003-2023onlinepdf";

        List<String> actual = crawler.crawl(link);
        assertEquals(0, actual.size());
    }

    @Test
    public void 削除済み() throws IOException {
        String link = "https://www.slideshare.net/AkiraNagai4/6-256787902";
        List<String> actual = crawler.crawl(link);

        assertEquals(0, actual.size());
    }

    @Test
    public void ユーザ削除済み() throws IOException {
        String link = "https://www.slideshare.net/kanametunes/nistcybersecurity-framework-250257381";
        List<String> actual = crawler.crawl(link);

        assertEquals(0, actual.size());
    }
}