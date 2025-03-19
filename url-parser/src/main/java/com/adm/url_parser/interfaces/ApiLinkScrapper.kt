package com.adm.url_parser.interfaces

import com.adm.url_parser.models.ParsedVideo
import com.adm.url_parser.models.UrlParserResponse

interface ApiLinkScrapper {
    suspend fun scrapeLink(url: String): Result<ParsedVideo?>
}

interface ApiLinkScrapperMainSdk {
    suspend fun scrapeLink(url: String): UrlParserResponse
}

interface ApiLinkScrapperForSubImpl {
    suspend fun scrapeLink(url: String): Result<ParsedVideo?>
}

