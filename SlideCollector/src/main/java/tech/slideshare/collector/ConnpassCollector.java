package tech.slideshare.collector;

import org.jsoup.HttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.slideshare.cache.Cache;
import tech.slideshare.cache.TempFileCache;
import tech.slideshare.connpass.Connpass;
import tech.slideshare.parser.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ConnpassCollector implements SlideCollector {

    private static final Logger logger = LoggerFactory.getLogger(ConnpassCollector.class);

    private final Parser docswellParser = new DocswellParser();

    private final Parser googleSlideParser = new GoogleSlideParser();

    private final Parser slideShareParser = new SlideShareParser();

    private final Parser speakerDeckParser = new SpeakerDeckParser();

    @Override
    public List<Slide> collect() throws IOException, InterruptedException {
        Cache cache = new TempFileCache(Connpass.class.getSimpleName());

        List<Slide> list = new ArrayList<>();
        LocalDate date = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            for (Connpass.Events.Event event : Connpass.Events.get(date)) {
                String hashTag = event.hashTag();
                if (hashTag != null && hashTag.isEmpty()) {
                    hashTag = null;
                }

                List<Slide> found = collectSlide(cache, event.id(), hashTag);
                list.addAll(found);

                logger.debug("Event id: {}, found: {}", event.id(), found.size());
            }
            date.minusDays(1);
        }

        cache.flush();

        return list;
    }

    protected List<Slide> collectSlide(Cache cache, int eventId, String hashTag) throws IOException, InterruptedException {
        List<Slide> list = new ArrayList<>();
        List<Connpass.Presentations.Presentation> presentations;

        try {
            presentations = Connpass.Presentations.get(eventId);
        } catch (HttpStatusException e) {
            // なぜかたまに削除されたページが含まれていることがある。その場合、無視する。
            if (e.getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                logger.warn(e.toString());
                return list;
            }
            throw e;
        }

        for (Connpass.Presentations.Presentation presentation : presentations) {
            String url = presentation.url();
            if (!cache.add(url)) {
                continue;
            }

            Parser parser = switch (URI.create(url).getHost()) {
                case "www.slideshare.net" -> slideShareParser;
                case "speakerdeck.com" -> speakerDeckParser;
                case "docs.google.com" -> googleSlideParser;
                case "www.docswell.com" -> docswellParser;
                default -> null;
            };

            if (parser == null) {
                // 把握していないスライド共有サービスの場合、今後の対応のためにログを出力する
                if (presentation.presentationType() == Connpass.Presentations.PresentationType.SLIDE) {
                    logger.warn("Unsupported slide parser: {}", url);
                }
                continue;
            }

            parser.parse(url).ifPresent(s -> {
                s.setHashTag(hashTag);
                list.add(s);
            });
        }

        return list;
    }

    @Override
    public String name() {
        return this.getClass().getSimpleName();
    }
}
