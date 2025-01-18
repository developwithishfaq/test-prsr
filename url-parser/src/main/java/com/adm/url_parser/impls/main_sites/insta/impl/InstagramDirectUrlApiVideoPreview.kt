package com.adm.url_parser.impls.main_sites.insta.impl

import android.util.Log
import com.adm.url_parser.commons.Commons.removeUnnecessarySlashes
import com.adm.url_parser.commons.network.ParserRequestTypes
import com.adm.url_parser.commons.network.UrlParserNetworkClient
import com.adm.url_parser.commons.network.UrlParserNetworkResponse
import com.adm.url_parser.impls.main_sites.insta.InstaCommons.purifyFrom00253D
import com.adm.url_parser.impls.main_sites.insta.InstaCommons.purifyInstagramUrl
import com.adm.url_parser.interfaces.ApiLinkScrapperForSubImpl
import com.adm.url_parser.models.MediaTypeData
import com.adm.url_parser.models.ParsedQuality
import com.adm.url_parser.models.ParsedVideo

class InstagramDirectUrlApiVideoPreview(
) : ApiLinkScrapperForSubImpl {
    private val TAG = "InstagramDirectUrlApiVideoPreview"

    override suspend fun scrapeLink(url: String): ParsedVideo? {
        val newUrl = url.purifyInstagramUrl(false)
        Log.d(TAG, " insta html scrapeVideos Link: $url\nNew Url = $newUrl")
        val response = UrlParserNetworkClient.makeNetworkRequestString(
            url = newUrl,
            requestType = ParserRequestTypes.Get,
            headers = mapOf(
                "Sec-Fetch-Mode" to "navigate",
                "Sec-Fetch-Dest" to "document",
                "x-requested-with" to "com.android.browser",
                "Upgrade-Insecure-Requests" to "1",
                "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9",
                "User-Agent" to "Mozilla/5.0 (Linux; U; Android 9; en-us; SM-G988N Build/JOP24G) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/86.0.4240.198 Mobile Safari/537.36",
                "Cookie" to "csrftoken=MuobrszuFzx0mhnd0ERpvp; datr=Xv-IZ1M6UgYV-cFJVnCqOlHq; ig_did=A7A681DD-2A45-4641-B645-76B0DDB6A054; dpr=1.5; mid=Z4j_XwABAAHO8JbQLK7gVnxS0-QL; wd=480x774; csrftoken=DQXjFKuLZhp53agTq3S7hS; mid=Zz2B8AAEAAHWk0oIY1pMO4AGCKvh"
            )
        )
        return if (response is UrlParserNetworkResponse.Success) {
            val res = response.data ?: ""
            val imageVersions =
                res.substringAfterLast("\"image_versions2\":").substringBefore("},\"")

            val thumbnail = imageVersions.substringAfter("\"url\":\"")
                .substringBefore("\",\"")
                .removeUnnecessarySlashes()
                .purifyFrom00253D()
            val caption = res.substringAfter("caption\":{\"text\":\"").substringBefore("\",")
            val videoVersions = res.substringAfterLast("video_versions\":").substringBefore("}],\"")
            val videoUrl = videoVersions.substringAfter("\"url\":\"").substringBefore("\"")
                .purifyFrom00253D()
                .removeUnnecessarySlashes()
                .replace("\\","")
//                .removeUnnecessarySlashes()
//                .purifyFrom00253D()

            Log.d(TAG, "Is caption:${res.contains("caption")} ")
            Log.d(TAG, "Is Videos:${res.contains("video_versions")} ")
            Log.d(TAG, "Is thumbnail:${res.contains("image_versions2")} ")
            Log.d(TAG, "Video Url:${videoUrl}")
            ParsedVideo(
                title = caption,
                thumbnail = thumbnail,
                duration = "",
                mediaType = MediaTypeData.Video,
                qualities = listOf(
                    ParsedQuality(
                        name = "HD",
                        url = videoUrl
                    )
                )
            )
        } else {
            Log.d(TAG, "scrapeVideos insta html : $response")
            null
        }
    }
}