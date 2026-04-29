package tech.slideshare.bluesky.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SlideDao extends AbstractDao {

    public SlideDao(Connection con) {
        super(con);
    }

    public SlideDto dequeue() throws SQLException {
        String sql = "SELECT " +
                "  s.slide_id" +
                "  , s.title" +
                "  , s.url " +
                "  , s.date " +
                "  , s.author " +
                "  , s.twitter " +
                "  , s.hash_tag " +
                "FROM " +
                "  slide s " +
                "  INNER JOIN tweet_queue tq " +
                "  USING (slide_id) " +
                "ORDER BY " +
                "  s.date DESC " +
                "LIMIT 1";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    SlideDto dto = new SlideDto(
                            rs.getInt("slide_id"),
                            rs.getString("title"),
                            rs.getString("url"),
                            rs.getDate("date"),
                            rs.getString("author"),
                            rs.getString("twitter"),
                            rs.getString("hash_tag")
                    );

                    new TweetQueueDao(con).delete(dto.getSlideId());

                    return dto;
                }
            }
        }

        return null;
    }
}

