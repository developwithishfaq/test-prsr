package com.adm.url_parser.sdk.impl

import android.util.Log
import com.adm.url_parser.commons.impl.get_in_device_api.GetInDeviceApiImpl
import com.adm.url_parser.commons.utils.support_checker.UrlParserCheckSupport
import com.adm.url_parser.impls.main_sites.daily_motion.DailyMotionLinkScrapper
import com.adm.url_parser.impls.main_sites.fb.FbDownloaderMain
import com.adm.url_parser.impls.main_sites.insta.InstaDownloaderMain
import com.adm.url_parser.impls.main_sites.insta.impl.graphql.GraphQlConfigs
import com.adm.url_parser.impls.main_sites.tiktok.TiktokApi
import com.adm.url_parser.impls.main_sites.twitter.TwitterDownloader
import com.adm.url_parser.impls.meta_data_links.dailymotion.DailyMotionMetaDataExtractorImpl
import com.adm.url_parser.impls.not_for_kids.brazzer.impls.BrazzerDirectLinkApi
import com.adm.url_parser.impls.not_for_kids.inxx_in.InxxInDirectLinkApi
import com.adm.url_parser.impls.not_for_kids.porn_hub.PornHubDirectLinkApi
import com.adm.url_parser.impls.not_for_kids.xhamster_desi.XHamsterDesiDirectLinkApi
import com.adm.url_parser.impls.not_for_kids.xnxx.XnxxApiImpl
import com.adm.url_parser.impls.not_for_kids.xnxx_health.XnxxHealthApiImpl
import com.adm.url_parser.interfaces.ApiLinkScrapper
import com.adm.url_parser.models.ValidatorResponse
import com.adm.url_parser.sdk.interfaces.UrlParserConfigs

fun UrlParserConfigs.isLinkSupported(url: String): Boolean {
    return getParserConfigs(dataMap = mapOf("url" to url)).scrapper.isNotEmpty()
}

class UrlParserConfigsImpl(
    private val urlParserCheckSupport: UrlParserCheckSupport,
    private val graphQlConfigs: GraphQlConfigs
) : UrlParserConfigs {

    override fun getParserConfigs(dataMap: Map<String, String>): ValidatorResponse {
        val url = dataMap["url"] ?: ""
        val extras = listOf<ApiLinkScrapper>(GetInDeviceApiImpl())

        return if (urlParserCheckSupport.isFbLink(url)) {
            Log.d("GetInDeviceApiImpl", "getParserConfigs: fb link $url")
            ValidatorResponse(
                scrapper = listOf(
                    FbDownloaderMain()
                ) + extras,
                parserName = "Facebook"
            )
        } else if (urlParserCheckSupport.isInstaLink(url)) {
            ValidatorResponse(
                scrapper = listOf(InstaDownloaderMain(graphQlConfigs)),
                parserName = "Instagram"
            )
        } else if (urlParserCheckSupport.isTiktokLink(url)) {
            ValidatorResponse(
                scrapper =
                listOf(TiktokApi()) +
                        extras,
                parserName = "Tiktok"
            )
        } else if (urlParserCheckSupport.isTwitterLink(url)) {
            ValidatorResponse(
                scrapper = listOf(TwitterDownloader()) + extras,
                parserName = "Twitter"
            )
        } else if (urlParserCheckSupport.isBrazzerLink(url)) {
            ValidatorResponse(
                scrapper = listOf(BrazzerDirectLinkApi()),
                parserName = "Brazzer"
            )
        } else if (urlParserCheckSupport.isXhamsterDesiLink(url)) {
            ValidatorResponse(
                scrapper = listOf(XHamsterDesiDirectLinkApi()),
                parserName = "XHamsterDesi"
            )
        } else if (urlParserCheckSupport.isXnxHealth(url)) {
            ValidatorResponse(
                scrapper = listOf(XnxxHealthApiImpl()),
                parserName = "XnxxHealthApi"
            )
        } else if (urlParserCheckSupport.isXnxUrl(url)) {
            ValidatorResponse(
                scrapper = listOf(XnxxApiImpl()),
                parserName = "XnxxApi"
            )
        } else if (urlParserCheckSupport.isXVideosComUrl(url)) {
            ValidatorResponse(
                scrapper = listOf(XnxxApiImpl()),
                parserName = "XVideos_com"
            )
        } else if (urlParserCheckSupport.isInxxLink(url)) {
            ValidatorResponse(
                scrapper = listOf(InxxInDirectLinkApi()),
                parserName = "InxxxCom"
            )
        } else if (urlParserCheckSupport.isPornHubLink(url)) {
            ValidatorResponse(
                scrapper = listOf(PornHubDirectLinkApi()),
                parserName = "PornHub"
            )
        } else if (urlParserCheckSupport.isDailymotionLink(url)) {
            ValidatorResponse(
                scrapper = listOf(DailyMotionLinkScrapper(dailyMotionMetaDataExtractor = DailyMotionMetaDataExtractorImpl())),
                parserName = "DailymotionMain"
            )
        } else if (urlParserCheckSupport.isDailymotionMetaDataLink(url)) {
            ValidatorResponse(
                scrapper = listOf(DailyMotionMetaDataExtractorImpl()),
                parserName = "DailymotionMetaData"
            )
        } else {
            ValidatorResponse(emptyList())
        }
    }
}