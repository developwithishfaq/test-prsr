package com.adm.url_parser.impls.main_sites.ted.model

import kotlinx.serialization.Serializable


@Serializable
data class Videos(
    val __typename: String,
    val nodes: List<Node>
)