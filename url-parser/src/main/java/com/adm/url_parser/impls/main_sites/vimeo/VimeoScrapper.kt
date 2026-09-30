package com.adm.url_parser.impls.main_sites.vimeo

import com.adm.url_parser.commons.Scrape
import com.adm.url_parser.commons.arr
import com.adm.url_parser.commons.int
import com.adm.url_parser.commons.obj
import com.adm.url_parser.commons.str
import com.adm.url_parser.interfaces.SiteScrapper
import com.adm.url_parser.models.ParsedVideo

/** Player config `request.files.progressive` (muxed MP4s). */
object VimeoScrapper : SiteScrapper {
    override val name = "Vimeo"

    // vimeo.com/123, vimeo.com/123/<unlisted-hash>, vimeo.com/channels/x/123, player.vimeo.com/video/123?h=hash
    private val ID = Regex("""vimeo\.com/(?:video/|(?:[^/?#]+/)*?)(\d+)(?:/([0-9a-f]{6,}))?""")
    private val HEADERS = mapOf("Referer" to "https://player.vimeo.com/", "Origin" to "https://vimeo.com")

    override fun matches(url: String) = ID.containsMatchIn(url)

    override suspend fun scrapeLink(url: String): Result<ParsedVideo?> {
        val m = ID.find(url) ?: return Result.failure(Exception("No vimeo id"))
        val id = m.groupValues[1]
        val hash = m.groupValues[2].ifBlank { Regex("""[?&]h=([0-9a-f]+)""").find(url)?.groupValues?.get(1).orEmpty() }
        val config = Scrape.get(
            "https://player.vimeo.com/video/$id/config" + if (hash.isNotBlank()) "?h=$hash" else "",
            headers = HEADERS,
        )?.let { Scrape.parse(it.body) }

        // ponytail: HLS/DASH-only videos (separate audio track) are skipped until the app can mux A/V
        val items = config.obj("request").obj("files").arr("progressive").orEmpty()
            .sortedByDescending { it.int("height") ?: 0 }
            .mapNotNull { p -> p.str("url")?.let { Scrape.video(it, p.str("quality") ?: "${p.int("height")}p") } }
        val video = config.obj("video")
        return Scrape.result(items, video.str("title"), video.obj("thumbs").str("base") ?: video.obj("thumbs").str("640"), HEADERS)
    }
}
