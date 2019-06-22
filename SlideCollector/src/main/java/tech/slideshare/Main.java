package tech.slideshare;

import name.falgout.jeffrey.throwing.stream.ThrowingStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.slideshare.collector.Backpaper0Collector;
import tech.slideshare.collector.SlideCollector;
import tech.slideshare.collector.SlideShareCollector;
import tech.slideshare.collector.SpeakerDeckCollector;
import tech.slideshare.database.SlideDao;
import tech.slideshare.database.TweetQueueDao;

import javax.xml.bind.JAXBException;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {

    private static Logger logger = LoggerFactory.getLogger(Main.class);

    private static final SlideCollector[] SLIDE_COLLECTOR_LIST =
            new SlideCollector[]{
                    new SlideShareCollector(),
                    new SpeakerDeckCollector(),
                    new Backpaper0Collector()
            };

    public static void main(String[] args) {
        String user = args[0];
        String password = args[1];

        logger.info("Start {}", Main.class.toString());

        int exitCode = 0;
        try {
            run(user, password);
        } catch (Throwable e) {
            logger.error("Collect failed!", e);
            exitCode = 1;
        }

        logger.info("End {}", Main.class.toString());

        System.exit(exitCode);
    }

    private static void run(String user, String password) throws SQLException, JAXBException, MalformedURLException {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/tech_slideshare", user, password)) {
            con.setAutoCommit(false);

            SlideDao slideDao = new SlideDao(con);
            TweetQueueDao tweetQueueDao = new TweetQueueDao(con);

            for (SlideCollector collector : SLIDE_COLLECTOR_LIST) {
                logger.info("Got hatena bookmark. [collector={}]", collector.getClass().getCanonicalName());

                ThrowingStream.of(collector.collect(), SQLException.class).forEach(s -> {
                    try {
                        if (slideDao.exists(s.getLink())) {
                            return;
                        }

                        String title = s.getTitle();
                        if (s.getAuthor().isPresent()) {
                            title += " (" + s.getAuthor().get() + ")";
                        }
                        logger.debug("Enqueue: {}, {}", title, s.getLink());

                        int slideId = slideDao.insert(title, s.getLink(), s.getDate());
                        tweetQueueDao.insert(slideId);

                        con.commit();
                    } catch (SQLException e) {
                        logger.error("Enqueue failed. [title={}, link={}]", s.getTitle(), s.getLink(), e);
                        con.rollback();
                    }
                });
            }
        }
    }

}
