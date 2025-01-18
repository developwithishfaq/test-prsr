package com.adm.url_parser.impls.not_for_kids.porn_hub

import kotlinx.serialization.Serializable

@Serializable
data class PornHubApiResponse(
    val defaultQuality: Boolean,
    val format: String,
    val group: Int,
    val height: Int,
    val quality: String,
    val videoUrl: String,
    val width: Int
)