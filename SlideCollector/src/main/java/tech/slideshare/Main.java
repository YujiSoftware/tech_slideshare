package tech.slideshare;

import jakarta.xml.bind.JAXBException;
import name.falgout.jeffrey.throwing.stream.ThrowingStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.slideshare.collector.*;
import tech.slideshare.database.SlideDao;
import tech.slideshare.database.TweetQueueDao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static final SlideCollector[] SLIDE_COLLECTOR_LIST =
            new SlideCollector[]{
                    new SlideShareCollector(),
                    new SpeakerDeckCollector(),
                    new GoogleSlideCollector(),
                    new Backpaper0Collector()
            };

    public static void main(String[] args) {
        String user = args[0];
        String password = args[1];

        logger.info("Start {}", Main.class);

        int exitCode = 0;
        try {
            run(user, password);
        } catch (Throwable e) {
            logger.error("Collect failed!", e);
            exitCode = 1;
        }

        logger.info("End {}", Main.class);

        System.exit(exitCode);
    }

    private static void run(String user, String password) throws SQLException, JAXBException, IOException {
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

                        logger.debug("Enqueue: {}, {}", s.getTitle(), s.getLink());

                        int slideId = slideDao.insert(s.getTitle(), s.getLink(), s.getDate(), s.getAuthor(), s.getTwitter());
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
