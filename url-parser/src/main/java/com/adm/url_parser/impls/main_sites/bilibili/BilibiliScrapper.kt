package com.adm.url_parser.impls.main_sites.bilibili

import com.adm.url_parser.commons.Scrape
import com.adm.url_parser.commons.arr
import com.adm.url_parser.commons.at
import com.adm.url_parser.commons.int
import com.adm.url_parser.commons.obj
import com.adm.url_parser.commons.str
import com.adm.url_parser.interfaces.SiteScrapper
import com.adm.url_parser.models.ParsedVideo

/**
 * bilibili.com: web-interface/view → cid → playurl?platform=html5 (progressive MP4 with sound).
 * bilibili.tv: intl playurl → DASH video_resource + audio_resource (audio muxed in by the app).
 * ponytail: bilibili.tv `/play/` (OGV episodes) not handled; add via ogv/play/episodes if users ask.
 */
object BilibiliScrapper : SiteScrapper {
    override val name = "Bilibili"

    private val BV = Regex("""/(BV[0-9A-Za-z]{10})""")
    private val AV = Regex("""/av(\d+)""", RegexOption.IGNORE_CASE)
    private val TV_AID = Regex("""bilibili\.tv/(?:[a-z]{2}(?:-[a-z]+)?/)?video/(\d+)""", RegexOption.IGNORE_CASE)
    private val HEADERS = mapOf("Referer" to "https://www.bilibili.com/", "User-Agent" to Scrape.UA_ANDROID)
    private val TV_HEADERS = mapOf("Referer" to "https://www.bilibili.tv/", "User-Agent" to Scrape.UA_ANDROID)
    private val LABELS = mapOf(16 to "360p", 32 to "480p", 64 to "720p", 80 to "1080p")

    override fun matches(url: String) =
        TV_AID.containsMatchIn(url) || (url.contains("bilibili.com/video/") && (BV.containsMatchIn(url) || AV.containsMatchIn(url)))

    override suspend fun scrapeLink(url: String): Result<ParsedVideo?> {
        TV_AID.find(url)?.let { return international(url, it.groupValues[1]) }
        val idParam = BV.find(url)?.let { "bvid=${it.groupValues[1]}" }
            ?: AV.find(url)?.let { "aid=${it.groupValues[1]}" }
            ?: return Result.failure(Exception("No bilibili id"))
        val view = api("https://api.bilibili.com/x/web-interface/view?$idParam")
            ?: return Result.failure(Exception("Bilibili view failed"))
        val part = Regex("""[?&]p=(\d+)""").find(url)?.groupValues?.get(1)?.toIntOrNull() ?: 1
        val cid = view.arr("pages").at(part - 1).int("cid") ?: view.int("cid")
            ?: return Result.failure(Exception("No cid"))
        val playId = view.str("bvid")?.let { "bvid=$it" } ?: "avid=${view.str("aid")}"

        val items = listOf(64, 16).mapNotNull { qn ->
            val play = api("https://api.bilibili.com/x/player/playurl?$playId&cid=$cid&platform=html5&fnval=1&qn=$qn")
            play.arr("durl").at(0).str("url")?.let { Scrape.video(it, LABELS[play.int("quality") ?: qn] ?: "HD") }
        }.distinctBy { it.url.substringBefore("?") }
        return Scrape.result(items, view.str("title"), view.str("pic")?.replace("http://", "https://"), HEADERS)
    }

    private suspend fun international(url: String, aid: String): Result<ParsedVideo?> {
        val playurl = Scrape.get(
            "https://api.bilibili.tv/intl/gateway/web/playurl?s_locale=en_US&platform=html5&aid=$aid&qn=64&type=0&device=wap&tf=0",
            ua = Scrape.UA_ANDROID, headers = TV_HEADERS,
        )?.let { Scrape.parse(it.body) }?.takeIf { it.str("code") == "0" }.obj("data").obj("playurl")
        val audio = playurl.arr("audio_resource")?.maxByOrNull { it.int("quality") ?: 0 }?.str("url")
        val items = playurl.arr("video").orEmpty()
            .sortedByDescending { it.obj("video_resource").int("quality") ?: 0 }
            .mapNotNull { v ->
                val res = v.obj("video_resource")
                res.str("url")?.let {
                    Scrape.video(it, v.obj("stream_info").str("desc_words") ?: LABELS[res.int("quality")] ?: "HD").copy(audioUrl = audio)
                }
            }
        val page = Scrape.get(url.substringBefore("?"), ua = Scrape.UA_ANDROID)?.body.orEmpty()
        return Scrape.result(items, Scrape.metaContent(page, "og:title"), Scrape.metaContent(page, "og:image"), TV_HEADERS)
    }

    private suspend fun api(url: String) =
        Scrape.get(url, ua = Scrape.UA_ANDROID, headers = mapOf("Referer" to "https://www.bilibili.com/"))
            ?.let { Scrape.parse(it.body) }?.takeIf { it.str("code") == "0" }?.obj("data")
}
