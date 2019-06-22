package tech.slideshare.collector;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import tech.slideshare.rss.HatenaBookmark;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.stream.Stream;

public class SlideShareCollector implements SlideCollector {

    private static final String ZERO_WIDTH_SPACE = "\u200B";

    private HatenaBookmark collector
            = new HatenaBookmark("https://b.hatena.ne.jp/entrylist?url=http%3A%2F%2Fwww.slideshare.net%2F&mode=rss");

    @Override
    public Stream<Slide> collect() throws JAXBException, MalformedURLException {
        return collector.getTechnology()
                .filter(i -> !i.title.contains("film"))
                .filter(i -> !i.title.contains("Film"))
                .filter(i -> !i.title.contains("!VAR4"))
                .filter(i -> !i.title.contains("4KTUBE-HD"))
                .filter(i -> !i.link.contains("/embed_code/"))
                .peek(i -> i.link = i.link.replaceAll("/mobile/", "/"))
                .map(i -> new Slide(i, () -> getAuthor(i.link)));
    }

    private static Optional<String> getAuthor(String link) {
        try {
            Document doc = Jsoup.connect(new URI(link + "/../").normalize().toASCIIString()).get();

            for (Element metaTag : doc.getElementsByTag("meta")) {
                String property = metaTag.attr("property");
                String content = metaTag.attr("content");

                if ("slideshare:name".equals(property)) {
                    String twitter = doc.select("div.profile-social-links > a.twitter").attr("href");
                    if (!twitter.equals("")) {
                        // 通知を送るとうざがられてしまうので、@ の後ろにゼロ幅スペースを入れてメンションにならないようにする
                        return Optional.of(content + ", @" + ZERO_WIDTH_SPACE + twitter.substring(twitter.lastIndexOf('/') + 1));
                    } else {
                        return Optional.of(content);
                    }
                }
            }

            return Optional.empty();
        } catch (IOException | URISyntaxException e) {
            return Optional.empty();
        }
    }
}
