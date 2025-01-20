package com.adm.url_parser.impls.main_sites.insta.impl

import android.util.Log
import com.adm.url_parser.commons.getJsonObjectSafe
import com.adm.url_parser.commons.network.UrlParserNetworkClient
import com.adm.url_parser.impls.main_sites.insta.InstaCommons.getInstagramUrlId
import com.adm.url_parser.impls.main_sites.insta.InstaCommons.getStringSafe
import com.adm.url_parser.impls.main_sites.insta.Variables
import com.adm.url_parser.interfaces.ApiLinkScrapperForSubImpl
import com.adm.url_parser.models.MediaTypeData
import com.adm.url_parser.models.ParsedQuality
import com.adm.url_parser.models.ParsedVideo
import io.ktor.client.HttpClient
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.InternalAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONObject

class InstaGraphQlScrapper(
) : ApiLinkScrapperForSubImpl {

    private val TAG = "InstaGraphQlScrapper"

    override suspend fun scrapeLink(url: String): ParsedVideo? {
        return withContext(Dispatchers.IO) {
            Log.d("apiImpl", "scrapeVideos:$url")
            val videoId = url.getInstagramUrlId()
            try {
                val response = UrlParserNetworkClient.makeNetworkRequestStringXXForm(
                    url = "https://www.instagram.com/graphql/query",
                    headers = mapOf(
                        HttpHeaders.Cookie to "csrftoken=KfUBze2TeAG0H4FrGFi0B2; csrftoken=DQXjFKuLZhp53agTq3S7hS; ig_did=9B0E8882-43CC-49CF-8596-28E549D03E6E; ig_nrcb=1; mid=Zz2B8AAEAAHWk0oIY1pMO4AGCKvh",
                        HttpHeaders.ContentType to "application/json",
                        HttpHeaders.Referrer to "https://www.instagram.com/p/$videoId/",
                        HttpHeaders.UserAgent to "Mozilla/5.0 (Linux; U; Android 9; en-us; SM-G988N Build/JOP24G) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/86.0.4240.198 Mobile Safari/537.36"
                    ),
                    formData = mapOf(
                        "doc_id" to "8845758582119845",
                        "variables" to Json.encodeToString(Variables(videoId))
                    )
                ).data ?: ""
                Log.d("apiImpl", "scrapeVideos data:$response ")
                val data = JSONObject(response)
                val dataObject = data.getJSONObject("data")
                val mediaObject = dataObject.getJSONObject("xdt_shortcode_media")
                val thumbnail = mediaObject.getString("thumbnail_src")
                val caption = mediaObject.getString("accessibility_caption")

                var isVideo = false

                val qualities = mutableListOf<ParsedQuality>()
                val nodes =
                    mediaObject.getJsonObjectSafe("edge_sidecar_to_children")?.getJSONArray("edges")
                if (nodes != null) {
                    for (i in 0 until nodes.length()) {
                        val node = nodes.getJSONObject(i)
                        val nodeData = node.getJSONObject("node")
                        val itemUrl = nodeData.getString("display_url")
                        val name = "Media_${i + 1}"
                        isVideo = nodeData.getBoolean("is_video")
                        qualities.add(
                            ParsedQuality(
                                name = name,
                                url = itemUrl
                            )
                        )
                    }
                } else {
                    val videoUrl = mediaObject.getStringSafe("video_url")
                    val displayUrl = mediaObject.getStringSafe("display_url")

                    val name = "HD"
                    if (videoUrl != null) {
                        isVideo = true
                        qualities.add(
                            ParsedQuality(
                                name = name,
                                url = videoUrl
                            )
                        )
                    } else if (displayUrl != null) {
                        isVideo = false
                        qualities.add(
                            ParsedQuality(
                                name = name,
                                url = displayUrl
                            )
                        )
                    }
                }
                if (qualities.isNotEmpty()) {
                    ParsedVideo(
                        title = caption,
                        thumbnail = thumbnail,
                        qualities = qualities,
                        mediaType = if (isVideo) {
                            MediaTypeData.Video
                        } else {
                            MediaTypeData.Image
                        }
                    )

                } else {
                    null
                }
            } catch (e: Exception) {
                Log.d(TAG, "scrapeLink: ")
                null
            }
        }
    }


}