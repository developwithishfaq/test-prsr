package com.adm.url_parser.impls.main_sites.tiktok

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class Data(
    val ai_dynamic_cover: String? = null,
    val anchors: String? = null,
    val anchors_extras: String? = null,
    val author: Author? = null,
    val collect_count: Int? = null,
    val comment_count: Int? = null,
    val commerce_info: CommerceInfo? = null,
    val commercial_video_info: String? = null,
    val cover: String? = null,
    val create_time: Int? = null,
    val digg_count: Int? = null,
    val download_count: Int? = null,
    val duration: Int? = null,
    val hd_size: Int? = null,
    val hdplay: String? = null,
    val id: String? = null,
    val is_ad: Boolean? = null,
    val item_comment_settings: Int? = null,
    val music: String? = null,
    val music_info: MusicInfo? = null,
    val origin_cover: String? = null,
    val play: String? = null,
    val play_count: Int? = null,
    val region: String? = null,
    val share_count: Int? = null,
    val size: Int? = null,
    val title: String? = null,
    val wm_size: String? = null,
    val wmplay: String? = null
)

@Serializable
@Keep
data class CommerceInfo(
    val adv_promotable: Boolean? = null,
    val auction_ad_invited: Boolean? = null,
    val branded_content_type: Int? = null,
    val with_comment_filter_words: Boolean? = null
)


@Serializable
@Keep
data class MusicInfo(
    val album: String? = null,
    val author: String? = null,
    val cover: String? = null,
    val duration: Int? = null,
    val id: String? = null,
    val original: Boolean? = null,
    val play: String? = null,
    val title: String? = null
)

@Serializable
@Keep
data class Author(
    val avatar: String? = null,
    val id: String? = null,
    val nickname: String? = null,
    val unique_id: String? = null
)