package com.adm.url_parser.sdk

import com.adm.url_parser.models.ValidatorResponse

interface UrlParserConfigs {
    fun getParserConfigs(dataMap: Map<String, String>): ValidatorResponse
}