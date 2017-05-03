package tech.slideshare.collector;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.slideshare.collector.database.SlideDao;
import tech.slideshare.collector.rss.Rss;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {

    private static Logger logger = LoggerFactory.getLogger(Main.class);

    private static final String[] HATENA_BOOKMARK_LIST =
            new String[]{
                    "http://b.hatena.ne.jp/entrylist?url=http%3A%2F%2Fwww.slideshare.net%2F&mode=rss",
                    "http://b.hatena.ne.jp/entrylist?url=http%3A%2F%2Fspeakerdeck.com%2F&mode=rss",
                    "http://b.hatena.ne.jp/entrylist?url=http%3A%2F%2Fdocs.com%2F&mode=rss",
            };

    public static void main(String[] args) throws JAXBException, MalformedURLException, SQLException, ParseException {
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

            JAXBContext context = JAXBContext.newInstance(Rss.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");

            for (String url : HATENA_BOOKMARK_LIST) {
                Rss r = (Rss) unmarshaller.unmarshal(new URL(url));

                logger.info("Got hatena bookmark. [url={}, length={}]", url, r.items.size());

                r.items.stream()
                        .filter(i -> i.subject != null && i.subject.equals("テクノロジー"))
                        .filter(i -> !i.link.contains("://www.slideshare.net/slideshow/embed_code/"))
                        .forEach(item -> {
                            try {
                                String title = item.title.replace(" // Speaker Deck", "");
                                String link = item.link.replace("http://www.slideshare.net/", "https://www.slideshare.net/");
                                Date date = format.parse(item.date);

                                if (slideDao.tryEnqueue(title, link, date)) {
                                    String author = getAuthor(link);
                                    if (author != null) {
                                        slideDao.updateTitle(
                                                link,
                                                String.format("%s (%s)", title, author));
                                    }
                                    logger.debug("Enqueue: {}, {}", title, link);
                                }
                            } catch (ParseException | SQLException | IOException | URISyntaxException e) {
                                throw new RuntimeException(e);
                            }
                        });

                con.commit();
            }
        }
    }

    private static String getAuthor(String link) throws IOException, URISyntaxException {
        URL url = new URL(link);
        switch (url.getHost()) {
            case "www.slideshare.net":
                return getAuthorFromSlideshare(link);
            case "speakerdeck.com":
                return getAuthorFromSpeakerDeck(link);
        }

        return null;
    }

    private static String getAuthorFromSlideshare(String link) throws IOException, URISyntaxException {
        Document doc = Jsoup.connect(new URI(link + "/../").normalize().toASCIIString()).get();

        for (Element metaTag : doc.getElementsByTag("meta")) {
            String property = metaTag.attr("property");
            String content = metaTag.attr("content");

            if ("slideshare:name".equals(property)) {
                String twitter = doc.select("div.profile-social-links > a.twitter").attr("href");
                if (!twitter.equals("")) {
                    return content + ", @" + twitter.substring(twitter.lastIndexOf('/') + 1);
                } else {
                    return content;
                }
            }
        }

        return null;
    }

    private static String getAuthorFromSpeakerDeck(String link) throws IOException, URISyntaxException {
        Document doc = Jsoup.connect(new URI(link + "/../").normalize().toASCIIString()).get();
        Elements h2 = doc.getElementsByTag("h2");

        return (h2.size() > 0) ? h2.get(0).text() : null;
    }
}
