package com.adm.url_parser.commons.impl

import com.adm.url_parser.interfaces.ApiLinkScrapper
import com.adm.url_parser.models.MediaTypeData
import com.adm.url_parser.models.ParsedQuality
import com.adm.url_parser.models.ParsedVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class FetchLinksFromHTML5Player : ApiLinkScrapper {
    override suspend fun scrapeLink(url: String): Result<ParsedVideo?> {
        return withContext(Dispatchers.IO) {
            try {
                val document = Jsoup.connect(url)
                    .userAgent("Mozilla")
                    .timeout(15000)
                    .get()

                var video: ParsedVideo? = null

                val scripts = document.select("script")
                // Find the script containing 'html5player'
                for (script in scripts) {
                    val scriptContent = script.data()
                    if (scriptContent.contains("html5player")) {
                        println("Script Found: \n$scriptContent")

                        // Extract values using Regex
                        val videoTitleRegex =
                            """html5player\.setVideoTitle\(['"](.*?)['"]\)""".toRegex()
                        val videoThumbnailRegex =
                            """html5player\.setThumbUrl\(['"](.*?)['"]\)""".toRegex()
                        val videoUrlRegex =
                            """html5player\.setVideoHLS\(['"](.*?)['"]\)""".toRegex()
                        val uploaderRegex =
                            """html5player\.setUploaderName\(['"](.*?)['"]\)""".toRegex()

                        val videoTitle =
                            videoTitleRegex.find(scriptContent)?.groupValues?.get(1) ?: "Not Found"
                        val videoThumbnail =
                            videoThumbnailRegex.find(scriptContent)?.groupValues?.get(1)
                                ?: "Not Found"
                        val videoUrl =
                            videoUrlRegex.find(scriptContent)?.groupValues?.get(1) ?: "Not Found"
                        val uploader =
                            uploaderRegex.find(scriptContent)?.groupValues?.get(1) ?: "Not Found"

                        video = ParsedVideo(
                            title = videoTitle,
                            thumbnail = videoThumbnail,
                            qualities = listOf(
                                ParsedQuality(
                                    url = videoUrl,
                                    name = "HD",
                                    mediaType = MediaTypeData.Video
                                )
                            )
                        )

                        println("Video Title: $videoTitle")
                        println("Video URL: $videoUrl")
                        println("Uploader: $uploader")
                        break
                    }
                }
                Result.success(video)
            } catch (e: Exception) {
                Result.failure(Exception("Exception ${e.message}"))
            }
        }
    }
}