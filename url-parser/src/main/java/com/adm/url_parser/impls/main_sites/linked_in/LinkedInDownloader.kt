package com.adm.url_parser.impls.main_sites.linked_in

import com.adm.url_parser.models.ParsedQuality
import com.adm.url_parser.models.ParsedVideo
import com.adm.url_parser.commons.network.ParserRequestTypes
import com.adm.url_parser.commons.network.UrlParserNetworkClient
import com.adm.url_parser.interfaces.ApiLinkScrapper
import com.adm.url_parser.models.MediaTypeData

class LinkedInDownloader : ApiLinkScrapper {
    override suspend fun scrapeLink(url: String): ParsedVideo? {
        val baseUrl = "https://sdownloader.liforte.com/getvl2023"
        val headers = mapOf(
            "Content-Type" to "application/json; charset=utf-8",
            "Host" to "sdownloader.liforte.com",
            "lfcode" to "b060bf1a70504f6fc2a8f468e202908c",
            "pid" to "x2downloader.tubedownloader.downloaderforall.socialvideodownloader",
            "adeviceid" to "2066CB36385816AF12486BD7B7CE979E"
        )
        val response = UrlParserNetworkClient.makeNetworkRequest<List<LinkedIn>>(
            url = baseUrl, headers = headers, requestType = ParserRequestTypes.Post(
                body = LinkedInModel(
                    Url = url,
                    avc = 17
                )
            )
        )
        return response.data.toVideoData()
    }

    private fun List<LinkedIn>?.toVideoData(): ParsedVideo? {
        val qualities = mutableListOf<ParsedQuality>()
        var thumbnail = ""
        var title = ""
        this?.forEach {
            if (it.title != null || it.text == "Thumbnail") {
                thumbnail = it.link ?: ""
            } else if (it.link.isNullOrBlank().not()) {
                title = it.title.toString()
                qualities.add(ParsedQuality(it.link ?: "", it.text?.take(10) ?: ""))
            }
        }
        return if (qualities.isNotEmpty()) {
            ParsedVideo(
                qualities = qualities,
                mediaType = MediaTypeData.Video,
                thumbnail = thumbnail,
                title = title
            )
        } else {
            null
        }
    }

}