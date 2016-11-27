package tech.slideshare.twitter;

import tech.slideshare.twitter.database.SlideDao;
import tech.slideshare.twitter.database.SlideDto;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {
    public static void main(String... args) throws SQLException {
        String user = args[0];
        String password = args[1];

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/tech_slideshare", user, password)) {
            con.setAutoCommit(false);

            SlideDao slideDao = new SlideDao(con);
            SlideDto slideDto = slideDao.getOne();

            System.out.println(slideDto.getTitle());
        }
    }
}
