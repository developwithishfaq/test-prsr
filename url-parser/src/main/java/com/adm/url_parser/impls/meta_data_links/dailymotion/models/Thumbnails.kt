package com.adm.url_parser.impls.meta_data_links.dailymotion.models

import kotlinx.serialization.Serializable


@Serializable
data class Thumbnails(
    val `1080`: String? = null,
    val `120`: String? = null,
    val `180`: String? = null,
    val `240`: String? = null,
    val `360`: String? = null,
    val `480`: String? = null,
    val `60`: String? = null,
    val `720`: String? = null
)