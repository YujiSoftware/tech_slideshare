package tech.slideshare.collector;

import tech.slideshare.rss.HatenaBookmark;

import javax.xml.bind.JAXBException;
import java.net.MalformedURLException;
import java.util.Optional;
import java.util.stream.Stream;

public class GoogleSlideCollector implements SlideCollector {

    private HatenaBookmark collector
            = new HatenaBookmark("https://b.hatena.ne.jp/entrylist?url=docs.google.com/presentation&mode=rss");

    @Override
    public Stream<Slide> collect() throws JAXBException, MalformedURLException {
        return collector.getTechnology()
                .filter(i -> !i.title.equals("Google スライド - オンラインでプレゼンテーションを作成/編集できる無料サービスです"))
                .peek(i -> i.link = i.link.replaceAll("\\?.*", ""))
                .peek(i -> i.link = i.link.replaceAll("#.*", ""))
                .peek(i -> i.link = i.link.replaceAll("/mobilepresent$", "/edit"))
                .peek(i -> i.link = i.link.replaceAll("/preview$", "/edit"))
                .peek(i -> i.title = i.title.replaceAll(" - Google スライド", ""))
                .map(i -> new Slide(i, () -> getAuthor(i.link)));
    }

    private static Optional<String> getAuthor(String link) {
        // TODO; 取得方法がなさそう
        return Optional.empty();
    }
}
