package tech.slideshare.rss;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Stream;

public class HatenaBookmark {

    private final String url;

    public HatenaBookmark(String url) {
        this.url = url;
    }

    public Stream<Item> getTechnology() throws JAXBException, MalformedURLException {
        return get().filter(i -> i.subject != null && i.subject.equals("テクノロジー"));
    }

    public Stream<Item> get() throws JAXBException, MalformedURLException {
        JAXBContext context = JAXBContext.newInstance(Rss.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();

        Rss r = (Rss) unmarshaller.unmarshal(new URL(url));

        return r.items.stream();
    }
}
