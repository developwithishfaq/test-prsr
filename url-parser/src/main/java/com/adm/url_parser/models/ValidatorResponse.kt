package com.adm.url_parser.models

import com.adm.url_parser.interfaces.ApiLinkScrapper

data class ValidatorResponse(
    val scrapper: ApiLinkScrapper?,
    val parserName: String = ""
)
