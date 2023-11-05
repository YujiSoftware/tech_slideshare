package tech.slideshare.crawler.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ContentDao extends AbstractDao {

    public ContentDao(Connection con) {
        super(con);
    }

    public void insert(int slidId, int page, String content) throws SQLException {
        String sql = "INSERT INTO content (slide_id, page, content) VALUES (?, ?, ?)";
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setInt(1, slidId);
            statement.setInt(2, page);
            statement.setString(3, content);
            statement.executeUpdate();
        }
    }
}
