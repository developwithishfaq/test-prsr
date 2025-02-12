package com.adm.url_parser.impls.meta_data_links.dailymotion.models

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class Subtitles(
//    val `data`: List<Any>,
    val enable: Boolean
)