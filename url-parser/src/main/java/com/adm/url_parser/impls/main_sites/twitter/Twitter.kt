package com.adm.url_parser.impls.main_sites.twitter

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class Twitter(
    val code: Int? = null,
    val message: String? = null,
    val tweet: Tweet? = null
)

@Serializable
@Keep
data class Tweet(
    val author: Author? = null,
    val created_at: String? = null,
    val created_timestamp: Int? = null,
    val id: String? = null,
    val lang: String? = null,
    val likes: Int? = null,
    val media: Media? = null,
    val possibly_sensitive: Boolean? = null,
    val replies: Int? = null,
    val retweets: Int? = null,
    val text: String? = null,
    val twitter_card: String? = null,
    val url: String? = null
)

@Serializable
@Keep
data class Author(
    val avatar_color: String? = null,
    val avatar_url: String? = null,
    val banner_url: String? = null,
    val id: String? = null,
    val name: String? = null,
    val screen_name: String? = null
)

@Serializable
@Keep
data class Media(
    val videos: List<Video>? = null,
    val images: List<Image>? = null
)


@Serializable
@Keep
data class Image(
    val type: String? = null,
    val image_url: String? = null,
)

@Serializable
@Keep
data class Video(
    val duration: Double? = null,
    val thumbnail_url: String? = null,
    val type: String? = null,
    val video_urls: List<VideoUrl>? = null
)

@Serializable
@Keep
data class VideoUrl(
    val bitrate: Int? = null,
    val content_type: String? = null,
    val url: String? = null
)