package tech.slideshare.bluesky.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TweetQueueDao extends AbstractDao {

    public TweetQueueDao(Connection con) {
        super(con);
    }

    public boolean delete(int slideId) throws SQLException {
        String sql = "DELETE FROM tweet_queue WHERE slide_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, slideId);
            return ps.executeUpdate() > 0;
        }
    }
}

