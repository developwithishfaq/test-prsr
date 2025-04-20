package com.adm.url_parser.impls.main_sites.fb.impls.insta_redirect_fb

import android.util.Log
import com.adm.url_parser.commons.network.ParserRequestTypes
import com.adm.url_parser.commons.network.UrlParserNetworkClient
import com.adm.url_parser.commons.network.UrlParserNetworkResponse
import com.adm.url_parser.impls.main_sites.insta.InstaDownloaderMain
import com.adm.url_parser.interfaces.ApiLinkScrapper
import com.adm.url_parser.models.ParsedVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FbSimpleShareLinkScrapper(
    private val instaDownloaderMain: InstaDownloaderMain
) : ApiLinkScrapper {
    private val TAG = "FbSimpleShareLinkScrapper"
    override suspend fun scrapeLink(url: String): Result<ParsedVideo?> {
        Log.d(TAG, "FbSimpleShareLinkScrapper:$url")
        return withContext(Dispatchers.IO) {
            val firstUrlResponse = UrlParserNetworkClient.makeNetworkRequestString(
                url = url,
                requestType = ParserRequestTypes.Get
            )
            if (firstUrlResponse is UrlParserNetworkResponse.Failure) {
                return@withContext Result.failure(Exception("Failed to hit url=${url}, exception ${firstUrlResponse.error}"))
            }
            val responseHtml = firstUrlResponse.data ?: ""
            val actorId =
                responseHtml.substringAfter("\"props\":{\"actorID\":").substringBefore(",")
                    .toLongOrNull()
            val storyToken =
                responseHtml.substringAfter("\"story_token\":\"").substringBefore("\",")
                    .toLongOrNull()
            if (actorId == null || storyToken == null) {
                return@withContext Result.failure(Exception("FbSimpleShareLinkScrapper actorId=$actorId storyToken=$storyToken ,one is null for url =$url"))
            }
            val secondUrl = "https://www.facebook.com/$actorId/posts/$storyToken"
            Log.d(TAG, "FbSimpleShareLinkScrapper:secondUrl=$secondUrl")
            val secondUrlResponse = UrlParserNetworkClient.makeNetworkRequestString(
                url = secondUrl,
                requestType = ParserRequestTypes.Get,
                headers = mapOf(
                    "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9"
                )
            )
            val secondResponseHtml = secondUrlResponse.data ?: ""
            val instaReelId =
                secondResponseHtml.substringAfter("https:\\/\\/www.instagram.com\\/reel\\/")
                    .substringBefore("\\/\"")
            val thirdUrl = "https://www.instagram.com/reel/$instaReelId/"
            Log.d(TAG, "FbSimpleShareLinkScrapper thirdUrl=$thirdUrl")
            val finalModel = instaDownloaderMain.scrapeLink(thirdUrl)
            Log.d(TAG, "FbSimpleShareLinkScrapper response=$finalModel")
            finalModel
        }

    }
}