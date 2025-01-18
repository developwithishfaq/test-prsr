package com.adm.url_parser.sdk

import android.util.Log
import com.adm.url_parser.commons.utils.support_checker.UrlParserCheckSupportImpl
import com.adm.url_parser.interfaces.ApiLinkScrapperMainSdk
import com.adm.url_parser.models.UrlParserResponse
import com.adm.url_parser.sdk.impl.UrlParserConfigsImpl
import com.adm.url_parser.sdk.usecases.UrlParserProductionUseCase

class UrlParserSdk(
    private val urlParserConfigs: UrlParserConfigs = UrlParserConfigsImpl(UrlParserCheckSupportImpl())
) : ApiLinkScrapperMainSdk {
    private val TAG = "UrlParserSdk"
    private lateinit var useCase: ApiLinkScrapperMainSdk

    override suspend fun scrapeLink(url: String): UrlParserResponse {
        useCase = UrlParserProductionUseCase(urlParserConfigs)
        val response = useCase.scrapeLink(url)
        Log.d(TAG, "Response(${response.isSupported}):${response.model} ")
        return response
    }
}