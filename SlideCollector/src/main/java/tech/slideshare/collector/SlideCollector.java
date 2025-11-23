package tech.slideshare.collector;

import jakarta.xml.bind.JAXBException;

import java.io.IOException;
import java.util.List;

public interface SlideCollector {
    List<Slide> collect() throws JAXBException, IOException, InterruptedException;

    String name();
}
