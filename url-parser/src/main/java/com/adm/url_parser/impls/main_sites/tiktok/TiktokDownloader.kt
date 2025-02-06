package com.adm.url_parser.impls.main_sites.tiktok

import com.adm.url_parser.commons.network.ParserRequestTypes
import com.adm.url_parser.commons.network.UrlParserNetworkClient
import com.adm.url_parser.interfaces.ApiLinkScrapperForSubImpl
import com.adm.url_parser.models.MediaTypeData
import com.adm.url_parser.models.ParsedQuality
import com.adm.url_parser.models.ParsedVideo

class TiktokDownloader : ApiLinkScrapperForSubImpl {
    override suspend fun scrapeLink(url: String): ParsedVideo? {
        val videoUrl = "https://www.tikwm.com/api/?url=$url&hd=1"
        val response = UrlParserNetworkClient.makeNetworkRequest<Tiktok>(
            url = videoUrl,
            requestType = ParserRequestTypes.Get,
        )
        val data = response.data?.data
        val title = data?.title
        val qualities = listOf(
            ParsedQuality(
                name = "SD",
                url = data?.play ?: "",
                size = data?.size?.toLong(),
                mediaType = MediaTypeData.Video
            ),
            ParsedQuality(
                name = "Watermark",
                url = data?.wmplay ?: "",
                size = data?.wm_size?.toLong(),
                mediaType = MediaTypeData.Video
            ),
            ParsedQuality(
                name = "HD",
                url = data?.hdplay ?: "",
                size = data?.hd_size?.toLong(),
                mediaType = MediaTypeData.Video
            ),
        ).filter {
            it.url.isNotBlank()
        }
        val thumbnail1 = response.data?.data?.ai_dynamic_cover
        val thumbnail2 = response.data?.data?.origin_cover
        val duration = response.data?.data?.duration ?: 0
        return if (qualities.isNotEmpty()) {
            ParsedVideo(
                qualities = qualities,
                title = title ?: "",
                thumbnail = thumbnail1 ?: thumbnail2 ?: "",
                duration = (duration * 60 * 1000).toLong()
            )
        } else {
            null
        }
    }
}
