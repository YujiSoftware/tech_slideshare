package tech.slideshare.twitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.slideshare.twitter.database.SlideDao;
import tech.slideshare.twitter.database.SlideDto;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {

    private static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String... args) throws SQLException, TwitterException {
        String user = args[0];
        String password = args[1];

        logger.info("Start {}", Main.class.toString());

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/tech_slideshare", user, password)) {
            con.setAutoCommit(false);

            SlideDto dto = new SlideDao(con).dequeue();
            Twitter twitter = TwitterFactory.getSingleton();
            Status status = twitter.updateStatus(dto.getTitle() + "\r\n" + dto.getUrl());

            logger.info("Successfully updated the status to [{}].", status.getText());

            con.commit();
        }

        logger.info("End {}", Main.class.toString());
    }
}
