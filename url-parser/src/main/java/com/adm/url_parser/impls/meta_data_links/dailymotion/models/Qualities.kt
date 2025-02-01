package com.adm.url_parser.impls.meta_data_links.dailymotion.models

import kotlinx.serialization.Serializable


@Serializable
data class Qualities(
    val auto: List<Auto>
)