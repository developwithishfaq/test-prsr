package com.adm.url_parser.impls.main_sites.linked_in

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class LinkedInModel(
    val Url: String = "",
    val avc: Int = 17
)