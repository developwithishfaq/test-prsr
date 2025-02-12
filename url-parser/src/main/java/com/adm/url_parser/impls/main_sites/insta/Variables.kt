package com.adm.url_parser.impls.main_sites.insta

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class Variables(
    val shortcode: String,
)