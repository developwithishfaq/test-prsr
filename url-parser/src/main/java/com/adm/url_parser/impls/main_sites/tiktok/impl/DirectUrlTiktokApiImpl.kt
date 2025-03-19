package com.adm.url_parser.impls.main_sites.tiktok.impl

import com.adm.url_parser.interfaces.ApiLinkScrapperForSubImpl
import com.adm.url_parser.models.ParsedVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DirectUrlTiktokApiImpl : ApiLinkScrapperForSubImpl {
    override suspend fun scrapeLink(url: String): Result<ParsedVideo?> {
        return withContext(Dispatchers.IO) {
            val videoId = url.substringAfter("video/").substringBefore("?is_from_webapp")
                .substringBefore("?_t")
            val videoUrl = "https://www.tikwm.com/video/media/play/${videoId}.mp4"
            Result.success(
                ParsedVideo(
                    qualities = listOf(
                        com.adm.url_parser.models.ParsedQuality(
                            name = "HD",
                            url = videoUrl,
                            size = null,
                            mediaType = com.adm.url_parser.models.MediaTypeData.Video
                        )
                    ),
                    title = "HD",
                    thumbnail = videoUrl,
                )
            )
        }
    }
}