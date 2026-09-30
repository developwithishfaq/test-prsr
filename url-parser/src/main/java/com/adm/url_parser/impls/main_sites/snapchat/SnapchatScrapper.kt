package com.adm.url_parser.impls.main_sites.snapchat

import com.adm.url_parser.commons.Scrape
import com.adm.url_parser.commons.arr
import com.adm.url_parser.commons.at
import com.adm.url_parser.commons.int
import com.adm.url_parser.commons.obj
import com.adm.url_parser.commons.str
import com.adm.url_parser.interfaces.SiteScrapper
import com.adm.url_parser.models.ParsedQuality
import com.adm.url_parser.models.ParsedVideo
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import org.jsoup.Jsoup

/** `__NEXT_DATA__` pageProps: spotlight / single snap, profile story, spotlight feed. */
object SnapchatScrapper : SiteScrapper {
    override val name = "Snapchat"

    override fun matches(url: String) =
        Regex("""^https?://(?:www\.|story\.|m\.)?snapchat\.com/.+""").containsMatchIn(url.trim())

    override suspend fun scrapeLink(url: String): Result<ParsedVideo?> {
        val normalized = url.trim().substringBefore("?").replace("://story.snapchat.com", "://www.snapchat.com")
        val page = Scrape.get(normalized) ?: return Result.failure(Exception("No page"))
        val props = Jsoup.parse(page.body).selectFirst("script#__NEXT_DATA__")?.data()
            ?.let(Scrape::parse).obj("props").obj("pageProps")
            ?: return Result.failure(Exception("No __NEXT_DATA__"))

        val single = props.obj("videoMetadata")
            ?: props.obj("spotlightFeed").arr("spotlightStories").at(0).obj("metadata").obj("videoMetadata")
        single.str("contentUrl")?.let { link ->
            val h = listOfNotNull(single.int("width"), single.int("height")).minOrNull()
            return Scrape.result(listOf(Scrape.video(link, h?.let { "${it}p" } ?: "HD")), single.str("name"), single.str("thumbnailUrl"))
        }
        val story = props.obj("story")
        val snaps = story.arr("snapList") ?: props.obj("preselectedStory").obj("premiumStory").obj("playerStory").arr("snapList")
        val items = snapItems(snaps)
        return Scrape.result(items, story.obj("storyTitle").str("value") ?: story.str("storyTitle"),
            story.obj("thumbnailUrl").str("value") ?: items.firstOrNull()?.url)
    }

    private fun snapItems(snaps: JsonArray?): List<ParsedQuality> = snaps.orEmpty().mapIndexedNotNull { i, snap: JsonElement ->
        val link = snap.obj("snapUrls").str("mediaUrl") ?: return@mapIndexedNotNull null
        if ((snap.int("snapMediaType") ?: 1) == 1) Scrape.video(link, "Snap ${i + 1}") else Scrape.image(link, "Snap ${i + 1}")
    }
}
