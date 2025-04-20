package com.adm.url_parser.impls.main_sites.tiktok.impl

import com.adm.url_parser.interfaces.ApiLinkScrapperForSubImpl
import com.adm.url_parser.models.ParsedVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DirectUrlTiktokApiImpl : ApiLinkScrapperForSubImpl {
    val DOWNLOAD_ORIGINAL_VIDEO_LINK = "https://www.tikwm.com/video/media/wmplay/"
    val DOWNLOAD_VIDEO_WITHOUT_WATERMARK = "https://www.tikwm.com/video/media/play/"
    val DOWNLOAD_VIDEO_WITHOUT_WATERMARK_HD = "https://www.tikwm.com/video/media/hdplay/"
    val SOURCE_IMAGEVIEW = "https://www.tikwm.com/video/cover/"
    override suspend fun scrapeLink(url: String): Result<ParsedVideo?> {
        return withContext(Dispatchers.IO) {


            val videoId = url.substringAfter("video/").substringBefore("?is_from_webapp")
                .substringBefore("?_t")

            val originalNoWaterMark = "${DOWNLOAD_VIDEO_WITHOUT_WATERMARK}$videoId.mp4"
            val original = "${DOWNLOAD_ORIGINAL_VIDEO_LINK}$videoId.mp4"
            val hdVideo = "${DOWNLOAD_VIDEO_WITHOUT_WATERMARK_HD}$videoId.mp4"
            val thumbnail = "${SOURCE_IMAGEVIEW}$videoId.mp4"

            Result.success(
                ParsedVideo(
                    qualities = listOf(
                        com.adm.url_parser.models.ParsedQuality(
                            name = "Original",
                            url = original,
                            size = null,
                            mediaType = com.adm.url_parser.models.MediaTypeData.Video
                        ),
                        com.adm.url_parser.models.ParsedQuality(
                            name = "HD Watermark",
                            url = hdVideo,
                            size = null,
                            mediaType = com.adm.url_parser.models.MediaTypeData.Video
                        ),
                        com.adm.url_parser.models.ParsedQuality(
                            name = "No Watermark",
                            url = originalNoWaterMark,
                            size = null,
                            mediaType = com.adm.url_parser.models.MediaTypeData.Video
                        )
                    ),
                    title = "Tiktok $videoId",
                    thumbnail = thumbnail,
                )
            )
        }
    }
}