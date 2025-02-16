package com.adm.url_parser.impls.main_sites.tiktok.impl

import com.adm.url_parser.interfaces.ApiLinkScrapperForSubImpl
import com.adm.url_parser.models.ParsedVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DirectUrlTiktokApiImpl : ApiLinkScrapperForSubImpl {
    override suspend fun scrapeLink(url: String): ParsedVideo? {
        return withContext(Dispatchers.IO) {
//            https://vt.tiktok.com/ZSMYmPodU/
//            https://www.tiktok.com/@chumkpop/video/7460628377792220438?is_from_webapp=1&sender_device=pc
//            https://www.tikwm.com/video/media/play/7460628377792220438.mp4
            val videoId = url.substringAfter("video/").substringBefore("?is_from_webapp")
            val videoUrl = "https://www.tikwm.com/video/media/play/${videoId}.mp4"
//            val videoUrl = "https://www.tikwm.com/video/media/play/${videoId}.mp4"
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
        }
    }
}