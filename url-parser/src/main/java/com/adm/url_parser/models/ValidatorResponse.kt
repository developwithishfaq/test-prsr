package com.adm.url_parser.models

import com.adm.url_parser.interfaces.ApiLinkScrapper

data class ValidatorResponse(
    val scrapper: List<ApiLinkScrapper>,
    val parserName: String = ""
)
