package tech.slideshare.collector;

import jakarta.xml.bind.JAXBException;
import tech.slideshare.cache.Cache;
import tech.slideshare.cache.TempFileCache;
import tech.slideshare.parser.Parser;
import tech.slideshare.rss.Bookmark;

import java.io.IOException;
import java.util.List;

public class HatenaBookmarkCollector implements SlideCollector {

    private final String name;

    private final Parser parser;

    private final Bookmark bookmark;

    public HatenaBookmarkCollector(String name, Parser parser, Bookmark bookmark) {
        this.name = name;
        this.parser = parser;
        this.bookmark = bookmark;
    }

    @Override
    public List<Slide> collect() throws JAXBException, IOException {
        Cache cache = new TempFileCache(HatenaBookmarkCollector.class.getSimpleName());

        List<Slide> list = collect(cache);

        cache.flush();

        return list;
    }

    public List<Slide> collect(Cache cache) throws JAXBException, IOException {
        return bookmark.get()
                .stream()
                .filter(i -> cache.add(i.link))
                .flatMap(i -> parser.parse(i.link, i.getDate()).stream())
                .toList();
    }

    @Override
    public String name() {
        return "HatenaBookmarkCollector (" + name + ")";
    }
}
