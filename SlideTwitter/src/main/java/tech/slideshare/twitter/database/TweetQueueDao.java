package tech.slideshare.twitter.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TweetQueueDao extends AbstractDao {

    public TweetQueueDao(Connection con) {
        super(con);
    }

    public boolean delete(int slideId) throws SQLException {
        String sql = "DELETE FROM tweet_queue WHERE slide_id = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, slideId);

            return pstmt.executeUpdate() > 0;
        }
    }
}
