package com.adm.url_parser.impls.main_sites.ted.model

import kotlinx.serialization.Serializable


@Serializable
data class Node(
    val __typename: String? = null,
    val audioDownload: String? = null,
    val canonicalUrl: String? = null,
    val id: String? = null,
    val nativeDownloads: NativeDownloads? = null,
)