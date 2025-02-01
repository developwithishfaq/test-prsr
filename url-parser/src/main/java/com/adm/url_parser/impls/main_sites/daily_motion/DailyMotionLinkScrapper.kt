package com.adm.url_parser.impls.main_sites.daily_motion

import com.adm.url_parser.interfaces.ApiLinkScrapper
import com.adm.url_parser.models.ParsedVideo

class DailyMotionLinkScrapper(
    private val dailyMotionMetaDataExtractor: ApiLinkScrapper
) : ApiLinkScrapper {
    override suspend fun scrapeLink(url: String): ParsedVideo? {
        val videoId = url.substringAfter("/video/").substringBefore("/")
        val metaDataUrl =
            "https://www.dailymotion.com/player/metadata/video/${videoId}?embedder=https%3A%2F%2Fwww.dailymotion.com%2Fpk&geo=1&player-id=x138o4&locale=en&dmV1st=c9d37c06-4f3d-4487-a8ce-8afd9a570772&dmTs=384695&is_native_app=0&app=com.videodownloader.mks&client_type=webapp&dmViewId=1ij0vegql8241eb7a77&parallelCalls=1"
        return dailyMotionMetaDataExtractor.scrapeLink(metaDataUrl)
    }
}