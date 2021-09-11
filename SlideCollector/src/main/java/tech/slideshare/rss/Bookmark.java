package tech.slideshare.rss;

import jakarta.xml.bind.JAXBException;

import java.net.MalformedURLException;
import java.util.stream.Stream;

public interface Bookmark {
    Stream<Item> getTechnology() throws JAXBException, MalformedURLException;
}
