package com.adm.url_parser.impls.main_sites.ok

import com.adm.url_parser.commons.Scrape
import com.adm.url_parser.commons.arr
import com.adm.url_parser.commons.obj
import com.adm.url_parser.commons.str
import com.adm.url_parser.interfaces.SiteScrapper
import com.adm.url_parser.models.ParsedVideo
import org.jsoup.Jsoup

/** Embed page `data-options` → `flashvars.metadata.videos[]`. Topic posts (multi-video) not handled. */
object OkRuScrapper : SiteScrapper {
    override val name = "OK.ru"

    private val POST = Regex("""^https?://(?:m\.|www\.)?(?:ok\.ru|odnoklassniki\.ru)/(?:video|live|videoembed)/\d+""", RegexOption.IGNORE_CASE)
    private val LABELS = linkedMapOf(
        "ultra" to "2160p", "quad" to "1440p", "full" to "1080p", "hd" to "720p",
        "sd" to "480p", "low" to "360p", "lowest" to "240p", "mobile" to "144p",
    )
    private val HEADERS = mapOf("User-Agent" to Scrape.UA_IPHONE)

    override fun matches(url: String) = POST.containsMatchIn(url.trim())

    override suspend fun scrapeLink(url: String): Result<ParsedVideo?> {
        // The full video page streams megabytes of HTML; the embed page (~40 KB) carries the same player options.
        val id = Regex("""/(?:video|live|videoembed)/(\d+)""").find(url)?.groupValues?.get(1)
            ?: return Result.failure(Exception("No ok.ru id"))
        val page = Scrape.get("https://ok.ru/videoembed/$id", ua = Scrape.UA_IPHONE) ?: return Result.failure(Exception("No page"))
        val metadata = Jsoup.parse(page.body).select("[data-options]")
            .mapNotNull {
                val flashvars = Scrape.parse(it.attr("data-options")).obj("flashvars")
                flashvars.obj("metadata") ?: flashvars.str("metadata")?.let(Scrape::parse) // object on desktop, JSON string on some pages
            }
            .firstOrNull { it.arr("videos") != null }
        val videos = metadata.arr("videos").orEmpty().filter { it.str("disallowed") != "true" }
        val items = LABELS.mapNotNull { (key, label) ->
            videos.firstOrNull { it.str("name") == key }?.str("url")?.let { Scrape.video(it, label) }
        }
        val movie = metadata.obj("movie")
        return Scrape.result(items, movie.str("title"), movie.str("poster"), HEADERS)
    }
}
