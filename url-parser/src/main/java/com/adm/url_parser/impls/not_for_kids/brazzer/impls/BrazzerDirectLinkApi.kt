package com.adm.url_parser.impls.not_for_kids.brazzer.impls

import android.util.Log
import com.adm.url_parser.models.MediaTypeData
import com.adm.url_parser.models.ParsedQuality
import com.adm.url_parser.models.ParsedVideo
import com.adm.url_parser.commons.network.ParserRequestTypes
import com.adm.url_parser.commons.network.UrlParserNetworkClient
import com.adm.url_parser.interfaces.ApiLinkScrapper

class BrazzerDirectLinkApi : ApiLinkScrapper {
    override suspend fun scrapeLink(url: String): ParsedVideo? {
        Log.d("apiImpl", "Brazzer Link:$url ")
        val response =
            UrlParserNetworkClient.makeNetworkRequestString(url, ParserRequestTypes.Get).data ?: ""
        val title = response.substringAfter("\"name\": \"").substringBefore("\",")
        val thumbnail =
            response.substringAfter("\"thumbnailUrl\": \"").substringBefore("\",")
        val link = response.substringAfter("\"contentUrl\": \"")
            .substringBefore("\",")
        Log.d("apiImpl", "scrapeLink 1:$link ")
        val quality = link.substringBeforeLast(".mp4").substringAfterLast("_")
        return if (link.isNotBlank()) {
            ParsedVideo(
                title = title,
                thumbnail = thumbnail,
                qualities = listOf(
                    ParsedQuality(
                        url = link,
                        name = quality
                    )
                ),
                mediaType = MediaTypeData.Video
            )
        } else {
            null
        }
    }
}