package tech.slideshare.crawler;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SlideShareCrawlerTest {
    @Test
    void crawl() throws IOException {
        SlideShareCrawler crawler = new SlideShareCrawler();
        List<String> actual = crawler.crawl("https://www.slideshare.net/YujiSoftware/jep280-java-9");

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

}