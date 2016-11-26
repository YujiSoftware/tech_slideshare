package tech.slideshare;

import tech.slideshare.database.SlideDao;
import tech.slideshare.rss.Item;
import tech.slideshare.rss.Rss;

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

    private static final String HATENA_BOOKMARK_SLIDESHARE =
            "http://b.hatena.ne.jp/entrylist?url=http%3A%2F%2Fwww.slideshare.net%2F&mode=rss";

    public static void main(String[] args) throws JAXBException, MalformedURLException, SQLException, ParseException {
        String user = args[0];
        String password = args[1];

        JAXBContext context = JAXBContext.newInstance(Rss.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        Rss r = (Rss) unmarshaller.unmarshal(new URL(HATENA_BOOKMARK_SLIDESHARE));

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");

        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/tech_slideshare", user, password);
        con.setAutoCommit(false);

        SlideDao slideDao = new SlideDao(con);

        for (Item i : r.items) {
            Date date = format.parse(i.date);
            if (slideDao.tryEnqueue(i.title, i.link, date)) {
                System.out.println("Enqueue: " + i.title);
            } else {
                System.out.println("Already: " + i.title);
            }
        }

        con.commit();
    }
}
