package com.adm.url_parser.impls.meta_data_links.dailymotion.models

import kotlinx.serialization.Serializable

@Serializable
data class ComScore(
    val c2: String,
    val c3: String,
    val c4: String
)