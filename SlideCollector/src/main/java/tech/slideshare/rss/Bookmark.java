package tech.slideshare.rss;

import jakarta.xml.bind.JAXBException;

import java.io.IOException;
import java.util.List;

public interface Bookmark {
    List<Item> get() throws JAXBException, IOException;
}
