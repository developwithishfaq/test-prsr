package com.adm.url_parser.models


data class ParsedVideo(
    val qualities: List<ParsedQuality>,
    val mediaType: MediaTypeData,
    val title: String? = null,
    val thumbnail: String? = null,
    val duration: String? = null,
    val headers: Map<String, String>? = null,
)

enum class MediaTypeData {
    Image,
    Video,
    Audio

}

data class ParsedQuality(
    val url: String,
    val name: String? = null,
    val size: Long? = null
)
