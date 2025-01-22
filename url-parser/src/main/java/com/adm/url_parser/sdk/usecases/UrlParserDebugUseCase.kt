package com.adm.url_parser.sdk.usecases

import com.adm.url_parser.interfaces.ApiLinkScrapperMainSdk
import com.adm.url_parser.models.UrlParserResponse
import com.adm.url_parser.sdk.interfaces.UrlParserConfigs
import kotlinx.coroutines.delay

class UrlParserDebugUseCase(
    private val urlParserConfigs: UrlParserConfigs
) : ApiLinkScrapperMainSdk {
    override suspend fun scrapeLink(url: String): UrlParserResponse {
        delay(3000)
        return UrlParserResponse(
            isSupported = false,
            model = null,
            parserName = "ParserName",
            parserClassName = "ParserClassName"
        )
    }
}