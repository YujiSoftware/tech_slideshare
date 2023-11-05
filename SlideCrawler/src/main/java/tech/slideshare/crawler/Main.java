package tech.slideshare.crawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.slideshare.crawler.database.ContentDao;
import tech.slideshare.crawler.database.SlideDao;
import tech.slideshare.crawler.database.SlideDto;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static final Crawler[] CRAWLERS = new Crawler[]{
            new SlideShareCrawler(),
            new DocswellCrawler(),
            new SpeakerDeckCrawler(),
    };

    public static void main(String[] args) {
        String user = args[0];
        String password = args[1];

        logger.info("Start {}", Main.class);

        int exitCode = 0;
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/tech_slideshare", user, password)) {
            con.setAutoCommit(false);

            for (Crawler crawler : CRAWLERS) {
                crawl(con, crawler);
            }
        } catch (Throwable e) {
            logger.error("Collect failed!", e);
            exitCode = 1;
        }

        logger.info("End {}", Main.class);

        System.exit(exitCode);
    }

    public static void crawl(Connection con, Crawler crawler) throws SQLException, IOException {
        SlideDao slideDao = new SlideDao(con);
        ContentDao content = new ContentDao(con);

        SlideDto slide = slideDao.getUncrawlled(crawler.getURL());
        if (slide == null) {
            return;
        }

        logger.info("Crawl: name=" + crawler.getClass().getSimpleName() + ", id=" + slide.slideId + ", url=" + slide.url);

        List<String> crawled = crawler.crawl(slide.url);

        int page = 1;
        for (String c : crawled) {
            content.insert(slide.slideId, page, c);
            page++;
        }
        slideDao.setCrawledFlag(slide.slideId);

        con.commit();
    }
}