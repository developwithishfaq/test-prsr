package com.adm.url_parser.sdk

import android.util.Log
import com.adm.url_parser.commons.utils.support_checker.UrlParserCheckSupportImpl
import com.adm.url_parser.interfaces.ApiLinkScrapperMainSdk
import com.adm.url_parser.models.UrlParserResponse
import com.adm.url_parser.sdk.impl.UrlParserConfigsImpl
import com.adm.url_parser.sdk.impl.VideoModelCleanerImpl
import com.adm.url_parser.sdk.interfaces.UrlParserConfigs
import com.adm.url_parser.sdk.interfaces.VideoModelCleaner
import com.adm.url_parser.sdk.usecases.UrlParserProductionUseCase

class UrlParserSdk(
    private val urlParserConfigs: UrlParserConfigs = UrlParserConfigsImpl(UrlParserCheckSupportImpl()),
    private val cleaner: VideoModelCleaner = VideoModelCleanerImpl()
) : ApiLinkScrapperMainSdk {
    private val TAG = "UrlParserSdk"
    private lateinit var useCase: ApiLinkScrapperMainSdk

    override suspend fun scrapeLink(url: String): UrlParserResponse {
        useCase = UrlParserProductionUseCase(urlParserConfigs)
        val response: UrlParserResponse = useCase.scrapeLink(url)
        val cleanedResponse = cleaner.getCleanedVideoModel(response)
        Log.d(TAG, "Response(${response.isSupported}):${response.model} ")
        Log.d(TAG, "Response Cleaned(${response.isSupported}):${cleanedResponse} ")
        return cleanedResponse
    }
}