package tech.slideshare.collector;

import jakarta.xml.bind.JAXBException;
import java.net.MalformedURLException;
import java.util.stream.Stream;

public interface SlideCollector {
    Stream<Slide> collect() throws JAXBException, MalformedURLException;
}
