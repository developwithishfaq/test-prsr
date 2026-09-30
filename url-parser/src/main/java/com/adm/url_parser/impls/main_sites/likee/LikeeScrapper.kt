package com.adm.url_parser.impls.main_sites.likee

import com.adm.url_parser.commons.Scrape
import com.adm.url_parser.commons.str
import com.adm.url_parser.interfaces.SiteScrapper
import com.adm.url_parser.models.ParsedVideo

/** Post page `window.data` (video_url / image2 / msg_text), og: tags as fallback. */
object LikeeScrapper : SiteScrapper {
    override val name = "Likee"

    override fun matches(url: String) =
        Regex("""likee\.(?:video|com)/(?:v/|@[^/]+/video/)""").containsMatchIn(url)

    override suspend fun scrapeLink(url: String): Result<ParsedVideo?> {
        val page = Scrape.get(url.trim(), ua = Scrape.UA_IPHONE) ?: return Result.failure(Exception("No page"))
        val data = Scrape.jsonAfter(page.body, "window.data")
        val video = data.str("video_url") ?: Scrape.metaContent(page.body, "og:video")
            ?: Scrape.metaContent(page.body, "og:video:url")
        return Scrape.result(
            listOfNotNull(video?.let { Scrape.video(it) }),
            data.str("msg_text") ?: Scrape.metaContent(page.body, "og:title"),
            data.str("image2") ?: data.str("image1") ?: Scrape.metaContent(page.body, "og:image"),
        )
    }
}
