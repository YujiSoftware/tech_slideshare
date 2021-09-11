package tech.slideshare.collector;

import jakarta.xml.bind.JAXBException;
import org.jsoup.Jsoup;
import tech.slideshare.rss.Bookmark;
import tech.slideshare.rss.HatenaBookmark;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Optional;
import java.util.stream.Stream;

public class SlideShareCollector implements SlideCollector {

    public static final String USER_AGENT
            = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:91.0) Gecko/20100101 Firefox/91.0";

    private final Bookmark bookmark;

    public SlideShareCollector() {
        bookmark = new HatenaBookmark("https://b.hatena.ne.jp/entrylist?url=http%3A%2F%2Fwww.slideshare.net%2F&mode=rss");
    }

    public SlideShareCollector(Bookmark bookmark) {
        this.bookmark = bookmark;
    }

    @Override
    public Stream<Slide> collect() throws JAXBException, MalformedURLException {
        return bookmark.getTechnology()
                .filter(i -> !i.title.contains("film"))
                .filter(i -> !i.title.contains("Film"))
                .filter(i -> !i.title.contains("!VAR4"))
                .filter(i -> !i.title.contains("4KTUBE-HD"))
                .filter(i -> !i.link.contains("/embed_code/"))
                .peek(i -> i.link = i.link.replaceAll("/mobile/", "/"))
                .filter(i -> i.link.split("/").length > 4)  // https://www.slideshare.net/ConsommeDoping のようなユーザページを含まないための対応
                .map(i -> new Slide(i, () -> getAuthor(i.link)));
    }

    private static Optional<String> getAuthor(String link) {
        try {
            // SlideShare はデフォルトでモバイル用のページを返してくるので、
            // 明示的にPC用のユーザエージェントを設定する必要がある
            Optional<String> author = Jsoup.connect(link).userAgent(USER_AGENT).get()
                    .getElementsByTag("meta")
                    .stream()
                    .filter(e -> e.attr("name").equals("slideshow_author"))
                    .findFirst()
                    .map(e -> e.attr("content"));
            if (author.isEmpty()) {
                return Optional.empty();
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
                    });
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
