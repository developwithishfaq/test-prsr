package com.adm.url_parser.impls.main_sites.tiktok

import android.util.Log
import com.adm.url_parser.impls.main_sites.tiktok.impl.DirectUrlTiktokApiImpl
import com.adm.url_parser.impls.main_sites.tiktok.impl.TiktokApi1Impl
import com.adm.url_parser.interfaces.ApiLinkScrapper
import com.adm.url_parser.interfaces.ApiLinkScrapperForSubImpl
import com.adm.url_parser.models.ParsedVideo

class TiktokApi : ApiLinkScrapper {
    private val TAG = "TiktokApi"
    private val list: List<ApiLinkScrapperForSubImpl> = listOf(
        DirectUrlTiktokApiImpl(),
        TiktokApi1Impl(),
    )

    override suspend fun scrapeLink(url: String): ParsedVideo? {
        Log.d(TAG, "Tiktok Url =${url}")
        list.forEachIndexed { index, api ->
            val response = api.scrapeLink(url)
            Log.d(TAG, "Tiktok($index):$response")
            if (response != null) {
                return response
            }
        }
        return null
    }
}