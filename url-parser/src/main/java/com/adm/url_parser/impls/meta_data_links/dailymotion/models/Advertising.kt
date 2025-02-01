package com.adm.url_parser.impls.meta_data_links.dailymotion.models

import kotlinx.serialization.Serializable

@Serializable
data class Advertising(
    val ad_error_url: String? = null,
    val ad_sync_script_url: String? = null,
    val ad_url: String? = null,
    val ima: Ima? = null
)