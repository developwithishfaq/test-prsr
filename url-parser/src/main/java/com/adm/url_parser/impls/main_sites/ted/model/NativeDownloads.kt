package com.adm.url_parser.impls.main_sites.ted.model

import kotlinx.serialization.Serializable


@Serializable
data class NativeDownloads(
    val __typename: String,
    val high: String?,
    val low: String?,
    val medium: String?
)