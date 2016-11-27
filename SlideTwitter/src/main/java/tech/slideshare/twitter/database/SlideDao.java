package tech.slideshare.twitter.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class SlideDao extends AbstractDao {

    public SlideDao(Connection con) {
        super(con);
    }

    public SlideDto getOne() throws SQLException {
        PreparedStatement pstmt =
                con.prepareStatement(
                        "SELECT \n" +
                                "  title, url \n" +
                                "FROM \n" +
                                "  slide s \n" +
                                "  INNER JOIN tweet_queue tq \n" +
                                "  USING (slide_id) \n" +
                                "ORDER BY \n" +
                                "  tq.date ASC \n" +
                                "LIMIT 1");

        ResultSet rs = pstmt.executeQuery();

        SlideDto dto = new SlideDto();
        rs.next();
        dto.setTitle(rs.getString("title"));
        dto.setUrl(rs.getString("url"));

        return dto;
    }
}
