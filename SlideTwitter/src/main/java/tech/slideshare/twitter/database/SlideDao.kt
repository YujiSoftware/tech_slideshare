package tech.slideshare.twitter.database

import java.sql.Connection

class SlideDao(con: Connection) : AbstractDao(con) {

    fun dequeue(): SlideDto? {
        val sql = "SELECT " +
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
                "LIMIT 1"

        con.prepareStatement(sql).use { it ->
            it.executeQuery().use {
                return if (it.next()) {
                    val dto = SlideDto(
                        it.getInt("slide_id"),
                        it.getString("title"),
                        it.getString("url"),
                        it.getDate("date"),
                        it.getString("author"),
                        it.getString("twitter"),
                        it.getString("hash_tag"),
                    )

                    TweetQueueDao(con).delete(dto.slideId)

                    dto
                } else {
                    null
                }
            }
        }
    }
}
