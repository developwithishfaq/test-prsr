package com.adm.url_parser.impls.main_sites.insta.impl

import android.util.Log
import com.adm.url_parser.commons.network.ParserRequestTypes
import com.adm.url_parser.commons.network.UrlParserNetworkClient
import com.adm.url_parser.commons.network.UrlParserNetworkResponse
import com.adm.url_parser.impls.main_sites.insta.InstaCommons.purifyFrom00253D
import com.adm.url_parser.impls.main_sites.insta.InstaCommons.purifyInstagramUrl
import com.adm.url_parser.interfaces.ApiLinkScrapperForSubImpl
import com.adm.url_parser.models.MediaTypeData
import com.adm.url_parser.models.ParsedQuality
import com.adm.url_parser.models.ParsedVideo

class InstagramDirectUrlApi(
) : ApiLinkScrapperForSubImpl {
    private val TAG = "InstagramDirectUrlApi"

    override suspend fun scrapeLink(url: String): Result<ParsedVideo?> {
        val newUrl = url.purifyInstagramUrl()
        Log.d(TAG, " insta html scrapeVideos Link: $url\nNew Url = $newUrl")
        val response = UrlParserNetworkClient.makeNetworkRequestString(
            url = newUrl,
            requestType = ParserRequestTypes.Get
        )
        return if (response is UrlParserNetworkResponse.Success) {
            val res = response.data ?: ""
            val clips = res.split("\"clips\\\"")
            val videos = mutableListOf<ParsedQuality>()
            val links = HashMap<String, String>()
            var newIndex = 0
            clips.forEachIndexed { index, cli ->
                val raw =
                    cli.substringAfter("\\\"video_url\\\":\\\"").substringBefore("\\\",\\\"")
                val text = raw.replace("\\", "").purifyFrom00253D()
                Log.d(TAG, "insta html scrapeVideos ex: $text")
                if (text.startsWith("http") && links[text] == null) {
                    links[text] = text
                    videos.add(
                        ParsedQuality(
                            name = if (newIndex == 0) {
                                "HD"
                            } else if (newIndex == 1) {
                                "SD"
                            } else {
                                "HD"
                            },
                            url = text,
                            mediaType = MediaTypeData.Video
                        )
                    )
                    newIndex += 1
                }
            }
            Log.d(TAG, "scrapeVideos insta size:${videos.size} ")
            Log.d(TAG, "scrapeVideos insta qualities:${videos} ")
            if (videos.size <= 0) {
                Result.failure(Exception("No Qualities Found in InstagramDirectUrlApi"))
            } else {
                Result.success(
                    ParsedVideo(
                        title = "Insta_${System.currentTimeMillis().toString().takeLast(5)}",
                        qualities = videos
                    )
                )
            }
        } else {
            Log.d(TAG, "scrapeVideos insta html : $response")
            Result.failure(Exception("InstagramDirectUrlApi is not hitting exception is ${response.error}"))
        }
    }
}