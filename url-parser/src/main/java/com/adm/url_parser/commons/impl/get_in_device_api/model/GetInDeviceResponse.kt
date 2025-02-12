package com.adm.url_parser.commons.impl.get_in_device_api.model

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class GetInDeviceResponse(
    val duration: String? = null,
    val medias: List<Media>? = null,
    val sid: String? = null,
    val source: String? = null,
    val thumbnail: String? = null,
    val title: String? = null,
    val url: String? = null
)