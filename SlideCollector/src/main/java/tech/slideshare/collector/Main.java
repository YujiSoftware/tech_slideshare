package tech.slideshare.collector;

import tech.slideshare.collector.database.SlideDao;
import tech.slideshare.collector.rss.Rss;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {

    private static final String[] HATENA_BOOKMARK_LIST =
            new String[]{
                    "http://b.hatena.ne.jp/entrylist?url=http%3A%2F%2Fwww.slideshare.net%2F&mode=rss",
                    "http://b.hatena.ne.jp/entrylist?url=http%3A%2F%2Fspeakerdeck.com%2F&mode=rss",
                    "http://b.hatena.ne.jp/entrylist?url=http%3A%2F%2Fdocs.com%2F&mode=rss",
            };

    public static void main(String[] args) throws JAXBException, MalformedURLException, SQLException, ParseException {
        String user = args[0];
        String password = args[1];

        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/tech_slideshare", user, password);
        con.setAutoCommit(false);

        SlideDao slideDao = new SlideDao(con);

        JAXBContext context = JAXBContext.newInstance(Rss.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");

        for (String url : HATENA_BOOKMARK_LIST) {
            Rss r = (Rss) unmarshaller.unmarshal(new URL(url));

            r.items.stream()
                    .filter(i -> i.subject.equals("テクノロジー"))
                    .forEach(item -> {
                        try {
                            Date date = format.parse(item.date);
                            if (slideDao.tryEnqueue(item.title, item.link, date)) {
                                System.out.println("Enqueue: " + item.title);
                            } else {
                                System.out.println("Already: " + item.title);
                            }
                        } catch (ParseException | SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });

            con.commit();
        }
    }
}
