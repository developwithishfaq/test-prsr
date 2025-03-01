package com.adm.url_parser.impls.not_for_kids.xnxx

import android.util.Log
import com.adm.url_parser.commons.network.ParserRequestTypes
import com.adm.url_parser.commons.network.UrlParserNetworkClient
import com.adm.url_parser.interfaces.ApiLinkScrapper
import com.adm.url_parser.models.MediaTypeData
import com.adm.url_parser.models.ParsedQuality
import com.adm.url_parser.models.ParsedVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class XnxxApiImpl : ApiLinkScrapper {
    private val TAG = "XnxxApiImpl"
    override suspend fun scrapeLink(url: String): ParsedVideo? {
        Log.d(TAG, "scrapeLink: Called")
        return withContext(Dispatchers.IO) {
            val response = UrlParserNetworkClient.makeNetworkRequestString(
                url = url,
                requestType = ParserRequestTypes.Get,
                headers = mapOf(
                    "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36",
                )
            ).data ?: ""
            val title = response.substringAfter("setVideoTitle('").substringBefore("');")
            val link = response.substringAfter("setVideoHLS('").substringBefore("');")
            val thumbnail = response.substringAfter("setThumbUrl('").substringBefore("');")
            Log.d(TAG, "Xnxx Video Title is $title")
            val video = if (link.isNotBlank()) {
                ParsedVideo(
                    title = title,
                    thumbnail = thumbnail,
                    qualities = listOf(
                        ParsedQuality(
                            url = link,
                            name = "HD",
                            mediaType = MediaTypeData.Video
                        )
                    )
                )
            } else {
                null
            }
            Log.d(TAG, "scrapeLink: Xnn video $video")
            video
        }
    }
}