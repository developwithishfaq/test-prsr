package com.adm.url_parser.impls.main_sites.insta

import android.util.Log
import com.adm.url_parser.impls.main_sites.insta.impl.InstaGraphQlApi
import com.adm.url_parser.impls.main_sites.insta.impl.InstagramDirectUrlApi
import com.adm.url_parser.impls.main_sites.insta.impl.InstagramDirectUrlApiVideoPreview
import com.adm.url_parser.impls.main_sites.insta.impl.InstagramDownloader
import com.adm.url_parser.interfaces.ApiLinkScrapper
import com.adm.url_parser.interfaces.ApiLinkScrapperForSubImpl
import com.adm.url_parser.models.ParsedVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class InstaDownloaderMain : ApiLinkScrapper {
    private val TAG = "InstaDownloaderMain"
    override suspend fun scrapeLink(url: String): ParsedVideo? {
        Log.d(TAG, "scrapeLink: $url")
        return withContext(Dispatchers.IO) {
            val instaDirectUrlPreview: ApiLinkScrapperForSubImpl =
                InstagramDirectUrlApiVideoPreview()
            val instaDirectUrl: ApiLinkScrapperForSubImpl = InstagramDirectUrlApi()
            val instaGraphQlApi: ApiLinkScrapperForSubImpl = InstaGraphQlApi()
            val instagramDownloader: ApiLinkScrapperForSubImpl = InstagramDownloader()
            val deferredResults = listOf(
                async { instaDirectUrlPreview.scrapeLink(url) },
                async { instaDirectUrl.scrapeLink(url) },
                async { instaGraphQlApi.scrapeLink(url) },
                async { instagramDownloader.scrapeLink(url) },
            )
            var video: ParsedVideo? = null
            deferredResults.forEachIndexed { index, model ->
                val result = model.await()
                Log.d(TAG, "scrapeLink(${index}):$result ")
                if (result != null) {
                    video = result
                    return@forEachIndexed
                }
            }
            video
        }
    }
}