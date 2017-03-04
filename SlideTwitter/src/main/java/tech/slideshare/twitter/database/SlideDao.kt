package tech.slideshare.twitter.database

import kotlinx.support.jdk7.use
import java.sql.Connection

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

        con.prepareStatement(sql).use {
            it.executeQuery().use {

                if (it.next()) {
                    val dto = SlideDto(
                            it.getInt("slide_id"),
                            it.getString("title"),
                            it.getString("url"),
                            it.getDate("date")
                    );

                    TweetQueueDao(con).delete(dto.slideId)

                    return dto
                } else {
                    return null
                }
            }
        }
    }
}
