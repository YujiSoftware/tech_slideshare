package tech.slideshare.twitter.database;

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
                "FROM " +
                "  slide s " +
                "  INNER JOIN tweet_queue tq " +
                "  USING (slide_id) " +
                "ORDER BY " +
                "  s.date ASC " +
                "LIMIT 1";
        try (PreparedStatement pstmt = getCon().prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    SlideDto dto = new SlideDto(
                            rs.getInt("slide_id"),
                            rs.getString("title"),
                            rs.getString("url"),
                            rs.getDate("date")
                    );

                    new TweetQueueDao(getCon()).delete(dto.getSlideId());

                    return dto;
                } else {
                    return null;
                }
            }
        }
    }
}
