package com.adm.url_parser.sdk.usecases

import com.adm.url_parser.interfaces.ApiLinkScrapper
import com.adm.url_parser.models.ParsedVideo

interface ScrappersUser {
    suspend operator fun invoke(list: List<ApiLinkScrapper>, url: String): Result<ParsedVideo?>
}