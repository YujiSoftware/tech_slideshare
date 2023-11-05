package tech.slideshare.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SpeakerDeckCrawler implements Crawler {

    @Override
    public String getURL() {
        return "https://speakerdeck.com/";
    }

    public List<String> crawl(String url) throws IOException {
        List<String> contents = new ArrayList<>();

        Document doc = Jsoup.connect(url).get();
        Elements elements = doc.select("#transcript ol li div.slide-transcript p");
        if (elements.isEmpty()) {
            return contents;
        }

        StringBuilder builder = new StringBuilder();
        for (Element element : elements) {
            builder.setLength(0);

            List<Node> nodes = element.childNodes();
            for (Node node : nodes) {
                if (node.nodeName().equals("br")) {
                    builder.append("\n");
                } else if (node instanceof TextNode n) {
                    builder.append(n.text());
                } else if (node instanceof Element e) {
                    builder.append(e.text());
                }
            }
            contents.add(builder.toString().trim());
        }

        return contents;
    }
}
