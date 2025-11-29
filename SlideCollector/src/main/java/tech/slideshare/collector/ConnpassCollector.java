package tech.slideshare.collector;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.slideshare.cache.Cache;
import tech.slideshare.cache.TempFileCache;
import tech.slideshare.connpass.Connpass;
import tech.slideshare.parser.*;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConnpassCollector implements SlideCollector {

    private static final Logger logger = LoggerFactory.getLogger(ConnpassCollector.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Parser docswellParser = new DocswellParser();

    private final Parser googleSlideParser = new GoogleSlideParser();

    private final Parser slideShareParser = new SlideShareParser();

    private final Parser speakerDeckParser = new SpeakerDeckParser();

    @Override
    public List<Slide> collect() throws IOException, InterruptedException {
        Cache cache = new TempFileCache(Connpass.class.getSimpleName());

        List<Slide> list = new ArrayList<>();
        for (Connpass.Event event : Connpass.getEvents(cache.updatedAt())) {
            List<Slide> found = collectSlide(cache, event.url());
            list.addAll(found);

            logger.debug("Event url: {}, found: {}", event.url(), found.size());
        }

        cache.flush();

        return list;
    }

    protected List<Slide> collectSlide(Cache cache, String url) throws IOException {
        List<Slide> list = new ArrayList<>();

        String presentation = url + "/presentation/";
        Document doc;
        try {
            doc = Jsoup.connect(presentation).get();
        } catch (HttpStatusException e) {
            // なぜかたまに削除されたページが含まれていることがある。その場合、無視する。
            if (e.getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                logger.warn(e.toString());
                return list;
            }
            throw e;
        }

        // SlideShare
        doc.getElementsByTag("div").select("[data-obj]")
                .stream()
                .map(e -> Data.readValue(e.attr("data-obj")))
                .filter(data -> data.serviceType.equals("slideshare"))
                .map(data -> data.extraData.get("embed_media_url"))
                .filter(cache::add)
                .flatMap(link -> slideShareParser.parse(link).stream())
                .forEach(list::add);

        // SpeakerDeck
        doc.getElementsByTag("div").select("[data-obj]")
                .stream()
                .map(e -> Data.readValue(e.attr("data-obj")))
                .filter(data -> data.serviceType.equals("speaker_deck"))
                .map(data -> "https:" + data.extraData.get("embed_media_url"))
                .filter(cache::add)
                .flatMap(link -> speakerDeckParser.parse(link).stream())
                .forEach(list::add);

        // GoogleSlide
        doc.getElementsByTag("a").stream()
                .map(link -> link.attr("href"))
                .filter(link -> link.startsWith("https://docs.google.com/presentation/"))
                .distinct()
                .filter(cache::add)
                .flatMap(link -> googleSlideParser.parse(link).stream())
                .forEach(list::add);

        // Docswell
        doc.getElementsByTag("a").stream()
                .map(link -> link.attr("href"))
                .filter(link -> link.startsWith("https://www.docswell.com/"))
                .distinct()
                .filter(cache::add)
                .flatMap(link -> docswellParser.parse(link).stream())
                .forEach(list::add);

        return list;
    }

    @Override
    public String name() {
        return this.getClass().getSimpleName();
    }

    private record Data(
            int id,
            String name,
            String description,
            @JsonProperty("service_type") String serviceType,
            @JsonProperty("extra_data") Map<String, String> extraData
    ) {
        private static Data readValue(String json) {
            return MAPPER.readValue(json, Data.class);
        }
    }
}
