package tech.slideshare.database;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class SlideDao extends AbstractDao {

    public SlideDao(Connection con) {
        super(con);
    }

    public int insert(String title, String url, String author, String twitter, String hashTag, String description, String image) throws SQLException {
        String sql = "INSERT INTO slide (title, url, date, author, twitter, hash_tag, description, image) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, title);
            statement.setString(2, url);
            statement.setTimestamp(3, Timestamp.from(Instant.now()));
            statement.setString(4, author);
            statement.setString(5, twitter);
            statement.setString(6, hashTag);
            statement.setString(7, description);
            statement.setString(8, image);

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

    public List<SlideDto> getLatest(int limit) throws SQLException {
        String sql = "SELECT slide_id, title, url, date, author, twitter, hash_tag, description, image FROM slide ORDER BY date DESC LIMIT ?";
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setInt(1, limit);

            statement.execute();

            List<SlideDto> list = new ArrayList<>(limit);
            try (ResultSet rs = statement.getResultSet()) {
                while (rs.next()) {
                    SlideDto slideDto = new SlideDto();
                    slideDto.slideId = rs.getInt(1);
                    slideDto.title = rs.getString(2);
                    slideDto.url = rs.getString(3);
                    slideDto.date = rs.getDate(4);
                    slideDto.author = rs.getString(5);
                    slideDto.twitter = rs.getString(6);
                    slideDto.hashTag = rs.getString(7);
                    slideDto.description = rs.getString(8);
                    slideDto.image = rs.getString(9);

                    list.add(slideDto);
                }
            }

            return list;
        }
    }
}
