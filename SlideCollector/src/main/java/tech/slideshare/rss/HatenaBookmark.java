package tech.slideshare.rss;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class HatenaBookmark implements Bookmark {

    private static final Logger logger = LoggerFactory.getLogger(HatenaBookmark.class);

    private final String url;

    public HatenaBookmark(String url) {
        this.url = url;
    }

    public List<Item> get() throws JAXBException, IOException {
        JAXBContext context = JAXBContext.newInstance(Rss.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();

        Rss r = (Rss) unmarshaller.unmarshal(new URL(url));

        return r.items
                .stream()
                .filter(i -> i.subject != null)
                .filter(i -> Arrays.asList(i.subject).contains("テクノロジー"))
                .toList();
    }
}
