package com.adm.url_parser.impls.not_for_kids.porn_hub

import android.util.Log
import com.adm.url_parser.models.ParsedQuality
import com.adm.url_parser.models.ParsedVideo
import com.adm.url_parser.commons.Commons.removeUnnecessarySlashes
import com.adm.url_parser.commons.network.ParserRequestTypes
import com.adm.url_parser.commons.network.UrlParserNetworkClient
import com.adm.url_parser.interfaces.ApiLinkScrapper
import com.adm.url_parser.models.MediaTypeData

/*
view-source:https://www.pornhub.com/view_video.php?viewkey=671659644d217
Bad
https://www.pornhub.com/video/get_media?s=eyJrIjoiYjJmYTNiNWE5Yzg3NmFmN2Q4MzQ5ZmRhN2U4NTk0YTdkM2ExMTY0ZDg5ZjM1NDkyNTU1YzVhOGJjM2MyMTlmNSIsInQiOjE3MzY2OTk5NjV9&v=671659644d217&e=0&t=p

view-source:https://www.pornhub.com/view_video.php?viewkey=671659644d217
Good
https://www.pornhub.com/video/get_media?s=eyJrIjoiNjE4NmNhNTQ0MDA3NzBiMjNhODY3MmIzYzFkZWNmODliZGEzNzg3NTJkODVhZDFjZjFlMGNjYTdhNjIxZTIzOSIsInQiOjE3MzY2OTM4NjB9&v=671659644d217&e=0&t=p
*/

class PornHubDirectLinkApiTest : ApiLinkScrapper {
    private val TAG = "PornHubDirectLinkApi"
    override suspend fun scrapeLink(url: String): ParsedVideo? {
        val response =
            UrlParserNetworkClient.makeNetworkRequestString(
                url,
                requestType = ParserRequestTypes.Get,
                headers = mapOf(
//                    "accept-encoding" to "gzip, deflate, br, zstd",
                    "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7",
                    "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36",
                )
            ).data ?: ""
        val apiLink =
            response.substringAfter("mediaDefinitions\":").substringBeforeLast("nextVideo\":{")
                .removeUnnecessarySlashes()
        Log.d(TAG, "scrapeLink:$response")
        Log.d(TAG, "scrapeLink Api Link = $apiLink")
        val apiResponse =
            UrlParserNetworkClient.makeNetworkRequest<List<PornHubApiResponse>>(
                apiLink,
                ParserRequestTypes.Get
            )
                .data ?: emptyList()
        val title = response.substringAfter("<title>").substringBefore("</title>")

        val qualities = apiResponse.map {
            ParsedQuality(
                url = it.videoUrl,
                name = it.format,
            )
        }
        return if (qualities.isNotEmpty()) {
            ParsedVideo(
                title = title,
                thumbnail = null,
                qualities = qualities,
                mediaType = MediaTypeData.Video
            )
        } else {
            null
        }
    }
}