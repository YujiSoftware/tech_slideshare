package tech.slideshare.crawler;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoogleSlideCrawlerTest {
    @Test
    void crawl() throws IOException {
        GoogleSlideCrawler crawler = new GoogleSlideCrawler();
        List<String> actual = crawler.crawl("https://docs.google.com/presentation/d/1F8bcMKlVFU3W17AGTpEZO8N7RIh643BlrBtXncF9AIM/preview");

        assertEquals(10, actual.size());
        assertEquals("""
                ざっくりと便利ツール紹介(仮)
                たぁさん""", actual.get(0));
        assertEquals("""
                ご清聴ありがとうございました！""", actual.get(9));
    }

    @Test
    public void unauthorized() throws IOException {
        GoogleSlideCrawler crawler = new GoogleSlideCrawler();
        List<String> actual = crawler.crawl("https://docs.google.com/presentation/d/1IsIrlle_KnEcc3xfenJyVL-ZPVMQ6vkb_qlMurTDsRs/preview");

        assertTrue(actual.isEmpty());
    }

    @Test
    public void requiredLogin() throws IOException {
        GoogleSlideCrawler crawler = new GoogleSlideCrawler();
        List<String> actual = crawler.crawl("https://docs.google.com/presentation/d/1C7VB67KwNW790U07y3kwsUoXwY-rZGWuKFhCvklEf48/edit");

        assertTrue(actual.isEmpty());
    }
}