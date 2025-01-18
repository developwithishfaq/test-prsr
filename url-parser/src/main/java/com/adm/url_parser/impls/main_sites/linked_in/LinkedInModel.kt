package com.adm.url_parser.impls.main_sites.linked_in

import kotlinx.serialization.Serializable

@Serializable
data class LinkedInModel(
    val Url: String = "",
    val avc: Int = 17
)