package tech.slideshare.rss;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Stream;

public class HatenaBookmark implements Bookmark {

    private final String url;

    public HatenaBookmark(String url) {
        this.url = url;
    }

    public Stream<Item> get() throws JAXBException, MalformedURLException {
        JAXBContext context = JAXBContext.newInstance(Rss.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();

        Rss r = (Rss) unmarshaller.unmarshal(new URL(url));

        return r.items.stream().filter(i -> i.subject != null && i.subject.equals("テクノロジー"));
    }
}
