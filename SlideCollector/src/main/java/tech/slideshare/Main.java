package tech.slideshare;

import tech.slideshare.rss.Rss;
import tech.slideshare.rss.Item;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.net.MalformedURLException;
import java.net.URL;

public class Main {

    private static final String HATENA_BOOKMARK_SLIDESHARE =
            "http://b.hatena.ne.jp/entrylist?url=http%3A%2F%2Fwww.slideshare.net%2F&mode=rss";

    public static void main(String[] args) throws JAXBException, MalformedURLException {
        JAXBContext context = JAXBContext.newInstance(Rss.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        Rss r = (Rss) unmarshaller.unmarshal(new URL(HATENA_BOOKMARK_SLIDESHARE));
        System.out.println(r.channel.description);

        for (Item i : r.items) {
            System.out.println(i.title);
        }
    }
}
