package com.adm.url_parser.impls.main_sites.pinterest

import android.util.Log
import com.adm.url_parser.commons.network.UrlParserNetworkClient
import com.adm.url_parser.interfaces.ApiLinkScrapper
import com.adm.url_parser.models.MediaTypeData
import com.adm.url_parser.models.ParsedQuality
import com.adm.url_parser.models.ParsedVideo
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonObject

class PinterestSupportApi : ApiLinkScrapper {
    override suspend fun scrapeLink(url: String): Result<ParsedVideo?> {
        val pinId = if (url.contains("pin.it")) {
            extractPinIdByUrl(extractVideoIdBySharedLink(url) ?: "")
        } else {
            extractPinIdByUrl(url)
        }
        Log.d(TAG, "scrapeLink(id=${pinId}): $url")
        val response = fetchPinDetails(pinId ?: url)
        val model =
            UrlParserNetworkClient.jsonHelper.decodeFromString<PinterestGraphQlResponse?>(response)
        Log.d(TAG, "scrapeLink: model=$model")
        val videoModel = if (model != null) {
            val (downloadableUrl, type, title) = if (model.isVideo()) {
                Triple(model.getVideoUrl(), MediaTypeData.Video, "Pinterest Video")
            } else {
                Triple(model.getImageUrl(), MediaTypeData.Image, "Pinterest Image")
            }
            if (type == MediaTypeData.Image) {
                ParsedVideo(
                    title = model.getTitle() ?: title,
                    qualities = listOf(
                        ParsedQuality(
                            url = downloadableUrl ?: "",
                            mediaType = type,
                            name = "Image"
                        )
                    )
                )
            } else {
                ParsedVideo(
                    title = model.getTitle() ?: title,
                    qualities = listOf(
                        ParsedQuality(
                            url = downloadableUrl ?: "",
                            mediaType = type,
                            name = "HLS",
                        )
                    )
                )
            }
        } else {
            null
        }
        Log.d(TAG, "scrapeLink pinterest:$videoModel")
        return if (videoModel == null) {
            Result.failure(Exception("No video found in PinterestSupportApi"))
        } else {
            Result.success(videoModel)
        }
    }

    private val TAG = "PinterestSupport"
    private fun extractPinIdByUrl(url: String): String? {
        if (url.contains("--")) {
            return url.substringAfter("--").substringBefore("/")
        } else {
            val regex = """/pin/(\d+)""".toRegex()
            val matchResult = regex.find(url)
            return matchResult?.groups?.get(1)?.value
        }
    }

    private suspend fun extractVideoIdBySharedLink(url: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val response: HttpResponse = UrlParserNetworkClient.getClient().get(url)
                val responseText = response.bodyAsText()
                val id = responseText.substringAfter("https://www.pinterest.com/pin/")
                    .substringBefore("/")
                Log.d(TAG, "Extracted Id:$id")
                id
            } catch (_: Exception) {
                null
            }
        }
    }

    private suspend fun fetchPinDetails(pinId: String): String {
        Log.d(TAG, "fetchPinDetails: pinId=$pinId")
        return withContext(Dispatchers.IO) {
            val url = "https://www.pinterest.com/_/graphql/"

            val requestBody = UrlParserNetworkClient.jsonHelper.encodeToString(
                JsonObject.serializer(), buildJsonObject {
                    put(
                        "queryHash",
                        JsonPrimitive("09db395a558573aceb6f502723775a8cbcc59013be571476b3d8bfe6067cd904")
                    )
                    putJsonObject("variables") {
                        put("pinId", JsonPrimitive(pinId))
                        put("isAuth", JsonPrimitive(false))
                    }
                })

            val response: HttpResponse = UrlParserNetworkClient.getClient().post(url) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
                headers {
                    append("accept", "application/json")
                    append("accept-language", "en-US,en;q=0.9")
                    append("connection", "keep-alive")
                    append(
                        "cookie",
                        "csrftoken=61b97a3612072fb94b88068a6b1230c9; _pinterest_sess=YOUR_SESSION_HERE;"
                    )
                    append("host", "www.pinterest.com")
                    append("origin", "https://www.pinterest.com")
                    append("referer", "https://www.pinterest.com/")
                    append(
                        "sec-ch-ua",
                        "\"Android WebView\";v=\"135\", \"Not-A.Brand\";v=\"8\", \"Chromium\";v=\"135\""
                    )
                    append("sec-ch-ua-mobile", "?1")
                    append("sec-ch-ua-platform", "Android")
                    append("sec-fetch-dest", "empty")
                    append("sec-fetch-mode", "cors")
                    append("sec-fetch-site", "same-origin")
                    append(
                        "user-agent",
                        "Mozilla/5.0 (Linux; U; Android 9; en-us; SM-G977N Build/JOP24G) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/86.0.4240.198 Mobile Safari/537.36"
                    )
                    append("x-csrftoken", "61b97a3612072fb94b88068a6b1230c9")
                    append("x-pinterest-appstate", "active")
                    append("x-pinterest-graphql-name", "CloseupPageQuery")
                    append("x-pinterest-pws-handler", "www/ideas/[interest]/[id].js")
                    append("x-requested-with", "XMLHttpRequest")
                }
            }
            val responseText = response.bodyAsText()
            Log.d(TAG, "fetchPinDetails:${responseText.substringAfter("video")}")
            responseText
        }
    }
}

/*
PinterestGraphQlResponse(data=MainDataModel(v3GetPinQuery=V3Query(data=InnerV3Data(videos=null, images=ImageQl(url=https://i.pinimg.com/originals/3b/6b/48/3b6b4860ba83cdd641c6579a7dc4205a.jpg), autoAltText=mother's day gift idea with flowers and butterflies in a glass frame on a table, storyPinData=StoryPinData(pages=[StoryPage(blocks=[StoryBlocksModel(videoDataV2=VideoDataV2(videoListMobile=VideoListMobile(vHLSV3MOBILE=HLSV4(url=https://v1.pinimg.com/videos/iht/hls/2c/c1/e6/2cc1e65b65239e38bbe8d6d38499d0a2.m3u8))))])])))))

*/
