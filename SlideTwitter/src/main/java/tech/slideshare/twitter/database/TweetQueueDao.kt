package tech.slideshare.twitter.database

import java.sql.Connection

class TweetQueueDao(con: Connection) : AbstractDao(con) {

    fun delete(slideId: Int): Boolean {
        val sql = "DELETE FROM tweet_queue WHERE slide_id = ?"

        con.prepareStatement(sql).use {
            it.setInt(1, slideId)

            return it.executeUpdate() > 0
        }
    }
}
