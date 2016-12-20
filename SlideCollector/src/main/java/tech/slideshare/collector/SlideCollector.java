package tech.slideshare.collector;

import javax.xml.bind.JAXBException;
import java.net.MalformedURLException;
import java.util.stream.Stream;

public interface SlideCollector {
    public Stream<Slide> getSlides() throws JAXBException, MalformedURLException;
}
