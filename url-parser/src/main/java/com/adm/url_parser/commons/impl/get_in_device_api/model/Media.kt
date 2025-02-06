package com.adm.url_parser.commons.impl.get_in_device_api.model

import kotlinx.serialization.Serializable

@Serializable
data class Media(
    val audioAvailable: Boolean? = null,
    val cached: Boolean? = null,
    val chunked: Boolean? = null,
    val extension: String? = null,
    val formattedSize: String? = null,
    val quality: String? = null,
    val requiresRendering: Boolean? = null,
    val size: Int,
    val url: String? = null,
    val videoAvailable: Boolean? = null
)