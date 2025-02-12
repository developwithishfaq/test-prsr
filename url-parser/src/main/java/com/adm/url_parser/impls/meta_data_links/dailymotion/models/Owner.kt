package com.adm.url_parser.impls.meta_data_links.dailymotion.models

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class Owner(
    val avatars: Avatars,
    val id: String,
//    val parent_id: Any,
    val screenname: String,
    val type: String,
    val url: String,
    val username: String,
//    val watermark_image_url: Any,
//    val watermark_link_url: Any
)