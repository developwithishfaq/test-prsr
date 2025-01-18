package com.adm.url_parser.commons.impl

import com.adm.url_parser.models.MediaTypeData
import com.adm.url_parser.models.ParsedVideo
import com.adm.url_parser.interfaces.ApiLinkScrapper
import com.adm.url_parser.models.ParsedQuality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class FetchLinksFromPreloadTag : ApiLinkScrapper {
    override suspend fun scrapeLink(url: String): ParsedVideo? {
        return withContext(Dispatchers.IO) {
            val document = Jsoup.connect(url)
                .userAgent("Mozilla")
                .timeout(15000)
                .get()

            val videoTitle = document.title()
            val thumbnail = document.selectFirst("meta[property=og:image]")?.attr("content")
            val linkElement = document.select("link[rel=preload]").first()
            val m3u8Link = linkElement?.attr("href") ?: ""
            if (m3u8Link.isNotBlank()) {
                ParsedVideo(
                    title = videoTitle,
                    thumbnail = thumbnail,
                    qualities = listOf(
                        ParsedQuality(
                            url = m3u8Link,
                            name = "HD"
                        )
                    ),
                    mediaType = MediaTypeData.Video
                )
            } else {
                null
            }
        }
    }
}