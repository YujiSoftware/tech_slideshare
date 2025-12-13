package tech.slideshare;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.slideshare.collector.ConnpassCollector;
import tech.slideshare.collector.HatenaBookmarkCollector;
import tech.slideshare.collector.Slide;
import tech.slideshare.collector.SlideCollector;
import tech.slideshare.database.SlideDao;
import tech.slideshare.database.SlideDto;
import tech.slideshare.database.TweetQueueDao;
import tech.slideshare.json.Json;
import tech.slideshare.parser.*;
import tech.slideshare.rss.Channel;
import tech.slideshare.rss.HatenaBookmark;
import tech.slideshare.rss.Item;
import tech.slideshare.rss.Rss;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static final int RSS_ITEMS = 20;

    private static final int JSON_ITEMS = 50;

    private enum Target {
        HATENA(
                List.of(
                        new HatenaBookmarkCollector(
                                "SlideShare",
                                new SlideShareParser(),
                                new HatenaBookmark("https://b.hatena.ne.jp/entrylist?url=http%3A%2F%2Fwww.slideshare.net%2F&mode=rss")
                        ),
                        new HatenaBookmarkCollector(
                                "SpeakerDeck",
                                new SpeakerDeckParser(),
                                new HatenaBookmark("https://b.hatena.ne.jp/entrylist?url=http%3A%2F%2Fspeakerdeck.com%2F&mode=rss")
                        ),
                        new HatenaBookmarkCollector(
                                "Googleスライド",
                                new GoogleSlideParser(),
                                new HatenaBookmark("https://b.hatena.ne.jp/entrylist?url=docs.google.com/presentation&mode=rss")
                        ),
                        new HatenaBookmarkCollector(
                                "Backpaper0",
                                new Backpaper0Parser(),
                                new HatenaBookmark("https://b.hatena.ne.jp/entrylist?url=http%3A%2F%2Fbackpaper0.github.io%2Fghosts%2F&mode=rss")
                        ),
                        new HatenaBookmarkCollector(
                                "Docswell",
                                new DocswellParser(),
                                new HatenaBookmark("https://b.hatena.ne.jp/site/www.docswell.com/?mode=rss")
                        )
                )
        ),
        CONNPASS(
                List.of(
                        new ConnpassCollector()
                )
        );

        final List<SlideCollector> collectors;

        Target(List<SlideCollector> collectors) {
            this.collectors = collectors;
        }
    }

    public static void main(String[] args) {
        String user = args[0];
        String password = args[1];
        Target target = Target.valueOf(args[2]);

        logger.info("Start {}, target: {}", Main.class, target.name());

        int exitCode = 0;
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/tech_slideshare", user, password)) {
            con.setAutoCommit(false);

            collect(con, target);

            Rss rss = createRSS(con);
            publishRss(rss);
            Json json = createJson(con);
            publishJson(json);
        } catch (Throwable e) {
            logger.error("Collect failed!", e);
            exitCode = 1;
        }

        logger.info("End {}", Main.class);

        System.exit(exitCode);
    }

    private static void collect(Connection con, Target target) throws SQLException, JAXBException, IOException, InterruptedException {
        SlideDao slideDao = new SlideDao(con);
        TweetQueueDao tweetQueueDao = new TweetQueueDao(con);

        for (SlideCollector collector : target.collectors) {
            logger.info("Start: {}", collector.name());

            for (Slide s : collector.collect()) {
                try {
                    if (slideDao.exists(s.getLink())) {
                        continue;
                    }

                    logger.debug("Enqueue: {}, {}", s.getTitle(), s.getLink());

                    int slideId = slideDao.insert(s.getTitle(), s.getLink(), s.getAuthor(), s.getTwitter(), s.getDescription(), s.getImage());
                    tweetQueueDao.insert(slideId);

                    con.commit();
                } catch (SQLException e) {
                    logger.error("Enqueue failed. [title={}, link={}]", s.getTitle(), s.getLink(), e);
                    con.rollback();
                }
            }
        }
    }

    private static Rss createRSS(Connection con) throws SQLException {
        Channel channel = new Channel();
        channel.title = "勉強会スライドBot";
        channel.link = "https://yuji.software/tech_slideshare";
        channel.description = "インターネット上に公開された技術系勉強会のスライドをツイートするbotです。";

        ArrayList<Item> items = new ArrayList<>(RSS_ITEMS);
        for (SlideDto slide : new SlideDao(con).getLatest(RSS_ITEMS)) {
            Item item = new Item();
            item.title = slide.title;
            item.link = slide.url;
            item.date = Item.RSS_DATE_FORMATTER.format(Instant.ofEpochMilli(slide.date.getTime()));

            ArrayList<String> authors = new ArrayList<>();
            if (slide.author != null) {
                authors.add(slide.author);
            }
            if (slide.twitter != null) {
                authors.add("@" + slide.twitter);
            }
            if (!authors.isEmpty()) {
                item.title += " (" + String.join(",", authors) + ")";
            }

            items.add(item);
        }

        Rss rss = new Rss();
        rss.channel = channel;
        rss.items = items;

        return rss;
    }

    private static void publishRss(Rss rss) throws JAXBException, IOException {
        JAXBContext context = JAXBContext.newInstance(Rss.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.toString());
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        try (Writer writer = Files.newBufferedWriter(Path.of("feed.xml"), StandardCharsets.UTF_8)) {
            marshaller.marshal(rss, writer);
        }
    }

    private static Json createJson(Connection con) throws SQLException {
        ArrayList<Json.Item> items = new ArrayList<>(JSON_ITEMS);
        for (SlideDto slide : new SlideDao(con).getLatest(JSON_ITEMS)) {
            Json.Item item = new Json.Item();
            item.title = slide.title;
            item.author = slide.author;
            item.twitter = slide.twitter;
            item.description = slide.description;
            item.image = slide.image;
            item.link = slide.url;
            item.date = Item.RSS_DATE_FORMATTER.format(Instant.ofEpochMilli(slide.date.getTime()));

            items.add(item);
        }

        Json json = new Json();
        json.items = items;

        return json;
    }

    private static void publishJson(Json json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try (Writer writer = Files.newBufferedWriter(Path.of("feed.json"), StandardCharsets.UTF_8)) {
            mapper.writeValue(writer, json);
        }
    }
}
