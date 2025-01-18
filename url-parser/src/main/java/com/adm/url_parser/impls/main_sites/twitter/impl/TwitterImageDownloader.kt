/*
package com.adm.url_parser.impls.main_sites.twitter.impl

import com.adm.url_parser.commons.network.ParserRequestTypes
import com.adm.url_parser.commons.network.UrlParserNetworkClient
import com.adm.url_parser.impls.main_sites.twitter.Twitter
import com.adm.url_parser.interfaces.ApiLinkScrapperForSubImpl
import com.adm.url_parser.models.ParsedVideo

class TwitterImageDownloader : ApiLinkScrapperForSubImpl {
    override suspend fun scrapeLink(url: String): ParsedVideo? {
        val response = UrlParserNetworkClient.makeNetworkRequestString(
            url = url,
            requestType = ParserRequestTypes.Get,
            headers = null
        ).data ?: ""
        response.substringBeforeLast("css-9pa8cd")
    }
}*/
