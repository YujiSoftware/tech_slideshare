package tech.slideshare.twitter

import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class TwitterToken private constructor(private val properties: Properties) {

    val oAuth2ClientID: String
        get() = properties.getProperty(OAUTH2_CLIENT_ID)
    val oAuth2ClientSecret: String
        get() = properties.getProperty(OAUTH2_CLIENT_SECRET)
    var oAuth2AccessToken: String
        get() = properties.getProperty(OAUTH2_ACCESS_TOKEN)
        set(accessToken) {
            properties.setProperty(OAUTH2_ACCESS_TOKEN, accessToken)
        }
    var oAuth2RefreshToken: String
        get() = properties.getProperty(OAUTH2_REFRESH_TOKEN)
        set(refreshToken) {
            properties.setProperty(OAUTH2_REFRESH_TOKEN, refreshToken)
        }

    @Throws(IOException::class)
    fun save() {
        Files.newBufferedWriter(FILE, StandardCharsets.UTF_8).use { writer ->
            properties.store(writer, "")
        }
    }

    companion object {
        private val FILE = Path.of("twitter.properties")
        private const val OAUTH2_CLIENT_ID = "TWITTER_OAUTH2_CLIENT_ID"
        private const val OAUTH2_CLIENT_SECRET = "TWITTER_OAUTH2_CLIENT_SECRET"
        private const val OAUTH2_ACCESS_TOKEN = "TWITTER_OAUTH2_ACCESS_TOKEN"
        private const val OAUTH2_REFRESH_TOKEN = "TWITTER_OAUTH2_REFRESH_TOKEN"

        @Throws(IOException::class)
        fun load(): TwitterToken {
            val properties = Properties()
            Files.newBufferedReader(FILE, StandardCharsets.UTF_8).use { reader ->
                properties.load(reader)
            }
            return TwitterToken(properties)
        }
    }
}
