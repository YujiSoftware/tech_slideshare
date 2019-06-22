package tech.slideshare.database;

import java.sql.*;
import java.time.ZonedDateTime;

public class SlideDao extends AbstractDao {

    public SlideDao(Connection con) {
        super(con);
    }

    public int insert(String title, String url, ZonedDateTime date) throws SQLException {
        String sql = "INSERT INTO slide (title, url, date) VALUES (?, ?, ?)";

        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, title);
            statement.setString(2, url);
            statement.setTimestamp(3, Timestamp.valueOf(date.toLocalDateTime()));

            statement.executeUpdate();
        }

        try (PreparedStatement statement = con.prepareStatement("SELECT LAST_INSERT_ID()")) {
            statement.execute();
            try (ResultSet rs = statement.getResultSet()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public boolean exists(String url) throws SQLException {
        String sql = "SELECT EXISTS (SELECT * FROM slide WHERE url = ?)";
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, url);

            statement.execute();
            try (ResultSet rs = statement.getResultSet()) {
                rs.next();
                return rs.getBoolean(1);
            }
        }
    }
}
