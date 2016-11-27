package tech.slideshare.twitter;

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
    public static void main(String... args) throws SQLException, TwitterException {
        String user = args[0];
        String password = args[1];

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/tech_slideshare", user, password)) {
            con.setAutoCommit(false);

            SlideDto dto = new SlideDao(con).dequeue();

            Twitter twitter = TwitterFactory.getSingleton();
            Status status = twitter.updateStatus(dto.getTitle() + "\r\n" + dto.getUrl());

            System.out.println("Successfully updated the status to [" + status.getText() + "].");

            con.commit();
        }
    }
}
