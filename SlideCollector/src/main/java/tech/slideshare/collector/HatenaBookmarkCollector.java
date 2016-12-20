package tech.slideshare.collector;

import tech.slideshare.collector.rss.Rss;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HatenaBookmarkCollector implements SlideCollector {
    private final String url;

    public HatenaBookmarkCollector(String url) {
        this.url = url;
    }

    @Override
    public Stream<Slide> getSlides() throws JAXBException, MalformedURLException {
        JAXBContext context = JAXBContext.newInstance(Rss.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");

        Rss r = (Rss) unmarshaller.unmarshal(new URL(url));

        return r.items.stream()
                .filter(i -> i.subject != null && i.subject.equals("テクノロジー"))
                .map(i -> {
                    Date date = null;

                    try {
                        date = format.parse(i.date);

                        return new Slide(i.title, i.link, date, i.title);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
