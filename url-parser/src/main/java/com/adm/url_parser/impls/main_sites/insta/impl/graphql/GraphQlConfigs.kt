package com.adm.url_parser.impls.main_sites.insta.impl.graphql

interface GraphQlConfigs {
    suspend fun getHeaders(url: String,videoId: String): Map<String, String>
    suspend fun getDocId(): String
}