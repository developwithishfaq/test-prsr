package com.adm.url_parser.impls.main_sites.fb.impls

import android.util.Log
import com.adm.url_parser.commons.Commons.getTitleFromHtml
import com.adm.url_parser.commons.Commons.removeUnnecessarySlashes
import com.adm.url_parser.commons.network.ParserRequestTypes
import com.adm.url_parser.commons.network.UrlParserNetworkClient
import com.adm.url_parser.commons.network.UrlParserNetworkResponse
import com.adm.url_parser.models.MediaTypeData
import com.adm.url_parser.models.ParsedQuality
import com.adm.url_parser.models.ParsedVideo

class FacebookDirectUrlApi(
) {
    private val TAG = "FacebookDirectUrlApi"
    suspend fun scrapeLink(videoId: String): ParsedVideo? {
        Log.d(TAG, "scrapeLink: videoId=${videoId}")
        val newUrl = "https://www.facebook.com/reel/${videoId.replace("/", "")}"
        Log.d(TAG, "scrapeLink: New Url=${newUrl}")
        val response = UrlParserNetworkClient.makeNetworkRequestString(
            url = newUrl,
            requestType = ParserRequestTypes.Get,
            headers = hashMapOf(
                "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9",
            )
        )
        Log.d(TAG, "scrapeVideos fb: $response")
        val videos = mutableListOf<ParsedQuality>()
        var thumbnail: String? = null
        return if (response is UrlParserNetworkResponse.Success) {
            val res = response.data ?: ""
            thumbnail = res.substringAfter("\"image\":{\"uri\":\"").substringBefore("\"},")
            val hdUrl = getLinkByTag(res, "browser_native_hd_url")
            val sdUrl = getLinkByTag(res, "browser_native_sd_url")
            hdUrl?.let {
                videos.add(
                    ParsedQuality(
                        name = "HD",
                        url = it
                    )
                )
            }
            sdUrl?.let {
                videos.add(
                    ParsedQuality(
                        name = "SD",
                        url = it
                    )
                )
            }
            Log.d(TAG, "scrapeVideos fb browser_native_sd_url: $sdUrl")
            Log.d(TAG, "scrapeVideos fb browser_native_hd_url: $hdUrl")
            Log.d(TAG, "scrapeVideos fb thumbnail: ${thumbnail.removeUnnecessarySlashes()}")
            Log.d(TAG, "scrapeVideos fb size:${videos.size} ")
            Log.d(TAG, "scrapeVideos fb qualities:${videos} ")
            ParsedVideo(
                title = res.getTitleFromHtml(),
                mediaType = MediaTypeData.Video,
                qualities = videos,
                thumbnail = thumbnail.removeUnnecessarySlashes()
            )
        } else {
            Log.d(TAG, "scrapeVideos insta html : $response")
            null
        }
    }



    private fun getLinkByTag(html: String, tag: String): String? {
        val hdUrl = if (html.contains(tag)) {
            html.substringAfter(tag).substringBefore("\",")
                .replace("\\", "")
                .replace("u00253D", "%3D")
                .replace("u00252", "%2")
                .replace("\":\"", "")
        } else {
            null
        }
        return if (hdUrl?.startsWith("http") == true) {
            hdUrl
        } else {
            null
        }
    }
}