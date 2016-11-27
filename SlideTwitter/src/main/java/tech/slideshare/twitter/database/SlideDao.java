package tech.slideshare.twitter.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SlideDao extends AbstractDao {

    public SlideDao(Connection con) {
        super(con);
    }

    public SlideDto getOne() throws SQLException {
        String sql = "SELECT " +
                "  s.slide_id" +
                "  , s.title" +
                "  , s.url " +
                "FROM " +
                "  slide s " +
                "  INNER JOIN tweet_queue tq " +
                "  USING (slide_id) " +
                "ORDER BY " +
                "  tq.date ASC " +
                "LIMIT 1";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();

                SlideDto dto = new SlideDto();
                dto.setSlideId(rs.getInt("slide_id"));
                dto.setTitle(rs.getString("title"));
                dto.setUrl(rs.getString("url"));

                return dto;
            }
        }
    }
}
