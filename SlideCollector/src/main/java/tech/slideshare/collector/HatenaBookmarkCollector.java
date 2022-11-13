package tech.slideshare.collector;

import jakarta.xml.bind.JAXBException;
import tech.slideshare.parser.Parser;
import tech.slideshare.rss.Bookmark;

import java.io.IOException;
import java.util.Objects;
import java.util.stream.Stream;

public class HatenaBookmarkCollector implements SlideCollector {
    private final Parser parser;

    private final Bookmark bookmark;

    public HatenaBookmarkCollector(Parser parser, Bookmark bookmark) {
        this.parser = parser;
        this.bookmark = bookmark;
    }

    @Override
    public Stream<Slide> collect() throws JAXBException, IOException {
        return bookmark.get()
                .map(i -> parser.parse(i.link, i.getDate()).orElse(null))
                .filter(Objects::nonNull);
    }
}
