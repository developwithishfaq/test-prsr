package com.adm.url_parser.impls.not_for_kids.brazzer.impls

import android.util.Log
import com.adm.url_parser.commons.network.ParserRequestTypes
import com.adm.url_parser.commons.network.UrlParserNetworkClient
import com.adm.url_parser.commons.network.UrlParserNetworkResponse
import com.adm.url_parser.interfaces.ApiLinkScrapper
import com.adm.url_parser.models.MediaTypeData
import com.adm.url_parser.models.ParsedQuality
import com.adm.url_parser.models.ParsedVideo

class BrazzerDirectLinkApi : ApiLinkScrapper {
    private val TAG = "BrazzerDirectLinkApi"
    override suspend fun scrapeLink(url: String): Result<ParsedVideo?> {
        Log.d(TAG, "Brazzer Link:$url ")
        val responseOfNetwork =
            UrlParserNetworkClient.makeNetworkRequestString(url, ParserRequestTypes.Get)
        if (responseOfNetwork is UrlParserNetworkResponse.Failure) {
            return Result.failure(Exception(responseOfNetwork.error ?: ""))
        }
        val response = responseOfNetwork.data ?: ""
        val title = response.substringAfter("\"name\": \"").substringBefore("\",")
        Log.d(TAG, "scrapeLink: title=$title")
        val thumbnail = response.substringAfter("\"thumbnailUrl\": \"").substringBefore("\",")
        val link = response.substringAfter("\"contentUrl\": \"").substringBefore("\",")
        Log.d(TAG, "scrapeLink 1:$link ")
        return if (link.isNotBlank()) {
            Result.success(
                ParsedVideo(
                    title = title, thumbnail = thumbnail, qualities = listOf(
                        ParsedQuality(
                            url = link, name = "Video", mediaType = MediaTypeData.Video
                        )
                    )
                )
            )
        } else {
            Result.failure(Exception("Link Not Found "))
        }
    }
}