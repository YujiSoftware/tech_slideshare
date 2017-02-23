package tech.slideshare.twitter.database

import java.sql.Connection
import java.sql.PreparedStatement

class TweetQueueDao(con: Connection) : AbstractDao(con) {

    fun delete(slideId: Int): Boolean {
        val sql = "DELETE FROM tweet_queue WHERE slide_id = ?";
        val pstmt: PreparedStatement
        try {
            pstmt = con.prepareStatement(sql)
            pstmt.setInt(1, slideId)

            pstmt.executeUpdate() > 0;
        } finally {
            pstmt?.close()
        }
    }
}
