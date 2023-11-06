package tech.slideshare.crawler.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SlideDao extends AbstractDao {

    public SlideDao(Connection con) {
        super(con);
    }

    public SlideDto getUncrawlled(String url) throws SQLException {
        String sql = "SELECT slide_id, title, url, date, author, twitter, description, image FROM slide WHERE crawled_flag = false AND url LIKE ? ORDER BY slide_id DESC LIMIT 1";
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, url + "%");
            statement.execute();

            try (ResultSet rs = statement.getResultSet()) {
                if (rs.next()) {
                    SlideDto slideDto = new SlideDto();
                    slideDto.slideId = rs.getInt(1);
                    slideDto.title = rs.getString(2);
                    slideDto.url = rs.getString(3);
                    slideDto.date = rs.getDate(4);
                    slideDto.author = rs.getString(5);
                    slideDto.twitter = rs.getString(6);
                    slideDto.description = rs.getString(7);
                    slideDto.image = rs.getString(8);

                    return slideDto;
                }
            }

            return null;
        }
    }

    public void setCrawledFlag(int slideId) throws SQLException {
        String sql = "UPDATE slide SET crawled_flag = TRUE WHERE slide_id = ?";
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setInt(1, slideId);
            statement.executeUpdate();
        }
    }
}
