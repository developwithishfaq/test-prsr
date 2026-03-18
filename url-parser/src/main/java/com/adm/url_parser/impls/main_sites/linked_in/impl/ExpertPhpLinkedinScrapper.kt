package com.adm.url_parser.impls.main_sites.linked_in.impl

import android.util.Log
import com.adm.url_parser.commons.network.UrlParserNetworkClient
import com.adm.url_parser.interfaces.ApiLinkScrapperForSubImpl
import com.adm.url_parser.models.MediaTypeData
import com.adm.url_parser.models.ParsedQuality
import com.adm.url_parser.models.ParsedVideo

class ExpertPhpLinkedinScrapper : ApiLinkScrapperForSubImpl {
    companion object {
        private const val TAG = "ExpertPhpLinkedinScrapper"
    }

    override suspend fun scrapeLink(url: String): Result<ParsedVideo?> {
        val response = UrlParserNetworkClient.makeNetworkRequestStringXXForm(
            url = "https://www.expertsphp.com/twitter-video-downloader.php",
            formData = mapOf(
                "url" to url,
                "utm_medium" to "member_desktop",
                "rcm" to "ACoAACbslWMBNEhIdSLFUGqn_XdEndA-WRbzDq4"
            ),
            headers = mapOf(
                "accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7",
                "upgrade-insecure-requests" to "1",
                "user-agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36",
                "Cookie" to "_ga_D1XX1R246W=GS1.1.1744823161.1.1.1744823161.0.0.0; _ga=GA1.2.1226854796.1744823162; _gid=GA1.2.1285539298.1744823163; _gat_gtag_UA_120752274_1=1; __gads=ID=6c325720771b3541:T=1744823163:RT=1744823163:S=ALNI_MZTjqLaaIFuuE1S63h093Z3pEV3_Q; __gpi=UID=00001080a2958c24:T=1744823163:RT=1744823163:S=ALNI_MYYQ-JtZJ3j09DBj4o44jZCw26Duw; __eoi=ID=7965ed10bbcbecdd:T=1744823163:RT=1744823163:S=AA-AfjZU-QTOWXOD_8EEPTtTs6Ox; FCNEC=%5B%5B%22AKsRol-xCuRWzDpZ8i6H_pyfEHbs4qECBj3R8xozYq7s35tDtMPxGyUqKJV_3ntsyj71RIq3uTkV1p5vK4LBQx1H8WNMV2-ZJxOu0GjGXds2yzzCk47W5k4ojteeNpQeBYXk10WzmxY7YAkVr5hSMAR0d4v9U2wo0Q%3D%3D%22%5D%5D"
            )
        )

        // Anchor everything to id="showdata" to avoid false matches in header/nav
        val showDataContent = response.data
            ?.substringAfter("id=\"showdata\"")
            ?: return Result.failure(Exception("No showdata section found"))

        // Video URL: present only for video posts, inside <video src="...">
        val videoUrl = showDataContent
            .substringAfter("<video", "")
            .substringAfter("src=\"", "")
            .substringBefore("\"")
            .takeIf { it.startsWith("https") }

        // Image/thumbnail URL: both post types have <img style="width: 100%..." src="URL">
        // For video posts this is the thumbnail; for image posts this is the image itself
        val imgUrl = showDataContent
            .substringAfter("<img style=", "")
            .substringAfter("src=\"", "")
            .substringBefore("\"")
            .takeIf { it.startsWith("https") }

        val model = when {
            videoUrl != null -> ParsedVideo(
                thumbnail = imgUrl,
                qualities = listOf(
                    ParsedQuality(videoUrl, mediaType = MediaTypeData.Video)
                )
            )
            imgUrl != null -> ParsedVideo(
                qualities = listOf(
                    ParsedQuality(imgUrl, mediaType = MediaTypeData.Image)
                )
            )
            else -> null
        }

        Log.d(TAG, "Linkedin response: videoUrl=$videoUrl, imgUrl=$imgUrl, model=$model")

        return if (model == null) {
            Result.failure(Exception("Could not parse media from response"))
        } else {
            Result.success(model)
        }
    }
}