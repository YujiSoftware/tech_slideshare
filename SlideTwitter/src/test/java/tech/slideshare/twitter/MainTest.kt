package tech.slideshare.twitter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import tech.slideshare.twitter.database.SlideDto
import java.sql.Date

class MainTest {
    @Test
    fun makeTweet_noAuthorNoTag() {
        val dto = SlideDto(
            slideId = 1,
            title = "Simple Title",
            url = "https://example.com/",
            date = Date.valueOf("2020-01-01"),
            author = null,
            twitter = null,
            hashTag = null
        )

        val expected = "Simple Title\r\nhttps://example.com/"
        val actual = Main.makeTweet(dto)
        assertEquals(expected, actual)
    }

    @Test
    fun makeTweet_withDottedTitle() {
        val dto = SlideDto(
            slideId = 1,
            title = "Title.with.dot",
            url = "https://example.com/",
            date = Date.valueOf("2020-01-01"),
            author = null,
            twitter = null,
            hashTag = null
        )

        val expected = "Title.\u200Bwith.\u200Bdot\r\nhttps://example.com/"
        val actual = Main.makeTweet(dto)
        assertEquals(expected, actual)
    }

    @Test
    fun makeTweet_withAuthor() {
        val dto = SlideDto(
            slideId = 1,
            title = "Short title",
            url = "https://example.com/",
            date = Date.valueOf("2020-01-02"),
            author = "Author Name",
            twitter = null,
            hashTag = null
        )

        val expected = "Short title (Author Name)\r\nhttps://example.com/"
        val actual = Main.makeTweet(dto)
        assertEquals(expected, actual)
    }

    @Test
    fun makeTweet_withTwitter() {
        val dto = SlideDto(
            slideId = 1,
            title = "Short title",
            url = "https://example.com/",
            date = Date.valueOf("2020-01-02"),
            author = null,
            twitter = "account",
            hashTag = null
        )

        val expected = "Short title (@\u200Baccount)\r\nhttps://example.com/"
        val actual = Main.makeTweet(dto)
        assertEquals(expected, actual)
    }

    @Test
    fun makeTweet_withAuthorAndTwitter() {
        val dto = SlideDto(
            slideId = 1,
            title = "Short title",
            url = "https://example.com/",
            date = Date.valueOf("2020-01-02"),
            author = "Author Name",
            twitter = "account",
            hashTag = null
        )

        val expected = "Short title (Author Name, @\u200Baccount)\r\nhttps://example.com/"
        val actual = Main.makeTweet(dto)
        assertEquals(expected, actual)
    }

    @Test
    fun makeTweet_withHashTag() {
        val dto = SlideDto(
            slideId = 1,
            title = "Short title",
            url = "https://example.com/",
            date = Date.valueOf("2020-01-02"),
            author = null,
            twitter = null,
            hashTag = "hashtag"
        )

        val expected = "Short title #hashtag\r\nhttps://example.com/"
        val actual = Main.makeTweet(dto)
        assertEquals(expected, actual)
    }

    @Test
    fun makeTweet_withAuthorAndTwitterAndHashTag() {
        val dto = SlideDto(
            slideId = 1,
            title = "Short title",
            url = "https://example.com/",
            date = Date.valueOf("2020-01-02"),
            author = "Author Name",
            twitter = "account",
            hashTag = "hashtag"
        )

        val expected = "Short title (Author Name, @\u200Baccount) #hashtag\r\nhttps://example.com/"
        val actual = Main.makeTweet(dto)
        assertEquals(expected, actual)
    }

    @Test
    fun makeTweet_truncation() {
        val longTitle = "x".repeat(120)
        val dto = SlideDto(
            slideId = 1,
            title = longTitle,
            url = "https://example.com/",
            date = Date.valueOf("2020-01-03"),
            author = "Author Name",
            twitter = null,
            hashTag = "hashtag"
        )

        val length = 117 - "… (Author Name) #hashtag".length
        val expected = "x".repeat(length) + "… (Author Name) #hashtag\r\nhttps://example.com/"
        val actual = Main.makeTweet(dto)
        assertEquals(expected, actual)
    }
}
