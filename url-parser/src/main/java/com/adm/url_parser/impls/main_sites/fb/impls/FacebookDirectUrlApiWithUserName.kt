package com.adm.url_parser.impls.main_sites.fb.impls

import android.util.Log
import com.adm.url_parser.commons.Commons.getTitleFromHtml
import com.adm.url_parser.commons.Commons.removeUnnecessarySlashes
import com.adm.url_parser.commons.network.ParserRequestTypes
import com.adm.url_parser.commons.network.UrlParserNetworkClient
import com.adm.url_parser.models.MediaTypeData
import com.adm.url_parser.models.ParsedQuality
import com.adm.url_parser.models.ParsedVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FacebookDirectUrlApiWithUserName(
) {
    private val TAG = "FacebookDirectUrlApiWithUserName"
    suspend fun scrapeLink(videoId: String, userName: String): ParsedVideo? {
        Log.d(TAG, "scrapeLink videoId=${videoId},userName=${userName}")
        return withContext(Dispatchers.IO) {
            val url = "https://www.facebook.com/$userName/videos/${videoId.replace("/", "")}/?=null"
            Log.d(TAG, "scrapeLink Full Url = $url")
            val response = UrlParserNetworkClient.makeNetworkRequestString(
                url = url, requestType = ParserRequestTypes.Get, headers = mapOf(
                    "Cookie" to "sb=5ZA9Zy8EyAPTVLLqGVnuMMqR",
                    "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.89 Safari/537.36",
                    "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9"
                )
            )
            val result = response.data ?: ""

            val title = result.getTitleFromHtml()

            val thumbnail = result.substringAfter("\"image\":{\"uri\":\"").substringBefore("\"")
                .replace("amp;", "")
                .removeUnnecessarySlashes()

            val hdUrl = "http" + result.substringAfter("browser_native_hd_url")
                .substringAfter("http")
                .substringBefore("\",")
                .removeUnnecessarySlashes()
                .replace("amp;", "")
            val sdUrl = "http" + result.substringAfter("browser_native_sd_url")
                .substringAfter("http")
                .substringBefore("\",")
                .removeUnnecessarySlashes()
                .replace("amp;", "")
//            val sdUrl = result.substringAfter("browser_native_sd_url\":\"").substringBefore("\",")
//                .removeUnnecessarySlashes()
//                .replace("amp;", "")

            Log.d(TAG, "scrapeLink: title=$title")
            Log.d(TAG, "scrapeLink: thumbnail=$thumbnail")
            Log.d(TAG, "scrapeLink: hdUrl=$hdUrl")
            Log.d(TAG, "scrapeLink: sdUrl=$sdUrl")
            val qualities = mutableListOf<ParsedQuality>()
            if (hdUrl.isNotBlank()) {
                qualities.add(
                    ParsedQuality(
                        name = "HD", url = hdUrl
                    )
                )
            }
            if (sdUrl.isNotBlank()) {
                qualities.add(
                    ParsedQuality(
                        name = "SD", url = sdUrl
                    )
                )
            }
            if (qualities.isNotEmpty()) {
                ParsedVideo(
                    title = title,
                    qualities = qualities,
                    mediaType = MediaTypeData.Video,
                    thumbnail = thumbnail
                )
            } else {
                null
            }
        }
    }
}