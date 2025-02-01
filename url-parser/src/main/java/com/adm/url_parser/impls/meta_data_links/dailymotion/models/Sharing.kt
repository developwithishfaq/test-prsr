package com.adm.url_parser.impls.meta_data_links.dailymotion.models

import kotlinx.serialization.Serializable


@Serializable
data class Sharing(
    val service: String,
    val url: String
)