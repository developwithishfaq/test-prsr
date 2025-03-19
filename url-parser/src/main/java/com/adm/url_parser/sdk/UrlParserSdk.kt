package com.adm.url_parser.sdk

import android.util.Log
import com.adm.url_parser.commons.utils.support_checker.UrlParserCheckSupportImpl
import com.adm.url_parser.impls.main_sites.insta.impl.graphql.GraphQlConfigs
import com.adm.url_parser.impls.main_sites.insta.impl.graphql.GraphQlConfigsImpl
import com.adm.url_parser.interfaces.ApiLinkScrapperMainSdk
import com.adm.url_parser.models.UrlParserResponse
import com.adm.url_parser.sdk.impl.UrlParserConfigsImpl
import com.adm.url_parser.sdk.impl.VideoModelCleanerImpl
import com.adm.url_parser.sdk.interfaces.UrlParserConfigs
import com.adm.url_parser.sdk.interfaces.VideoModelCleaner
import com.adm.url_parser.sdk.usecases.UrlParserProductionUseCase

class UrlParserSdk(
    private val cleaner: VideoModelCleaner = VideoModelCleanerImpl(),
    private val graphQlConfigs: GraphQlConfigs = GraphQlConfigsImpl(),
    private val urlParserConfigs: UrlParserConfigs = UrlParserConfigsImpl(
        urlParserCheckSupport = UrlParserCheckSupportImpl(),
        graphQlConfigs = graphQlConfigs
    ),
) : ApiLinkScrapperMainSdk {
    private val TAG = "UrlParserSdk"
    private lateinit var useCase: ApiLinkScrapperMainSdk

    override suspend fun scrapeLink(url: String): UrlParserResponse {
        useCase = UrlParserProductionUseCase(urlParserConfigs = urlParserConfigs)
        val response: UrlParserResponse = useCase.scrapeLink(url)
        val cleanedResponse: UrlParserResponse = cleaner.getCleanedVideoModel(response)
        Log.d(TAG, "Response(${response.isSupported}):${response.model} ")
        Log.d(TAG, "Response Cleaned(${response.isSupported}):${cleanedResponse} ")
        return cleanedResponse
    }
}

