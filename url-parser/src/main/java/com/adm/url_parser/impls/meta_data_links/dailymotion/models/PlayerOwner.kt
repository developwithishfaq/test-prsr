package com.adm.url_parser.impls.meta_data_links.dailymotion.models

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class PlayerOwner(
    val id: String,
//    val watermark_image_url: Any,
//    val watermark_link_url: Any
)