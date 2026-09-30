package com.adm.url_parser.impls.main_sites.kwai

import com.adm.url_parser.commons.Scrape
import com.adm.url_parser.interfaces.SiteScrapper
import com.adm.url_parser.models.ParsedVideo

/**
 * Kwai / SnackVideo / Kuaishou: harvest `.mp4` URLs from the server-rendered HTML; `_b_` = HD, `_sl` = SD.
 * If the page is client-rendered this fails and the in-app browser's network sniffer still catches the mp4.
 */
object KwaiScrapper : SiteScrapper {
    override val name = "Kwai"

    private val MP4 = Regex("""https:[^"'\s<>]+?\.mp4[^"'\s\\<>]*""")

    override fun matches(url: String) =
        Regex("""(?:kwai\.com|snackvideo\.com|kuaishou\.com)/.*(?:/video/|/photo/|/short-video/)""").containsMatchIn(url)

    override suspend fun scrapeLink(url: String): Result<ParsedVideo?> {
        val page = Scrape.get(url.trim(), ua = Scrape.UA_IPHONE) ?: return Result.failure(Exception("No page"))
        val html = page.body.replace("\\u002F", "/").replace("\\/", "/").replace("&amp;", "&")
        val urls = MP4.findAll(html).map { it.value }.distinct().toList()
        val hd = urls.firstOrNull { it.contains("_b_") }
        val sd = urls.firstOrNull { it.contains("_sl") && it != hd }
        val items = listOfNotNull(hd?.let { Scrape.video(it, "HD") }, sd?.let { Scrape.video(it, "SD") })
            .ifEmpty { urls.take(1).map { Scrape.video(it) } }
        return Scrape.result(items, Scrape.metaContent(page.body, "og:title"), Scrape.metaContent(page.body, "og:image"))
    }
}
