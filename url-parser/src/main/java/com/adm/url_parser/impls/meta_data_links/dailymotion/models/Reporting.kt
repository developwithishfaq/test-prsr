package com.adm.url_parser.impls.meta_data_links.dailymotion.models

import kotlinx.serialization.Serializable


@Serializable
data class Reporting(
    val comScore: ComScore? = null,
    val enable: Boolean? = null,
    val ias: Ias? = null
)