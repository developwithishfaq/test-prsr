package com.adm.url_parser.impls.not_for_kids.porn_hub

import android.util.Log
import com.adm.url_parser.commons.Commons.getTitleFromHtml
import com.adm.url_parser.commons.Commons.removeUnnecessarySlashes
import com.adm.url_parser.commons.network.ParserRequestTypes
import com.adm.url_parser.commons.network.UrlParserNetworkClient
import com.adm.url_parser.interfaces.ApiLinkScrapper
import com.adm.url_parser.models.MediaTypeData
import com.adm.url_parser.models.ParsedQuality
import com.adm.url_parser.models.ParsedVideo

class PornHubDirectLinkApi : ApiLinkScrapper {
    private val TAG = "PornHubDirectLinkApi"
    override suspend fun scrapeLink(url: String): ParsedVideo? {
        val response =
            UrlParserNetworkClient.makeNetworkRequestString(
                url,
                requestType = ParserRequestTypes.Get
            ).data ?: ""
        val title = response.getTitleFromHtml()

        val allVideoLinks = response.split("\"videoUrl\":\"")
        val extractedLinks = mutableListOf<ParsedQuality>()
        allVideoLinks.forEach {
            if (it.startsWith("https:")) {
                val videoLinkAndQuality = it.substringBefore("},")
                val videoLink = videoLinkAndQuality.substringBeforeLast("\",\"quality\"")
                    .removeUnnecessarySlashes()
                val quality =
                    videoLinkAndQuality.substringAfterLast("\"quality\":\"").replace("\"", "")
                if (quality.startsWith("http").not()){
                    extractedLinks.add(
                        ParsedQuality(
                            url = videoLink,
                            name = quality
                        )
                    )
                }
            }
        }
        extractedLinks.forEach {
            Log.d(TAG, "Extracted(${it.name}):${it.url}")
        }

        val videoUrl = response.substringAfter("\"videoUrl\":\"").substringBefore("\",\"")
            .removeUnnecessarySlashes()

        val qualityNameAndMp4Id = videoUrl.substringBefore(".mp4/").substringAfterLast("/")
        val qualityName = qualityNameAndMp4Id.substringBeforeLast("_")
        Log.d(TAG, "qualityName-${qualityName}")
        Log.d(TAG, "videoUrl:$videoUrl ")

        return if (extractedLinks.isNotEmpty()) {
            ParsedVideo(
                title = title,
                thumbnail = null,
                qualities = extractedLinks,
                mediaType = MediaTypeData.Video
            )
        } else {
            null
        }
    }
}