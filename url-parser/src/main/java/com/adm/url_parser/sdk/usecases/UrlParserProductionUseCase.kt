package com.adm.url_parser.sdk.usecases

import android.util.Log
import com.adm.url_parser.interfaces.ApiLinkScrapperMainSdk
import com.adm.url_parser.models.UrlParserResponse
import com.adm.url_parser.sdk.interfaces.UrlParserConfigs

class UrlParserProductionUseCase(
    private val urlParserConfigs: UrlParserConfigs
) : ApiLinkScrapperMainSdk {
    override suspend fun scrapeLink(url: String): UrlParserResponse {
        var scrapperName = ""
        Log.d("UrlParserSdk", "scrapeLink: $url")
        val configs = urlParserConfigs.getParserConfigs(mapOf("url" to url))
        val response = if (configs.scrapper != null) {
            scrapperName = configs.parserName
            UrlParserResponse(
                isSupported = true,
                model = configs.scrapper.scrapeLink(url),
                parserName = scrapperName,
                parserClassName = configs.scrapper.javaClass.simpleName,
            )
        } else {
            UrlParserResponse(
                isSupported = false,
                model = null,
                parserName = "",
                parserClassName = "",
                error = "Unsupported Link"
            )
        }
        Log.d("UrlParserSdk", "Url Parse Response (${response.parserName}): ${response.model} ")
        return response
    }
}