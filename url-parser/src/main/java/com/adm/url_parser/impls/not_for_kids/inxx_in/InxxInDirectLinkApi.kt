package com.adm.url_parser.impls.not_for_kids.inxx_in

import android.util.Log
import com.adm.url_parser.commons.network.ParserRequestTypes
import com.adm.url_parser.commons.network.UrlParserNetworkClient
import com.adm.url_parser.commons.network.UrlParserNetworkResponse
import com.adm.url_parser.interfaces.ApiLinkScrapper
import com.adm.url_parser.models.MediaTypeData
import com.adm.url_parser.models.ParsedQuality
import com.adm.url_parser.models.ParsedVideo

class InxxInDirectLinkApi : ApiLinkScrapper {
    private val TAG = "InxxInDirectLinkApi"
    override suspend fun scrapeLink(url: String): Result<ParsedVideo?> {
        val responseOfNetwork = UrlParserNetworkClient.makeNetworkRequestString(
            url = url,
            requestType = ParserRequestTypes.Get,
            headers = mapOf(
                "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36",
                "Cookie" to "kt_tcookie=1; PHPSESSID=c1q4999sodo9c9maqp2dsq0aij; kt_ips=51.158.61.253; kt_vast_450304=2dbc861c4a2f889a23abd299378b9dec; _ga_12N02X2VGM=GS1.1.1736787951.3.1.1736787972.39.0.0; _ga=GA1.2.482862200.1736689019; _gid=GA1.2.1219859049.1736787994"
            )
        )
        if (responseOfNetwork is UrlParserNetworkResponse.Failure) {
            return Result.failure(Exception("InxxInDirectLinkApi is not hitting exception is ${responseOfNetwork.error}"))
        }
        val response = responseOfNetwork.data ?: ""
        val thumbnail = response.substringAfter("preview_url: '").substringBefore("',")
        val link = response.substringAfter("video_url: '").substringBefore("',")
        val title = response.substringAfter("video_title: '").substringBefore("',")
        val quality = response.substringAfter("video_url_text: '").substringBefore("',")
        Log.d(TAG, "link=${link}")
        Log.d(TAG, "Data=${response}")
        return if (response.isNotBlank() && link.isNotBlank()) {
            Result.success(

                ParsedVideo(
                    title = title,
                    thumbnail = thumbnail,
                    qualities = listOf(
                        ParsedQuality(
                            url = link,
                            name = quality,
                            mediaType = MediaTypeData.Video
                        )
                    )
                )
            )
        } else {
            Result.failure(Exception("Response or Link is blank in InxxInDirectLinkApi"))
        }
    }
}