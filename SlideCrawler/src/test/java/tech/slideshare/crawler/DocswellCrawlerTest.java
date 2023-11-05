package tech.slideshare.crawler;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DocswellCrawlerTest {
    @Test
    void crawl() throws IOException {
        DocswellCrawler crawler = new DocswellCrawler();
        List<String> actual = crawler.crawl("https://www.docswell.com/s/ydnjp/Z8GWWW-2023-07-26-115048");

        assertEquals(38, actual.size());
        assertEquals("""
                情報区分 公開
                ansibleとCI/CDで進めるサーバ構築/運⽤
                in QUNOG 26
                ヤフー株式会社
                サイトオペレーション本部
                永島
                薫
                ©2023 Yahoo Japan Corporation All rights reserved.""", actual.get(0));
    }
}