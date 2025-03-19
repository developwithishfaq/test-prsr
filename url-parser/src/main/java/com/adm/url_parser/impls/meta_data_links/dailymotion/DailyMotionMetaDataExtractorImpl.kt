package com.adm.url_parser.impls.meta_data_links.dailymotion

import com.adm.url_parser.commons.network.ParserRequestTypes
import com.adm.url_parser.commons.network.UrlParserNetworkClient
import com.adm.url_parser.commons.network.UrlParserNetworkResponse
import com.adm.url_parser.impls.meta_data_links.dailymotion.models.DailyMotionMetaData
import com.adm.url_parser.interfaces.ApiLinkScrapper
import com.adm.url_parser.models.MediaTypeData
import com.adm.url_parser.models.ParsedQuality
import com.adm.url_parser.models.ParsedVideo

class DailyMotionMetaDataExtractorImpl : ApiLinkScrapper {
    override suspend fun scrapeLink(url: String): Result<ParsedVideo?> {
        return try {
            val response = UrlParserNetworkClient.makeNetworkRequest<DailyMotionMetaData>(
                url = url,
                requestType = ParserRequestTypes.Get
            )
            if (response is UrlParserNetworkResponse.Failure) {
                return Result.failure(Exception(response.error))
            }
            val data = response.data
            if (data != null) {
                val videoUrl = data.qualities?.auto?.getOrNull(0)?.url
                if (videoUrl != null) {
                    val thumbnail = if (data.thumbnails != null) {
                        with(data.thumbnails) {
                            `360` ?: `480` ?: `720` ?: `1080`
                        }
                    } else {
                        null
                    }
                    val duration = (data.duration?.toLong() ?: 0L) * 60 * 1000
                    Result.success(

                        ParsedVideo(
                            title = "DailyMotion",
                            qualities = listOf(
                                ParsedQuality(
                                    url = videoUrl,
                                    name = "HD",
                                    mediaType = MediaTypeData.Video
                                )
                            ),
                            thumbnail = thumbnail,
                            duration = duration,
                            headers = null
                        )
                    )
                } else {
                    Result.failure(Exception("Video Url Not Found in dailymotion meta deta json"))
                }
            } else {
                Result.failure(Exception("Daily motion meta data link is not responding"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Exception is ${e.message}"))
        }
    }
}