package tech.slideshare.twitter

import kotlinx.support.jdk7.use
import org.slf4j.LoggerFactory
import tech.slideshare.twitter.database.SlideDao
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import java.sql.DriverManager

object Main {

    private val logger = LoggerFactory.getLogger(Main.javaClass)

    @JvmStatic fun main(args: Array<String>) {
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

        System.exit(exitCode)
    }

    private fun run(user: String, password: String) {
        DriverManager.getConnection("jdbc:mysql://localhost:3306/tech_slideshare", user, password).use { con ->
            con.setAutoCommit(false)

            SlideDao(con).dequeue()?.let {
                try {
                    val twitter = TwitterFactory.getSingleton()
                    val status = twitter.updateStatus(it.title + "\r\n" + it.url)

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
