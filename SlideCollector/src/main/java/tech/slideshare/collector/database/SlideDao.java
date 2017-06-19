package tech.slideshare.collector.database;

import java.sql.*;
import java.util.Date;

public class SlideDao extends AbstractDao {

    public SlideDao(Connection con) {
        super(con);
    }

    public boolean tryEnqueue(String title, String url, Date date) throws SQLException {
        String slideSql = "INSERT INTO slide (title, url, date) \n" +
                "SELECT \n" +
                "  ?, ?, ? \n" +
                "WHERE \n" +
                "NOT EXISTS ( \n" +
                "  SELECT url FROM slide WHERE url = ? \n" +
                ")";
        try (PreparedStatement pstmt = con.prepareStatement(slideSql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, url);
            pstmt.setTimestamp(3, new Timestamp(date.getTime()));
            pstmt.setString(4, url);

            if (pstmt.executeUpdate() == 0) {
                return false;
            }
        }

        String queueSql =
                "INSERT INTO tweet_queue (slide_id) VALUES (LAST_INSERT_ID())";
        try (PreparedStatement pstmt = con.prepareStatement(queueSql)) {
            return pstmt.executeUpdate() > 0;
        }
    }

    public void updateTitle(String url, String title) throws SQLException {
        String sql = "UPDATE slide SET title = ? WHERE url = ?";
        try(PreparedStatement pstmt = con.prepareStatement(sql)){
            pstmt.setString(1, title);
            pstmt.setString(2, url);

            pstmt.executeUpdate();
        }
    }

    public boolean exists(String url) throws SQLException {
        String sql = "SELECT EXISTS (SELECT * FROM slide WHERE url = ?)";
        try(PreparedStatement pstmt = con.prepareStatement(sql)){
            pstmt.setString(1, url);

            if(pstmt.execute()){
                try(ResultSet rs = pstmt.getResultSet()){
                    return rs.getBoolean(1);
                }
            }else{
                return false;
            }
        }
    }
}
