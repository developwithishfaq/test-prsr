package com.adm.url_parser.models

import androidx.annotation.Keep
import com.adm.url_parser.interfaces.ApiLinkScrapper

@Keep
data class ValidatorResponse(
    val scrapper: List<ApiLinkScrapper>,
    val parserName: String = ""
)
