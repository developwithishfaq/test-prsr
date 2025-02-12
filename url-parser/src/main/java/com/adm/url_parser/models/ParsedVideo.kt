package com.adm.url_parser.models

import androidx.annotation.Keep


@Keep
data class ParsedVideo(
    val qualities: List<ParsedQuality>,
    val title: String? = null,
    val thumbnail: String? = null,
    val duration: Long? = null,
    val headers: Map<String, String>? = null,
)

enum class MediaTypeData {
    Image,
    Video,
    Audio

}

@Keep
data class ParsedQuality(
    val url: String,
    val name: String? = null,
    val size: Long? = null,
    val mediaType: MediaTypeData
)
