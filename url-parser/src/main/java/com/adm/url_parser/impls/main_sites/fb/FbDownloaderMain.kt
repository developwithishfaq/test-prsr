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
    override suspend fun scrapeLink(url: String): Result<ParsedVideo?> {
        Log.d(TAG, "scrapeLink: $url")
        return withContext(Dispatchers.IO) {
            val fbDirectUrlApi = FacebookDirectUrlApi()
            val fbDirectUrlApiWithUserName = FacebookDirectUrlApiWithUserName()
            if (url.contains("share")) {
                val generalInfo = getGeneralInfoOfFbUrl.getGeneralInfo(url)
                if (generalInfo.userName.isNotBlank()) {
                    val videoIdAndUserNameModel = async {
                        fbDirectUrlApiWithUserName.scrapeLink(
                            generalInfo.videoId,
                            generalInfo.userName
                        )
                    }
                    val videoIdModel = async {
                        fbDirectUrlApi.scrapeLink(generalInfo.videoId)
                    }
                    val model1 = videoIdAndUserNameModel.await()
                    if (model1 != null) {
                        Result.success(model1)
                    } else {
                        val model2 = videoIdModel.await()
                        if (model2 != null) {
                            Result.success(model2)
                        } else {
                            Result.failure(Exception("Video Not Found"))
                        }
                    }
                } else {
                    Result.failure(Exception("User Name Is Blank"))
                }
            } else {
                val videoIdModel = async {
                    fbDirectUrlApi.scrapeLink(url.extractFbVideoId())
                }
                val model = videoIdModel.await()
                if (model != null) {
                    Result.success(model)
                } else {
                    Result.failure(Exception("fbDirectUrlApi did not returned video model"))
                }
            }
        }
    }
}