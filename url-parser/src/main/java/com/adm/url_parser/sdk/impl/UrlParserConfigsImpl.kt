package com.adm.url_parser.sdk.impl

import com.adm.url_parser.commons.utils.support_checker.UrlParserCheckSupport
import com.adm.url_parser.impls.main_sites.daily_motion.DailyMotionLinkScrapper
import com.adm.url_parser.impls.main_sites.fb.FbDownloaderMain
import com.adm.url_parser.impls.main_sites.insta.InstaDownloaderMain
import com.adm.url_parser.impls.main_sites.insta.impl.graphql.GraphQlConfigs
import com.adm.url_parser.impls.main_sites.tiktok.TiktokDownloader
import com.adm.url_parser.impls.main_sites.twitter.TwitterDownloader
import com.adm.url_parser.impls.meta_data_links.dailymotion.DailyMotionMetaDataExtractorImpl
import com.adm.url_parser.impls.not_for_kids.brazzer.impls.BrazzerDirectLinkApi
import com.adm.url_parser.impls.not_for_kids.inxx_in.InxxInDirectLinkApi
import com.adm.url_parser.impls.not_for_kids.porn_hub.PornHubDirectLinkApi
import com.adm.url_parser.impls.not_for_kids.xhamster_desi.XHamsterDesiDirectLinkApi
import com.adm.url_parser.models.ValidatorResponse
import com.adm.url_parser.sdk.interfaces.UrlParserConfigs

fun UrlParserConfigs.isLinkSupported(url: String): Boolean {
    return getParserConfigs(dataMap = mapOf("url" to url)).scrapper != null
}

class UrlParserConfigsImpl(
    private val urlParserCheckSupport: UrlParserCheckSupport,
    private val graphQlConfigs: GraphQlConfigs
) : UrlParserConfigs {
    override fun getParserConfigs(dataMap: Map<String, String>): ValidatorResponse {
        val url = dataMap["url"] ?: ""
        return if (urlParserCheckSupport.isFbLink(url)) {
            ValidatorResponse(
                scrapper = FbDownloaderMain(),
                parserName = "Facebook"
            )
        } else if (urlParserCheckSupport.isInstaLink(url)) {
            ValidatorResponse(
                scrapper = InstaDownloaderMain(graphQlConfigs), parserName = "Instagram"
            )
        } else if (urlParserCheckSupport.isTiktokLink(url)) {
            ValidatorResponse(
                scrapper = TiktokDownloader(), parserName = "Tiktok"
            )
        } else if (urlParserCheckSupport.isTwitterLink(url)) {
            ValidatorResponse(
                scrapper = TwitterDownloader(),
                parserName = "Twitter"
            )
        } else if (urlParserCheckSupport.isBrazzerLink(url)) {
            ValidatorResponse(
                scrapper = BrazzerDirectLinkApi(), parserName = "Brazzer"
            )
        } else if (urlParserCheckSupport.isXhamsterDesiLink(url)) {
            ValidatorResponse(
                scrapper = XHamsterDesiDirectLinkApi(), parserName = "XHamsterDesi"
            )
        } else if (urlParserCheckSupport.isInxxLink(url)) {
            ValidatorResponse(
                scrapper = InxxInDirectLinkApi(), parserName = "InxxxCom"
            )
        } else if (urlParserCheckSupport.isPornHubLink(url)) {
            ValidatorResponse(
                scrapper = PornHubDirectLinkApi(), parserName = "PornHub"
            )
        } else if (urlParserCheckSupport.isDailymotionLink(url)) {
            ValidatorResponse(
                scrapper = DailyMotionLinkScrapper(dailyMotionMetaDataExtractor = DailyMotionMetaDataExtractorImpl()),
                parserName = "DailymotionMain"
            )
        } else if (urlParserCheckSupport.isDailymotionMetaDataLink(url)) {
            ValidatorResponse(
                scrapper = DailyMotionMetaDataExtractorImpl(), parserName = "DailymotionMetaData"
            )
        } else {
            ValidatorResponse(null)
        }
    }
}