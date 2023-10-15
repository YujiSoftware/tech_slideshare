package tech.slideshare.twitter

import com.github.scribejava.core.model.OAuth2AccessToken
import com.twitter.clientlib.ApiException
import com.twitter.clientlib.TwitterCredentialsOAuth2
import com.twitter.clientlib.api.TwitterApi
import com.twitter.clientlib.model.TweetCreateRequest
import com.twitter.clientlib.model.TweetCreateResponse
import org.slf4j.LoggerFactory
import tech.slideshare.twitter.database.SlideDao
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

        val token = TwitterToken.load()
        val credentials = TwitterCredentialsOAuth2(
            token.oAuth2ClientID,
            token.oAuth2ClientSecret,
            token.oAuth2AccessToken,
            token.oAuth2RefreshToken,
            true
        )
        val api = TwitterApi(credentials)
        api.addCallback { accessToken: OAuth2AccessToken ->
            token.oAuth2AccessToken = accessToken.accessToken
            token.oAuth2RefreshToken = accessToken.refreshToken
        }

        var exitCode = 0
        try {
            run(user, password, api)
        } catch (e: Throwable) {
            logger.error("Tweet failed!", e)
            exitCode = 1
        }

        token.save();

        logger.info("End {}", Main.javaClass.toString())

        exitProcess(exitCode)
    }

    private fun run(user: String, password: String, api: TwitterApi) {
        DriverManager.getConnection("jdbc:mysql://localhost:3306/tech_slideshare", user, password).use { con ->
            con.autoCommit = false

            SlideDao(con).dequeue()?.let {
                try {
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

                    val request = TweetCreateRequest().text(title + "\r\n" + it.url)
                    val result: TweetCreateResponse = api.tweets().createTweet(request).execute()
                    if (result.errors != null) {
                        throw ApiException(result.toJson());
                    }

                    logger.info("Tweet success. [slide_id={}, result={}]", it.slideId, result.toJson())
                    con.commit()
                } catch (e: ApiException) {
                    logger.error("Tweet failed. [slide_id={}]", it.slideId, e)
                    con.rollback()
                }
            }
        }
    }
}
