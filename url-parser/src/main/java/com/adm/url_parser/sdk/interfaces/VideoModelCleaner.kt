package com.adm.url_parser.sdk.interfaces

import com.adm.url_parser.models.UrlParserResponse

interface VideoModelCleaner {
    fun getCleanedVideoModel(videoModel: UrlParserResponse): UrlParserResponse
}