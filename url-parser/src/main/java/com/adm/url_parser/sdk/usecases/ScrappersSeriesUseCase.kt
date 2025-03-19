package com.adm.url_parser.sdk.usecases

import com.adm.url_parser.interfaces.ApiLinkScrapper
import com.adm.url_parser.models.ParsedVideo

class ScrappersSeriesUseCase : ScrappersUser {
    override suspend operator fun invoke(
        list: List<ApiLinkScrapper>,
        url: String
    ): Result<ParsedVideo?> {
        var error: String? = null
        list.forEach {
            val response = it.scrapeLink(url)
            if (response.isSuccess) {
                val model = response.getOrNull()
                if (model != null && model.qualities.isNotEmpty()) {
                    return response
                }
            } else {
                error = response.exceptionOrNull()?.message
            }
        }
        return Result.failure(Exception("No response found in ScrappersSeriesUseCase error $error"))
    }
}