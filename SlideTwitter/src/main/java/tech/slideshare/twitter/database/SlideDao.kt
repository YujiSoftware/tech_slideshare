package tech.slideshare.twitter.database

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

class SlideDao(con: Connection) : AbstractDao(con) {

    fun dequeue(): SlideDto? {
        val sql = "SELECT " +
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
                "LIMIT 1"

        var pstmt: PreparedStatement? = null
        var rs: ResultSet? = null
        try {
            pstmt = con.prepareStatement(sql)
            rs = pstmt.executeQuery()

            if (rs.next()) {
                val dto = SlideDto (
                        rs.getInt("slide_id"),
                        rs.getString("title"),
                        rs.getString("url"),
                        rs.getDate("date")
                );

                TweetQueueDao (con).delete(dto.slideId)

                return dto
            } else {
                return null
            }
        } finally {
            pstmt?.close()
            rs?.close()
        }
    }
}
