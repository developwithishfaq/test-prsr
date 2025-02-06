package com.adm.url_parser.impls.main_sites.tiktok

import com.adm.url_parser.impls.main_sites.tiktok.impl.DirectUrlTiktokApiImpl
import com.adm.url_parser.impls.main_sites.tiktok.impl.TiktokApi1Impl
import com.adm.url_parser.interfaces.ApiLinkScrapper
import com.adm.url_parser.interfaces.ApiLinkScrapperForSubImpl
import com.adm.url_parser.models.ParsedVideo

class TiktokApi : ApiLinkScrapper {
    private val list: List<ApiLinkScrapperForSubImpl> = listOf(
        TiktokApi1Impl(),
        DirectUrlTiktokApiImpl(),
    )

    override suspend fun scrapeLink(url: String): ParsedVideo? {
        list.forEach { api ->
            val response = api.scrapeLink(url)
            if (response != null) {
                return response
            }
        }
        return null
    }
}