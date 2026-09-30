package com.adm.url_parser.impls.main_sites.rednote

import com.adm.url_parser.commons.Scrape
import com.adm.url_parser.commons.arr
import com.adm.url_parser.commons.at
import com.adm.url_parser.commons.obj
import com.adm.url_parser.commons.str
import com.adm.url_parser.interfaces.SiteScrapper
import com.adm.url_parser.models.ParsedVideo
import kotlinx.serialization.json.JsonObject

/** Xiaohongshu note page `__INITIAL_STATE__` → note.noteDetailMap[*].note: h264 masterUrl or imageList. */
object RedNoteScrapper : SiteScrapper {
    override val name = "RedNote"

    override fun matches(url: String) =
        url.contains("xiaohongshu.com/") && (url.contains("/explore/") || url.contains("/discovery/item/"))

    override suspend fun scrapeLink(url: String): Result<ParsedVideo?> {
        val page = Scrape.get(url.trim(), ua = Scrape.UA_IPHONE) ?: return Result.failure(Exception("No page"))
        val note = (Scrape.jsonAfter(page.body, "__INITIAL_STATE__").obj("note").obj("noteDetailMap") as? JsonObject)
            ?.values?.firstNotNullOfOrNull { it.obj("note") }
        val stream = note.obj("video").obj("media").obj("stream")
        val video = (stream.arr("h264").at(0) ?: stream.arr("h265").at(0)).str("masterUrl")
            ?: Regex(""""masterUrl":"([^"]+)"""").find(page.body)?.groupValues?.get(1)?.replace("\\u002F", "/")
        val images = note.arr("imageList").orEmpty().mapIndexedNotNull { i, img ->
            (img.str("urlDefault") ?: img.str("url"))?.let { Scrape.image(it.https(), "Image ${i + 1}") }
        }
        val items = if (note.str("type") == "video" || images.isEmpty()) listOfNotNull(video?.let { Scrape.video(it.https()) }) else images
        return Scrape.result(items, note.str("title") ?: note.str("desc"), images.firstOrNull()?.url)
    }

    private fun String.https() = replaceFirst("http://", "https://")
}
