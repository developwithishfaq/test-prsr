package com.adm.url_parser.impls.main_sites.tiktok

import kotlinx.serialization.Serializable


@Serializable
data class Tiktok(
    val code: Int? = null,
    val `data`: Data? = null,
    val msg: String? = null,
    val processed_time: Double? = null
)