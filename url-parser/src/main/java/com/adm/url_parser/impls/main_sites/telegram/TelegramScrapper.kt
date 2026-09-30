package com.adm.url_parser.impls.main_sites.telegram

import com.adm.url_parser.commons.Scrape
import com.adm.url_parser.interfaces.SiteScrapper
import com.adm.url_parser.models.ParsedVideo
import org.jsoup.Jsoup

/** Public channel posts via the `?embed=1` widget page. */
object TelegramScrapper : SiteScrapper {
    override val name = "Telegram"

    private val POST = Regex("""^https?://(?:www\.)?(?:t\.me|telegram\.me|telegram\.dog)/(?:s/)?([A-Za-z0-9_]{4,})/(\d+)""")
    private val BG_URL = Regex("""background-image:\s*url\(['"]?([^'")]+)""")

    override fun matches(url: String) = POST.containsMatchIn(url.trim())

    override suspend fun scrapeLink(url: String): Result<ParsedVideo?> {
        val (channel, id) = POST.find(url.trim())?.destructured ?: return Result.failure(Exception("Not a post"))
        val page = Scrape.get("https://t.me/$channel/$id?embed=1&mode=tme") ?: return Result.failure(Exception("No page"))
        val doc = Jsoup.parse(page.body)
        val bg = { css: String -> doc.select(css).mapNotNull { BG_URL.find(it.attr("style"))?.groupValues?.get(1) } }

        val videos = doc.select("video[src]").map { it.attr("src") }.distinct()
        val photos = bg(".tgme_widget_message_photo_wrap")
        val items = videos.mapIndexed { i, v -> Scrape.video(v, if (videos.size > 1) "Video ${i + 1}" else "HD") } +
                photos.mapIndexed { i, p -> Scrape.image(p, "Image ${i + 1}") }
        val title = doc.selectFirst(".tgme_widget_message_text")?.text()
            ?: Scrape.metaContent(page.body, "og:description")
        val thumb = bg(".tgme_widget_message_video_thumb").firstOrNull() ?: Scrape.metaContent(page.body, "og:image")
        return Scrape.result(items, title, thumb)
    }
}
