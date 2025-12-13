package tech.slideshare.crawler;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DocswellCrawler implements Crawler {

    @Override
    public String getURL() {
        return "https://www.docswell.com/";
    }

    public List<String> crawl(String url) throws IOException {
        Document doc;
        try {
            doc = Jsoup.connect(url).get();
        } catch (HttpStatusException e) {
            // ファイルが削除された場合、404 (NOT FOUND) が返ってくる
            if (e.getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                return Collections.emptyList();
            }
            throw e;
        }

        Elements header = doc.getElementsContainingOwnText("各ページのテキスト");
        if (header.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> contents = new ArrayList<>();
        Element element = header.getFirst().nextElementSibling();
        while (element != null && element.tagName().equals("div")) {
            Elements p = element.getElementsByTag("p");
            String text = p.stream()
                    .map(Element::textNodes)
                    .flatMap(Collection::stream)
                    .map(TextNode::getWholeText)
                    .collect(Collectors.joining("\n"));
            contents.add(text.trim());

            element = element.nextElementSibling();
        }

        return contents;
    }
}
