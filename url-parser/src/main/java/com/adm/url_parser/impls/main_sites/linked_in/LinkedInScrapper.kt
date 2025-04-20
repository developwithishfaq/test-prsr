package com.adm.url_parser.impls.main_sites.linked_in

import com.adm.url_parser.impls.main_sites.linked_in.impl.ExpertPhpLinkedinScrapper
import com.adm.url_parser.interfaces.ApiLinkScrapper
import com.adm.url_parser.models.ParsedVideo

class LinkedInScrapper : ApiLinkScrapper {
    override suspend fun scrapeLink(url: String): Result<ParsedVideo?> {
        return ExpertPhpLinkedinScrapper().scrapeLink(url)
    }
}