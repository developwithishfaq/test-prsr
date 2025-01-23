package com.adm.url_parser.impls.main_sites.insta.impl.graphql

import io.ktor.http.HttpHeaders

class GraphQlConfigsImpl : GraphQlConfigs {
    override suspend fun getHeaders(url: String, videoId: String): Map<String, String> {
        return mapOf(
            HttpHeaders.Cookie to "csrftoken=KfUBze2TeAG0H4FrGFi0B2; csrftoken=DQXjFKuLZhp53agTq3S7hS; ig_did=9B0E8882-43CC-49CF-8596-28E549D03E6E; ig_nrcb=1; mid=Zz2B8AAEAAHWk0oIY1pMO4AGCKvh",
            HttpHeaders.ContentType to "application/json",
            HttpHeaders.Referrer to "https://www.instagram.com/p/$videoId/",
            HttpHeaders.UserAgent to "Mozilla/5.0 (Linux; U; Android 9; en-us; SM-G988N Build/JOP24G) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/86.0.4240.198 Mobile Safari/537.36"
        )
    }

    override suspend fun getDocId(): String {
        return "8845758582119845"
    }
}