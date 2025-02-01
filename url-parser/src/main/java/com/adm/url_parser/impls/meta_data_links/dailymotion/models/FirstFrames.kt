package com.adm.url_parser.impls.meta_data_links.dailymotion.models

import kotlinx.serialization.Serializable

@Serializable
data class FirstFrames(
    val `1080`: String?,
    val `120`: String?,
    val `180`: String?,
    val `240`: String?,
    val `360`: String?,
    val `480`: String?,
    val `60`: String?,
    val `720`: String?
)