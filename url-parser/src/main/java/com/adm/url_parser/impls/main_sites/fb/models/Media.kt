package com.adm.url_parser.impls.main_sites.fb.models

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class Media(
    val audioAvailable: Boolean? = null,
    val cached: Boolean? = null,
    val chunked: Boolean? = null,
    val extension: String? = null,
    val formattedSize: String? = null,
    val quality: String? = null,
    val requiresRendering: Boolean? = null,
    val size: Int? = null,
    val url: String? = null,
    val videoAvailable: Boolean? = null
)