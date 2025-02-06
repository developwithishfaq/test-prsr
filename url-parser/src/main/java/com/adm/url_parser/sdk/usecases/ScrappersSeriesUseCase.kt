package com.adm.url_parser.sdk.usecases

import com.adm.url_parser.interfaces.ApiLinkScrapper
import com.adm.url_parser.models.ParsedVideo

class ScrappersSeriesUseCase : ScrappersUser {
    override suspend operator fun invoke(list: List<ApiLinkScrapper>, url: String): ParsedVideo? {
        list.forEach {
            val response = it.scrapeLink(url)
            if (response != null && response.qualities.isNotEmpty()) {
                return response
            }
        }
        return null
    }
}