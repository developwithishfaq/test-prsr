package com.adm.url_parser.impls.meta_data_links.dailymotion.models

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class DailyMotionMetaData(
    val access_id: String? = null,
    val advertising: Advertising? = null,
    val channel: String? = null,
    val consent: Consent? = null,
    val created_time: Int? = null,
    val data_center: String? = null,
    val detected_language: String? = null,
    val duration: Int? = null,
    val explicit: Boolean? = null,
    val filmstrip_url: String? = null,
    val first_frames: FirstFrames? = null,
    val has_paid_partnership: Boolean? = null,
    val id: String? = null,
    val info: Info? = null,
    val is_created_for_kids: Boolean? = null,
    val is_password_protected: Boolean? = null,
    val language: String? = null,
    val live_show_viewers: Boolean? = null,
    val media_type: String? = null,
    val mode: String? = null,
    val owner: Owner? = null,
    val partner: Boolean? = null,
    val player_owner: PlayerOwner? = null,
    val `private`: Boolean? = null,
    val protected_delivery: Boolean? = null,
    val qualities: Qualities? = null,
    val reporting: Reporting? = null,
    val seeker_url: String? = null,
    val sharing: List<Sharing>? = null,
    val stream_type: String? = null,
    val subtitles: Subtitles? = null,
//    val tags: List<Any>,
    val thumbnails: Thumbnails? = null,
    val title: String? = null,
//    val ui: List<Any>,
    val url: String? = null,
    val verified: Boolean? = null,
    val view_id: String? = null
)