package com.adm.url_parser.impls.main_sites.insta.impl

import android.util.Log
import com.adm.url_parser.commons.getJsonArraySafe
import com.adm.url_parser.commons.getJsonObjectSafe
import com.adm.url_parser.commons.network.ParserRequestTypes
import com.adm.url_parser.commons.network.UrlParserNetworkClient
import com.adm.url_parser.impls.main_sites.insta.InstaCommons.extractReelId
import com.adm.url_parser.impls.main_sites.insta.InstaCommons.purifyInstagramUrl
import com.adm.url_parser.impls.main_sites.insta.Variables
import com.adm.url_parser.interfaces.ApiLinkScrapperForSubImpl
import com.adm.url_parser.models.MediaTypeData
import com.adm.url_parser.models.ParsedQuality
import com.adm.url_parser.models.ParsedVideo
import kotlinx.serialization.encodeToString
import org.json.JSONObject

class InstaGraphQlApi : ApiLinkScrapperForSubImpl {
    private val TAG = "InstaGraphQlApi"
    override suspend fun scrapeLink(url: String): ParsedVideo? {
        val newInstaUrl = url.purifyInstagramUrl()
        log("newInstaUrl = ${newInstaUrl}")
        val reelId = newInstaUrl.extractReelId() ?: url
        val formData = mapOf(
            "doc_id" to "25531498899829322",
            "variables" to UrlParserNetworkClient.getJsonHelperr().encodeToString(Variables(reelId))
        )
        log("id=${reelId}\nURL=${url}")
        val response = UrlParserNetworkClient.makeNetworkRequestString(
            url = "https://www.instagram.com/graphql/query", formData = formData, headers = mapOf(
                "Cookie" to "csrftoken=DQXjFKuLZhp53agTq3S7hS; ig_did=5E7CFA4D-0A3B-47D5-8CB8-A2D37C00B2FE; ig_nrcb=1; mid=ZnRWwQAEAAGAhJ61ZqPzuYivBk6f"
            ),
            requestType = ParserRequestTypes.Post(null)
        )
        val responseData = response.data ?: ""
        Log.d(TAG, "startScrapping Response:${response} ")
        Log.d(TAG, "startScrapping Response Data:${responseData} ")

        try{

            val data = JSONObject(responseData)

            val extractedData = data.getJSONObject("data").getJSONObject("xdt_shortcode_media")
            val isVideo = extractedData.getBoolean("is_video")
            val displayUrl = extractedData.getString("display_url")


            val videoModel: ParsedVideo? = if (isVideo) {
                val videoUrl = extractedData.getString("video_url")
                log("Extracted Data =$extractedData")
                val videoDuration = extractedData.getString("video_duration")
                val videoName = try {
                    val array = extractedData.getJSONObject("edge_media_to_caption")
                        .getJSONArray("edges")
                    if (array.length() > 0) {
                        array.getJSONObject(0).getJSONObject("node").getString("text")
                    } else {
                        "instagram_${System.currentTimeMillis()}"
                    }
                } catch (_: Exception) {
                    "instagram_${System.currentTimeMillis()}"
                }
                ParsedVideo(
                    title = videoName,
                    thumbnail = displayUrl,
                    duration = "$videoDuration sec",
                    qualities = listOf(
                        ParsedQuality(
                            url = videoUrl,
                            name = "Default"
                        )
                    ),
                    mediaType = MediaTypeData.Video
                )
            } else {
                val isMultiplePhotos: JSONObject? =
                    extractedData.getJsonObjectSafe("edge_sidecar_to_children")
                val caption = extractedData.getString("accessibility_caption")
                Log.d(TAG, "insta graphQl isMultiplePhotos=${isMultiplePhotos}")
                if (isMultiplePhotos != null) {
                    val photosList = isMultiplePhotos.getJsonArraySafe("edges")
                    photosList?.let {
                        val dataList = mutableListOf<ParsedVideo>()
                        val qualities = mutableListOf<ParsedQuality>()
                        for (item in 0 until photosList.length()) {
                            val mainNode = photosList.getJSONObject(item).getJSONObject("node")
                            val isThatVideo = mainNode.getBoolean("is_video")
                            val newUrl = if (isThatVideo) {
                                mainNode.getString("video_url")
                            } else {
                                mainNode.getString("display_url")
                            }
                            val videoName = if (isThatVideo) {
                                extractedData.getJSONObject("edge_media_to_caption")
                                    .getJSONArray("edges").getJSONObject(0)
                                    .getJSONObject("node").getString("text")
                            } else {
                                caption
                            }
                            Log.d(TAG, "insta graphQl isThatVideo:${isThatVideo}")
                            qualities.add(
                                ParsedQuality(
                                    url = newUrl,
                                    name = "Image ${qualities.size + 1}"
                                )
                            )
                            dataList.add(
                                ParsedVideo(
                                    title = videoName,
                                    thumbnail = newUrl,
                                    mediaType = if (isThatVideo) {
                                        MediaTypeData.Video
                                    } else {
                                        MediaTypeData.Image
                                    },
                                    qualities = emptyList()
                                )
                            )
                        }
                        dataList.getOrNull(0)?.copy(
                            qualities = qualities
                        )
                    }
                } else {
                    Log.d(TAG, "insta graphQl isMultiplePhotos=null")
                    ParsedVideo(
                        title = caption,
                        thumbnail = displayUrl,
                        mediaType = MediaTypeData.Image,
                        qualities = listOf(
                            ParsedQuality(
                                url = displayUrl
                            )
                        )
                    )
                }

            }
            return videoModel
        }catch (_:Exception){
            return null
        }
    }

    fun log(message: String) {
        Log.d(TAG, ":$message")
    }
}