package tech.slideshare.twitter.database

import java.sql.Date

data class SlideDto(
    val slideId: Int,
    val title: String,
    val url: String,
    val date: Date,
    val author: String?,
    val twitter: String?,
    val hashTag: String?,
)
