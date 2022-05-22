package tech.slideshare;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import name.falgout.jeffrey.throwing.stream.ThrowingStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.slideshare.collector.*;
import tech.slideshare.database.SlideDao;
import tech.slideshare.database.SlideDto;
import tech.slideshare.database.TweetQueueDao;
import tech.slideshare.rss.Channel;
import tech.slideshare.rss.Item;
import tech.slideshare.rss.Rss;

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

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static final int RSS_ITEMS = 20;

    private static final SlideCollector[] SLIDE_COLLECTOR_LIST =
            new SlideCollector[]{
                    new SlideShareCollector(),
                    new SpeakerDeckCollector(),
                    new GoogleSlideCollector(),
                    new Backpaper0Collector(),
                    new DocswellCollector()
            };

    public static void main(String[] args) {
        String user = args[0];
        String password = args[1];

        logger.info("Start {}", Main.class);

        int exitCode = 0;
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/tech_slideshare", user, password)) {
            con.setAutoCommit(false);

            boolean updated = collect(con);
            if (updated) {
                Rss rss = createRSS(con);
                publishRss(rss);
            }
        } catch (Throwable e) {
            logger.error("Collect failed!", e);
            exitCode = 1;
        }

        logger.info("End {}", Main.class);

        System.exit(exitCode);
    }

    private static boolean collect(Connection con) throws SQLException, JAXBException, IOException {
        SlideDao slideDao = new SlideDao(con);
        TweetQueueDao tweetQueueDao = new TweetQueueDao(con);
        boolean updated = false;

        for (SlideCollector collector : SLIDE_COLLECTOR_LIST) {
            logger.info("Got hatena bookmark. [collector={}]", collector.getClass().getCanonicalName());

            long count = ThrowingStream.of(collector.collect(), SQLException.class)
                    .map(s -> {
                        try {
                            if (slideDao.exists(s.getLink())) {
                                return false;
                            }

                            logger.debug("Enqueue: {}, {}", s.getTitle(), s.getLink());

                            int slideId = slideDao.insert(s.getTitle(), s.getLink(), s.getDate(), s.getAuthor(), s.getTwitter());
                            tweetQueueDao.insert(slideId);

                            con.commit();

                            return true;
                        } catch (SQLException e) {
                            logger.error("Enqueue failed. [title={}, link={}]", s.getTitle(), s.getLink(), e);
                            con.rollback();

                            return false;
                        }
                    })
                    .normalFilter(b -> b)
                    .count();

            if (count > 0) {
                updated = true;
            }
        }

        return updated;
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
}
