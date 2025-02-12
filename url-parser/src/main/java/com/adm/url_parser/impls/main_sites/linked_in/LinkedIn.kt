package com.adm.url_parser.impls.main_sites.linked_in

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class LinkedIn(
    val link: String? = null,
    val text: String? = null,
    val title: String? = null
)