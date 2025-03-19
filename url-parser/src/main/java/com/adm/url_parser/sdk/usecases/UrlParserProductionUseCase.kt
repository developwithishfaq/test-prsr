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
        val scrapper: ScrappersUser = ScrapperParallelUseCase()
        val configs = urlParserConfigs.getParserConfigs(dataMap = mapOf("url" to url))
        val response = if (configs.scrapper.isNotEmpty()) {
            scrapperName = configs.parserName
            val responseModel = scrapper.invoke(list = configs.scrapper, url = url)
            UrlParserResponse(
                isSupported = true,
                model = if (responseModel.isSuccess) {
                    responseModel.getOrNull()
                } else {
                    null
                },
                error = if (responseModel.isFailure) {
                    responseModel.exceptionOrNull()?.message
                } else {
                    null
                },
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