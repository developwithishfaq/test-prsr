package com.adm.url_parser.commons.impl.get_in_device_api

import android.util.Log
import com.adm.url_parser.commons.impl.get_in_device_api.model.GetInDeviceResponse
import com.adm.url_parser.commons.network.UrlParserNetworkClient
import com.adm.url_parser.interfaces.ApiLinkScrapper
import com.adm.url_parser.models.MediaTypeData
import com.adm.url_parser.models.ParsedQuality
import com.adm.url_parser.models.ParsedVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetInDeviceApiImpl : ApiLinkScrapper {
    private val TAG = "GetInDeviceApiImpl"
    override suspend fun scrapeLink(url: String): ParsedVideo? {
        Log.d(TAG, "scrapeLink: $url")
        return withContext(Dispatchers.IO) {
            val response = UrlParserNetworkClient.makeNetworkRequestXXForm<GetInDeviceResponse>(
                url = "https://www.getindevice.com/wp-json/aio-dl/video-data",
                formData = mapOf(
                    "url" to url
                ),
            )
            val data = response.data
            Log.d(TAG, "scrapeLink data = $data")
            val list = data?.medias ?: emptyList()
            val qualities = mutableListOf<ParsedQuality>()
            val thumbnail = data?.thumbnail
            list.forEach { media ->
                if (media.quality != "audio") {
                    val mediaType = if (media.videoAvailable == true) {
                        MediaTypeData.Video
                    } else if (media.audioAvailable == true) {
                        MediaTypeData.Audio
                    } else {
                        MediaTypeData.Image
                    }
                    Log.d(TAG, "scrapeLink Quality = ${media.quality}")
                    Log.d(TAG, "scrapeLink Type = $mediaType")
                    Log.d(TAG, "scrapeLink Url = ${media.url}")
                    Log.d(TAG, "--------------------------------")
                    qualities.add(
                        ParsedQuality(
                            url = media.url ?: "",
                            name = media.quality ?: "",
                            mediaType = mediaType,
                            size = media.size.toLong()
                        )
                    )
                }
            }

            Log.d(TAG, "scrapeLink Qualities Size = ${qualities.size}")

            if (qualities.isNotEmpty()) {
                ParsedVideo(
                    qualities = qualities,
                    title = response.data?.title ?: "",
                    thumbnail = thumbnail,
                    duration = data?.duration?.toLongOrNull()
                )
            } else {
                null
            }
        }
    }
}