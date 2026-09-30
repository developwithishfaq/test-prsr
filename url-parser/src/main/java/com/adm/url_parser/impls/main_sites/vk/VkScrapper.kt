package com.adm.url_parser.impls.main_sites.vk

import com.adm.url_parser.commons.Scrape
import com.adm.url_parser.commons.arr
import com.adm.url_parser.commons.at
import com.adm.url_parser.commons.int
import com.adm.url_parser.commons.obj
import com.adm.url_parser.commons.str
import com.adm.url_parser.interfaces.SiteScrapper
import com.adm.url_parser.models.ParsedVideo
import kotlinx.serialization.json.JsonArray

/** `video_ext.php` embed page → `apiPrefetchCache` video.get → `files.mp4_*`. */
object VkScrapper : SiteScrapper {
    override val name = "VK"

    private val HOST = Regex("""^https?://(?:[a-z0-9-]+\.)*(?:vk\.com|vk\.ru|vkvideo\.ru)/""", RegexOption.IGNORE_CASE)
    private val VIDEO = Regex("""(?:video|clip)(-?\d+)_(\d+)""")
    private val RUNGS = listOf(2160, 1440, 1080, 720, 480, 360, 240, 144)
    private val HEADERS = mapOf("User-Agent" to Scrape.UA_DESKTOP)

    override fun matches(url: String) = HOST.containsMatchIn(url) && VIDEO.containsMatchIn(url)

    override suspend fun scrapeLink(url: String): Result<ParsedVideo?> {
        val (oid, id) = VIDEO.find(url)?.destructured ?: return Result.failure(Exception("No vk id"))
        val body = listOf("vk.com", "vk.ru").firstNotNullOfOrNull { host ->
            Scrape.get("https://$host/video_ext.php?oid=$oid&id=$id&hd=2")?.body?.takeIf { it.contains("\"mp4_") }
        } ?: return Result.failure(Exception("VK embed unavailable"))

        val item = (Scrape.jsonAfter(body, "\"apiPrefetchCache\":") as? JsonArray)
            ?.firstOrNull { it.str("method") == "video.get" }.obj("response").arr("items").at(0)
        val files = item.obj("files")
        val items = RUNGS.mapNotNull { h ->
            (files.str("mp4_$h") ?: Regex(""""mp4_$h"\s*:\s*"(https:[^"]+)"""").find(body)?.groupValues?.get(1)?.replace("\\/", "/"))
                ?.let { Scrape.video(it, "${h}p") }
        }
        val thumb = item.arr("image")?.maxByOrNull { it.int("width") ?: 0 }?.str("url")
        return Scrape.result(items, item.str("title"), thumb, HEADERS)
    }
}
