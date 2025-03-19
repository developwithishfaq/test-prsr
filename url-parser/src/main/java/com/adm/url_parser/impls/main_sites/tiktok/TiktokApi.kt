package com.adm.url_parser.impls.main_sites.tiktok

import android.util.Log
import com.adm.url_parser.impls.main_sites.tiktok.impl.DirectUrlTiktokApiImpl
import com.adm.url_parser.impls.main_sites.tiktok.impl.TiktokApi1Impl
import com.adm.url_parser.interfaces.ApiLinkScrapper
import com.adm.url_parser.interfaces.ApiLinkScrapperForSubImpl
import com.adm.url_parser.models.ParsedVideo

class TiktokApi : ApiLinkScrapper {
    private val TAG = "TiktokApi"

    override suspend fun scrapeLink(url: String): Result<ParsedVideo?> {
        val list: List<ApiLinkScrapperForSubImpl> = if (url.contains("/video/")) {
            listOf(
                DirectUrlTiktokApiImpl(),
                TiktokApi1Impl(),
            )
        } else {
            listOf(
                TiktokApi1Impl(),
            )
        }
        Log.d(TAG, "Tiktok Url =${url}")
        list.forEachIndexed { index, api ->
            val response = api.scrapeLink(url)
            if (response.isSuccess) {
                Log.d(TAG, "Tiktok($index):$response")
                if (response.getOrNull()?.qualities?.isNotEmpty() == true) {
                    return response
                }
            }
        }
        return Result.failure(Exception("No response found in TiktokApi"))
    }
}