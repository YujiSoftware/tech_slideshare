package tech.slideshare.rss;

import jakarta.xml.bind.JAXBException;

import java.io.IOException;
import java.util.stream.Stream;

public interface Bookmark {
    Stream<Item> get() throws JAXBException, IOException;
}
