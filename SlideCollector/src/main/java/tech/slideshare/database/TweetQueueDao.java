package tech.slideshare.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TweetQueueDao extends AbstractDao {

    public TweetQueueDao(Connection con) {
        super(con);
    }

    public void insert(int slideId) throws SQLException {
        String sql = "INSERT INTO tweet_queue (slide_id) VALUES (?)";

        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setInt(1, slideId);

            statement.executeUpdate();
        }
    }
}
