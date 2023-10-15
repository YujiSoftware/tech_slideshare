package tech.slideshare.parser;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.slideshare.collector.Slide;

import java.io.IOException;
import java.util.Optional;

public class SlideShareParser implements Parser {

    private static final Logger logger = LoggerFactory.getLogger(SlideShareParser.class);

    public static final String USER_AGENT
            = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:91.0) Gecko/20100101 Firefox/91.0";

    public Optional<Slide> parse(String link) {
        try {
            if (link.contains("://www.slideshare.net/secret/")) {
                return Optional.empty();
            }

            // SlideShare はデフォルトでモバイル用のページを返してくるので、
            // 明示的にPC用のユーザエージェントを設定する必要がある
            Document doc = Jsoup.connect(link).userAgent(USER_AGENT).get();

            // URL を正規化
            Optional<String> canonical = doc.getElementsByTag("link")
                    .stream()
                    .filter(e -> e.attr("rel").equals("canonical"))
                    .findFirst()
                    .map(e -> e.attr("href"));
            if (canonical.isPresent() && !link.equals(canonical.get())) {
                link = canonical.get();
                doc = Jsoup.connect(link).userAgent(USER_AGENT).get();
            }

            // スライドページかどうかの判定
            boolean isPresentation = doc.getElementsByTag("meta")
                    .stream()
                    .filter(e -> e.attr("name").equals("twitter:card"))
                    .anyMatch(e -> e.attr("content").equals("player"));
            if (!isPresentation) {
                return Optional.empty();
            }

            // 1ページしかないものは、スパムと判定して除外
            long pageCount = doc.getElementsByTag("ul")
                    .stream()
                    .filter(e -> e.classNames().stream().map(String::toLowerCase).anyMatch(c -> c.startsWith("transcript")))
                    .mapToLong(e -> e.getElementsByTag("li").size())
                    .sum();
            if (pageCount <= 1) {
                return Optional.empty();
            }

            String title = doc.select("meta[property~=og:title]").attr("content");
            String author = getAuthor(doc);
            String twitter = getTwitter(doc);
            String description = doc.select("meta[property~=og:description]").attr("content");
            String image = doc.select("meta[property~=og:image]").attr("content");

            // スパム対策として、特定のキーワードを含むタイトルのものは除外
            if (title.contains("film") || title.contains("Film") || title.contains("-var4-") || title.contains("4KTUBE-HD") || title.contains("{{!VAR4}")) {
                return Optional.empty();
            }

            return Optional.of(new Slide(title, link, author, twitter, description, image));
        } catch (HttpStatusException e) {
            logger.warn(String.format("Can't get SlideShare document. [url=%s, statusCode=%d]", e.getUrl(), e.getStatusCode()), e);
            return Optional.empty();
        } catch (IOException e) {
            logger.warn("Can't get SlideShare document.", e);
            return Optional.empty();
        }
    }

    private static String getAuthor(Document doc) {
        return doc.select("div[class^='AuthorLink'] a")
                .stream()
                .findFirst()
                .map(e -> e.text().trim())
                .orElse(null);
    }

    private static String getTwitter(Document doc) {
        try {
            Optional<String> author = doc
                    .getElementsByTag("meta")
                    .stream()
                    .filter(e -> e.attr("name").equals("slideshow_author"))
                    .findFirst()
                    .map(e -> e.attr("content"));
            if (author.isEmpty()) {
                return null;
            }

            return Jsoup.connect(author.get()).userAgent(USER_AGENT).get()
                    .getElementsByTag("a")
                    .stream()
                    .filter(e -> e.classNames().contains("twitter"))
                    .findFirst()
                    .map(e -> e.attr("href"))
                    .map(t -> {
                        String[] paths = t.split("/");

                        return paths[paths.length - 1];
                    })
                    .orElse(null);
        } catch (HttpStatusException e) {
            logger.warn(String.format("Can't get SlideShare author. [url=%s, statusCode=%d]", e.getUrl(), e.getStatusCode()), e);
            return null;
        } catch (IOException e) {
            logger.warn("Can't get SlideShare author.", e);
            return null;
        }
    }
}
