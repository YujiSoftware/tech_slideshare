package tech.slideshare.collector;

import tech.slideshare.collector.rss.Item;

import javax.xml.bind.JAXBException;
import java.net.MalformedURLException;
import java.util.List;

public interface SlideCollector {
    public List<Slide> getSlides() throws JAXBException, MalformedURLException;
}
