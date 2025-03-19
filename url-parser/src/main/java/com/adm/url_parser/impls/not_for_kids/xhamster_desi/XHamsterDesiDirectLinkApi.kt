package com.adm.url_parser.impls.not_for_kids.xhamster_desi

import com.adm.url_parser.commons.impl.FetchLinksFromPreloadTag
import com.adm.url_parser.interfaces.ApiLinkScrapper
import com.adm.url_parser.models.ParsedVideo

class XHamsterDesiDirectLinkApi : ApiLinkScrapper {
    override suspend fun scrapeLink(url: String): Result<ParsedVideo?> {
        return FetchLinksFromPreloadTag().scrapeLink(url)
    }
}