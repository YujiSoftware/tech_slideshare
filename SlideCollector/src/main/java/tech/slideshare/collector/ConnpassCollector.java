package tech.slideshare.collector;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import tech.slideshare.connpass.Connpass;
import tech.slideshare.parser.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class ConnpassCollector implements SlideCollector {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Parser docswellParser = new DocswellParser();

    private final Parser googleSlideParser = new GoogleSlideParser();

    private final Parser slideShareParser = new SlideShareParser();

    private final Parser speakerDeckParser = new SpeakerDeckParser();

    @Override
    public Stream<Slide> collect() throws IOException {
        Stream<Slide> slides = Stream.empty();

        List<Connpass.Event> events = Connpass.getEvents();
        for (Connpass.Event event : events) {
            slides = Stream.concat(slides, collectSlide(event.eventUrl()));
        }

        return slides;
    }

    protected Stream<Slide> collectSlide(String eventUrl) throws IOException {
        String presentation = eventUrl + "/presentation/";
        Document doc = Jsoup.connect(presentation).get();

        Stream<Slide> slideShare =
                doc.getElementsByTag("div").select("[data-obj]")
                        .stream()
                        .map(e -> Data.readValue(e.attr("data-obj")))
                        .filter(data -> data.serviceType.equals("slideshare"))
                        .map(data -> data.extraData.get("embed_media_url"))
                        .map(link -> slideShareParser.parse(link, ZonedDateTime.now()).orElse(null))
                        .filter(Objects::nonNull);

        Stream<Slide> speakerDeck =
                doc.getElementsByTag("div").select("[data-obj]")
                        .stream()
                        .map(e -> Data.readValue(e.attr("data-obj")))
                        .filter(data -> data.serviceType.equals("speaker_deck"))
                        .map(data -> "https:" + data.extraData.get("embed_media_url"))
                        .map(link -> speakerDeckParser.parse(link, ZonedDateTime.now()).orElse(null))
                        .filter(Objects::nonNull);

        Stream<Slide> googleSlide =
                doc.getElementsByTag("a").stream()
                        .map(link -> link.attr("href"))
                        .filter(link -> link.startsWith("https://docs.google.com/presentation/"))
                        .distinct()
                        .map(link -> googleSlideParser.parse(link, ZonedDateTime.now()).orElse(null))
                        .filter(Objects::nonNull);

        Stream<Slide> docswell =
                doc.getElementsByTag("a").stream()
                        .map(link -> link.attr("href"))
                        .filter(link -> link.startsWith("https://www.docswell.com/"))
                        .distinct()
                        .map(link -> docswellParser.parse(link, ZonedDateTime.now()).orElse(null))
                        .filter(Objects::nonNull);

        return Stream.concat(Stream.concat(Stream.concat(slideShare, speakerDeck), googleSlide), docswell);
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
