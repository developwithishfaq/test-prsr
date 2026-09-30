package com.adm.url_parser.impls.main_sites.rumble

import com.adm.url_parser.commons.Scrape
import com.adm.url_parser.commons.int
import com.adm.url_parser.commons.obj
import com.adm.url_parser.commons.str
import com.adm.url_parser.interfaces.SiteScrapper
import com.adm.url_parser.models.ParsedQuality
import com.adm.url_parser.models.ParsedVideo
import kotlinx.serialization.json.JsonObject
import java.net.URLEncoder

/** Embed id (page / oEmbed) → `embedJS/u3/?request=video` → `ua.mp4|webm|hls`. */
object RumbleScrapper : SiteScrapper {
    override val name = "Rumble"

    private val VIDEO_PAGE = Regex("""rumble\.com/(?:(?:shorts|embed)/v[0-9a-zA-Z]+|v[0-9a-zA-Z]+(?:-[^/?#]*)?\.html)""")
    private val EMBED_ID = Regex("""/embed/([0-9a-zA-Z]+)""")
    private val HEADERS = mapOf("Referer" to "https://rumble.com/")

    override fun matches(url: String) = VIDEO_PAGE.containsMatchIn(url)

    override suspend fun scrapeLink(url: String): Result<ParsedVideo?> {
        val clean = url.substringBefore("?").substringBefore("#")
        val ids = LinkedHashSet<String>()
        EMBED_ID.find(clean)?.let { ids += it.groupValues[1] }
        Scrape.get(clean, headers = HEADERS)?.body?.let { html -> EMBED_ID.findAll(html).forEach { ids += it.groupValues[1] } }
        if (ids.isEmpty()) {
            Scrape.get("https://rumble.com/api/Media/oembed.json?url=${URLEncoder.encode(clean, "UTF-8")}")?.body
                ?.let { EMBED_ID.find(it)?.groupValues?.get(1) }?.let { ids += it }
        }
        for (id in ids) {
            val json = Scrape.get("https://rumble.com/embedJS/u3/?request=video&ver=2&v=$id", headers = HEADERS)
                ?.let { Scrape.parse(it.body) } ?: continue
            val ua = json.obj("ua") ?: continue
            val items = listOf("mp4", "webm", "hls").firstNotNullOfOrNull { fmt -> formatItems(ua[fmt]).ifEmpty { null } }
                ?: continue
            return Scrape.result(items, json.str("title"), json.str("i") ?: json.str("t"), HEADERS)
        }
        return Result.failure(Exception("Rumble video not found"))
    }

    private fun formatItems(format: Any?): List<ParsedQuality> =
        (format as? JsonObject).orEmpty().mapNotNull { (key, v) ->
            val link = v.str("url") ?: return@mapNotNull null
            val h = key.filter(Char::isDigit).toIntOrNull() ?: v.obj("meta").int("h")
            h to Scrape.video(link, h?.let { "${it}p" } ?: "HD")
        }.sortedByDescending { it.first ?: 0 }.map { it.second }
}
