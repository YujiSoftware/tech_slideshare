package tech.slideshare.twitter

import org.slf4j.LoggerFactory
import tech.slideshare.twitter.database.SlideDao
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import java.sql.DriverManager
import kotlin.system.exitProcess

object Main {

    private const val ZERO_WIDTH_SPACE = "\u200B"

    private val logger = LoggerFactory.getLogger(Main.javaClass)

    @JvmStatic
    fun main(args: Array<String>) {
        val user = args[0]
        val password = args[1]

        logger.info("Start {}", Main.javaClass.toString())

        var exitCode = 0
        try {
            run(user, password)
        } catch (e: Throwable) {
            logger.error("Tweet failed!", e)
            exitCode = 1
        }

        logger.info("End {}", Main.javaClass.toString())

        exitProcess(exitCode)
    }

    private fun run(user: String, password: String) {
        DriverManager.getConnection("jdbc:mysql://localhost:3306/tech_slideshare", user, password).use { con ->
            con.autoCommit = false

            SlideDao(con).dequeue()?.let {
                try {
                    val twitter = TwitterFactory.getSingleton()

                    // タイトルにスパムURLが付与されている可能性があるため、
                    // ドットの後に不可視文字を入れてリンクにならないようにする。
                    var title: String = it.title.replace("\\.".toRegex(), "\\.$ZERO_WIDTH_SPACE")

                    val authors = mutableListOf<String>()
                    if (it.author != null) {
                        authors += it.author
                    }
                    if (it.twitter != null) {
                        authors += "@${ZERO_WIDTH_SPACE}${it.twitter}"
                    }
                    if (authors.size > 0) {
                        val author = " (" + authors.joinToString() + ")"

                        // Twitter の文字数上限 140 文字で、URL が 23 文字分を使うので、
                        // 残り 117 文字まで使える
                        if (title.length > 117 - author.length) {
                            title = title.substring(0, 117 - author.length - 1)
                            title += "…"
                        }

                        title += author
                    }

                    val status = twitter.updateStatus(title + "\r\n" + it.url)

                    logger.info("Successfully updated the status to [{}]. [slide_id={}]", status.text, it.slideId)

                    con.commit()
                } catch (e: TwitterException) {
                    if (e.statusCode < 500) {
                        logger.error("Unsuccessfully updated the status. Skipped. [slide_id={}]", it.slideId, e)
                        con.commit()
                    } else {
                        throw e
                    }
                }
            }
        }
    }
}
