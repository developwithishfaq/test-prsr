package com.adm.url_parser.impls.not_for_kids.xnxx_health

import com.adm.url_parser.commons.impl.FetchLinksFromHTML5Player
import com.adm.url_parser.interfaces.ApiLinkScrapper
import com.adm.url_parser.models.ParsedVideo

class XnxxHealthApiImpl : ApiLinkScrapper {
    override suspend fun scrapeLink(url: String): ParsedVideo? {
        return FetchLinksFromHTML5Player().scrapeLink(url)
    }
}