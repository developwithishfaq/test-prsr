package com.adm.url_parser.impls.main_sites.twitter

import com.adm.url_parser.commons.network.ParserRequestTypes
import com.adm.url_parser.commons.network.UrlParserNetworkClient
import com.adm.url_parser.interfaces.ApiLinkScrapper
import com.adm.url_parser.models.MediaTypeData
import com.adm.url_parser.models.ParsedQuality
import com.adm.url_parser.models.ParsedVideo

class TwitterDownloader : ApiLinkScrapper {
    override suspend fun scrapeLink(url: String): ParsedVideo? {
        val id = (url.substringAfter("status/").substringBefore("/")) ?: ""

        val baseUrl = "https://tweeload.aculix.net/status/$id.json"

        val headers = mapOf(
            "Authorization" to "cKMQlY4jGCflOStlN3UfnWCxLQSb5GL7UPjPJ3jGS5fkno1Jaf"
        )
        val response = UrlParserNetworkClient.makeNetworkRequest<Twitter>(
            url = baseUrl,
            requestType = ParserRequestTypes.Get,
            headers = headers
        )

        val qualities =
            response.data?.tweet?.media?.videos?.getOrNull(0)?.video_urls?.mapNotNull { model ->
                if (model.url != null) {
                    ParsedQuality(
                        url = model.url,
                        name = model.url.substringAfter("vid/avc1/").substringBefore("x") + "p",
                        mediaType = MediaTypeData.Video
                    )
                } else {
                    null
                }
            } ?: emptyList()
        return if (qualities.isNotEmpty()) {
            ParsedVideo(
                qualities = qualities,
                title = response.data?.tweet?.text ?: "",
            )
        } else {
            null
        }
    }
}