package com.adm.url_parser.models

import androidx.annotation.Keep

@Keep
data class UrlParserResponse(
    val isSupported: Boolean,
    val model: ParsedVideo?,
    val parserName: String,
    val parserClassName: String,
    val error: String? = null,
)
