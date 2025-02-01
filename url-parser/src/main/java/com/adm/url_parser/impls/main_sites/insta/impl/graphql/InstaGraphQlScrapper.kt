package com.adm.url_parser.impls.main_sites.insta.impl.graphql

import android.util.Log
import com.adm.url_parser.commons.getAtSafe
import com.adm.url_parser.commons.getJsonArraySafe
import com.adm.url_parser.commons.getJsonObjectSafe
import com.adm.url_parser.commons.network.UrlParserNetworkClient
import com.adm.url_parser.impls.main_sites.insta.InstaCommons.getInstagramUrlId
import com.adm.url_parser.impls.main_sites.insta.InstaCommons.getStringSafe
import com.adm.url_parser.impls.main_sites.insta.Variables
import com.adm.url_parser.interfaces.ApiLinkScrapperForSubImpl
import com.adm.url_parser.models.MediaTypeData
import com.adm.url_parser.models.ParsedQuality
import com.adm.url_parser.models.ParsedVideo
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONObject

class InstaGraphQlScrapper(
    private val graphQlConfigs: GraphQlConfigs
) : ApiLinkScrapperForSubImpl {

    private val TAG = "InstaGraphQlScrapper"

    override suspend fun scrapeLink(url: String): ParsedVideo? {
        return withContext(Dispatchers.IO) {
            Log.d(TAG, "scrapeVideos:$url")
            val videoId = url.getInstagramUrlId()
            try {
                val response = UrlParserNetworkClient.makeNetworkRequestStringXXForm(
                    url = "https://www.instagram.com/graphql/query",
                    headers = graphQlConfigs.getHeaders(url, videoId) + mapOf(
                        HttpHeaders.Referrer to "https://www.instagram.com/p/$videoId/"
                    ),
                    formData = mapOf(
                        "doc_id" to graphQlConfigs.getDocId(),
                        "variables" to Json.encodeToString(Variables(videoId))
                    )
                ).data ?: ""
                Log.d(TAG, "scrapeVideos data:$response ")
                val data = JSONObject(response)
                val dataObject = data.getJSONObject("data")
                val mediaObject = dataObject.getJSONObject("xdt_shortcode_media")
                val thumbnail = mediaObject.getStringSafe("thumbnail_src")
                val caption = mediaObject.getCaption()

                val qualities = mutableListOf<ParsedQuality>()
                val nodes =
                    mediaObject.getJsonObjectSafe("edge_sidecar_to_children")
                        ?.getJsonArraySafe("edges")
                if (nodes != null) {
                    for (i in 0 until nodes.length()) {
                        val node = nodes.getJSONObject(i)
                        val nodeData = node.getJSONObject("node")
                        val itemUrl = nodeData.getString("display_url")
                        val videoUrl = nodeData.getStringSafe("video_url")
                        val isVideo = nodeData.has("video_url") && videoUrl.isNullOrBlank().not()
                        val name = "Media_${i + 1}"
                        qualities.add(
                            ParsedQuality(
                                name = name,
                                url = videoUrl ?: itemUrl,
                                mediaType = if (isVideo) {
                                    MediaTypeData.Video
                                } else {
                                    MediaTypeData.Image
                                }
                            )
                        )
                    }
                } else {
                    val videoUrl = mediaObject.getStringSafe("video_url")
                    val displayUrl = mediaObject.getStringSafe("display_url")
                    val displayQualities = mediaObject.getJsonArraySafe("display_resources")
                    val isQualitiesAvailable = (displayQualities?.length() ?: 0) > 0

                    val name = "HD"
                    if (videoUrl != null) {
                        qualities.add(
                            ParsedQuality(
                                name = name,
                                url = videoUrl,
                                mediaType = MediaTypeData.Video
                            )
                        )
                    } else if (displayUrl != null) {
                        if (isQualitiesAvailable) {
                            repeat(displayQualities?.length() ?: 0) {
                                val quality = displayQualities?.getJsonObjectSafe(it)
                                if (quality != null) {
                                    val link = quality.getStringSafe("src")
                                    val width = quality.getStringSafe("config_width")
                                    val height = quality.getStringSafe("config_height")
                                    qualities.add(
                                        ParsedQuality(
                                            url = link ?: "",
                                            name = width ?: "HD",
                                            mediaType = MediaTypeData.Image
                                        )
                                    )
                                }
                            }
                        } else {
                            qualities.add(
                                ParsedQuality(
                                    name = "Image",
                                    url = displayUrl,
                                    mediaType = MediaTypeData.Image
                                )
                            )
                        }
                    }
                }
                if (qualities.isNotEmpty()) {
                    ParsedVideo(
                        title = caption,
                        thumbnail = thumbnail,
                        qualities = qualities
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

    private fun JSONObject.getCaption(): String? {
        val caption = getStringSafe("accessibility_caption")
        return if (caption == null || caption == "null") {
            val text =
                getJsonObjectSafe("edge_media_to_caption")?.getJsonArraySafe("edges")?.getAtSafe(0)
                    ?.getJsonObjectSafe("node")?.getStringSafe("text")
            text
        } else {
            caption
        }
    }

}