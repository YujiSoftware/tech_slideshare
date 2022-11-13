package tech.slideshare.collector;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import tech.slideshare.cache.Cache;
import tech.slideshare.cache.TempFileCache;
import tech.slideshare.connpass.Connpass;
import tech.slideshare.parser.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConnpassCollector implements SlideCollector {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Parser docswellParser = new DocswellParser();

    private final Parser googleSlideParser = new GoogleSlideParser();

    private final Parser slideShareParser = new SlideShareParser();

    private final Parser speakerDeckParser = new SpeakerDeckParser();

    @Override
    public List<Slide> collect() throws IOException {
        Cache cache = new TempFileCache(Connpass.class.getSimpleName());

        List<Slide> list = new ArrayList<>();
        for (Connpass.Event event : Connpass.getEvents()) {
            list.addAll(collectSlide(cache, event.eventUrl()));
        }

        cache.flush();

        return list;
    }

    protected List<Slide> collectSlide(Cache cache, String eventUrl) throws IOException {
        List<Slide> list = new ArrayList<>();

        String presentation = eventUrl + "/presentation/";
        Document doc = Jsoup.connect(presentation).get();

        // SlideShare
        doc.getElementsByTag("div").select("[data-obj]")
                .stream()
                .map(e -> Data.readValue(e.attr("data-obj")))
                .filter(data -> data.serviceType.equals("slideshare"))
                .map(data -> data.extraData.get("embed_media_url"))
                .filter(cache::add)
                .flatMap(link -> slideShareParser.parse(link, ZonedDateTime.now()).stream())
                .forEach(list::add);

        // SpeakerDeck
        doc.getElementsByTag("div").select("[data-obj]")
                .stream()
                .map(e -> Data.readValue(e.attr("data-obj")))
                .filter(data -> data.serviceType.equals("speaker_deck"))
                .map(data -> "https:" + data.extraData.get("embed_media_url"))
                .filter(cache::add)
                .flatMap(link -> speakerDeckParser.parse(link, ZonedDateTime.now()).stream())
                .forEach(list::add);

        // GoogleSlide
        doc.getElementsByTag("a").stream()
                .map(link -> link.attr("href"))
                .filter(link -> link.startsWith("https://docs.google.com/presentation/"))
                .distinct()
                .filter(cache::add)
                .flatMap(link -> googleSlideParser.parse(link, ZonedDateTime.now()).stream())
                .forEach(list::add);

        // Docswell
        doc.getElementsByTag("a").stream()
                .map(link -> link.attr("href"))
                .filter(link -> link.startsWith("https://www.docswell.com/"))
                .distinct()
                .filter(cache::add)
                .flatMap(link -> docswellParser.parse(link, ZonedDateTime.now()).stream())
                .forEach(list::add);

        return list;
    }

    private record Data(
            int id,
            String name,
            String description,
            @JsonProperty("service_type") String serviceType,
            @JsonProperty("extra_data") Map<String, String> extraData
    ) {
        private static Data readValue(String json) {
            try {
                return MAPPER.readValue(json, Data.class);
            } catch (JsonProcessingException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    }
}
