package com.adm.url_parser.impls.main_sites.linked_in.impl

import android.util.Log
import com.adm.url_parser.commons.network.UrlParserNetworkClient
import com.adm.url_parser.interfaces.ApiLinkScrapperForSubImpl
import com.adm.url_parser.models.MediaTypeData
import com.adm.url_parser.models.ParsedQuality
import com.adm.url_parser.models.ParsedVideo

class ExpertPhpLinkedinScrapper : ApiLinkScrapperForSubImpl {
    override suspend fun scrapeLink(url: String): Result<ParsedVideo?> {
        val response = UrlParserNetworkClient.makeNetworkRequestStringXXForm(
            url = "https://www.expertsphp.com/twitter-video-downloader.php",
            formData = mapOf(
                "url" to url,
                "utm_medium" to "member_desktop",
                "rcm" to "ACoAACbslWMBNEhIdSLFUGqn_XdEndA-WRbzDq4"
            ),
            headers = mapOf(
//                "accept-encoding" to "gzip, deflate, br, zstd",
                "accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7",
                "upgrade-insecure-requests" to "1",
                "user-agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36",
                "Cookie" to "_ga_D1XX1R246W=GS1.1.1744823161.1.1.1744823161.0.0.0; _ga=GA1.2.1226854796.1744823162; _gid=GA1.2.1285539298.1744823163; _gat_gtag_UA_120752274_1=1; __gads=ID=6c325720771b3541:T=1744823163:RT=1744823163:S=ALNI_MZTjqLaaIFuuE1S63h093Z3pEV3_Q; __gpi=UID=00001080a2958c24:T=1744823163:RT=1744823163:S=ALNI_MYYQ-JtZJ3j09DBj4o44jZCw26Duw; __eoi=ID=7965ed10bbcbecdd:T=1744823163:RT=1744823163:S=AA-AfjZU-QTOWXOD_8EEPTtTs6Ox; FCNEC=%5B%5B%22AKsRol-xCuRWzDpZ8i6H_pyfEHbs4qECBj3R8xozYq7s35tDtMPxGyUqKJV_3ntsyj71RIq3uTkV1p5vK4LBQx1H8WNMV2-ZJxOu0GjGXds2yzzCk47W5k4ojteeNpQeBYXk10WzmxY7YAkVr5hSMAR0d4v9U2wo0Q%3D%3D%22%5D%5D"
            )
        )
        val href = response.data?.substringAfter("<div class=\"text-center\">")
        val videoLink = response.data?.substringAfter("<video")?.substringAfter(" src=\"")
            ?.substringBefore("\" ")
        val link = href
            ?.substringBefore("Download images Link")
            ?.substringAfter("<a href=\"")?.substringBefore("\"") ?: ""
        val model = if (videoLink.isNullOrBlank().not() && videoLink.startsWith("https")) {
            ParsedVideo(
                thumbnail = if (link.isNotBlank() && link.startsWith("https")) {
                    link
                } else {
                    null
                },
                qualities = listOf(
                    ParsedQuality(
                        videoLink, mediaType = if (videoLink.contains("/image/")) {
                            MediaTypeData.Image
                        } else {
                            MediaTypeData.Video
                        }
                    )
                ),
            )
        } else if (link.isNotBlank() && link.startsWith("https")) {

            ParsedVideo(
                qualities = listOf(
                    ParsedQuality(
                        link, mediaType = if (link.contains("/image/")) {
                            MediaTypeData.Image
                        } else {
                            MediaTypeData.Video
                        }
                    )
                ),
            )
        } else {
            null
        }
        Log.d("cvv", "Linkedin response :${videoLink}")
        return if (model == null) {
            Result.failure(Exception())
        } else {
            Result.success(model)
        }
    }
}