package com.adm.url_parser.impls.meta_data_links.dailymotion.models

import kotlinx.serialization.Serializable

@Serializable
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