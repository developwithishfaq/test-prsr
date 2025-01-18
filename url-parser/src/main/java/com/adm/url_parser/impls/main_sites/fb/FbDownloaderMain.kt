package com.adm.url_parser.impls.main_sites.fb

import android.util.Log
import com.adm.url_parser.impls.main_sites.fb.FbCommons.extractFbVideoId
import com.adm.url_parser.impls.main_sites.fb.impls.FacebookDirectUrlApi
import com.adm.url_parser.impls.main_sites.fb.impls.FacebookDirectUrlApiWithUserName
import com.adm.url_parser.impls.main_sites.fb.impls.info.GetGeneralInfoOfFbUrl
import com.adm.url_parser.impls.main_sites.fb.impls.info.GetGeneralInfoOfFbUrlImpl
import com.adm.url_parser.interfaces.ApiLinkScrapper
import com.adm.url_parser.models.ParsedVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class FbDownloaderMain(
    private val getGeneralInfoOfFbUrl: GetGeneralInfoOfFbUrl = GetGeneralInfoOfFbUrlImpl()
) : ApiLinkScrapper {
    private val TAG = "FbDownloaderMain"
    override suspend fun scrapeLink(url: String): ParsedVideo? {
        Log.d(TAG, "scrapeLink: $url")
        return withContext(Dispatchers.IO) {
            val fbDirectUrlApi = FacebookDirectUrlApi()
            val fbDirectUrlApiWithUserName = FacebookDirectUrlApiWithUserName()
            if (url.contains("share")) {
                val generalInfo = getGeneralInfoOfFbUrl.getGeneralInfo(url)
                val videoIdAndUserNameModel = async {
                    fbDirectUrlApiWithUserName.scrapeLink(generalInfo.videoId, generalInfo.userName)
                }
                val videoIdModel = async {
                    fbDirectUrlApi.scrapeLink(generalInfo.videoId)
                }
                videoIdAndUserNameModel.await()?.let {
                    return@withContext it
                }
                videoIdModel.await()?.let {
                    return@withContext it
                }
            } else {
                val videoIdModel = async {
                    fbDirectUrlApi.scrapeLink(url.extractFbVideoId())
                }
                videoIdModel.await()?.let {
                    return@withContext it
                }
            }
        }
    }
}