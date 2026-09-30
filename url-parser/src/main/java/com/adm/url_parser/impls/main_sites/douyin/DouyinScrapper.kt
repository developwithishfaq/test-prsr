package com.adm.url_parser.impls.main_sites.douyin

import com.adm.url_parser.commons.Scrape
import com.adm.url_parser.commons.arr
import com.adm.url_parser.commons.at
import com.adm.url_parser.commons.content
import com.adm.url_parser.commons.obj
import com.adm.url_parser.commons.str
import com.adm.url_parser.interfaces.SiteScrapper
import com.adm.url_parser.models.ParsedVideo
import kotlinx.serialization.json.JsonObject

/** iesdouyin share page `_ROUTER_DATA` → item_list[0]: images, or play_addr.uri → watermark-free `play` URL. */
object DouyinScrapper : SiteScrapper {
    override val name = "Douyin"

    private val AWEME_ID = Regex("""(?:douyin\.com|iesdouyin\.com)/(?:share/)?(?:video|note|slides)/(\d+)""")

    override fun matches(url: String) = AWEME_ID.containsMatchIn(url)

    override suspend fun scrapeLink(url: String): Result<ParsedVideo?> {
        val id = AWEME_ID.find(url)?.groupValues?.get(1) ?: return Result.failure(Exception("No aweme id"))
        val page = Scrape.get(
            "https://www.iesdouyin.com/share/video/$id/", ua = Scrape.UA_ANDROID,
            headers = mapOf("Referer" to "https://www.douyin.com/", "Accept-Language" to "zh-CN,zh;q=0.9,en;q=0.8"),
        ) ?: return Result.failure(Exception("No page"))
        val item = (Scrape.jsonAfter(page.body, "_ROUTER_DATA").obj("loaderData") as? JsonObject)?.values
            ?.firstNotNullOfOrNull { it.obj("videoInfoRes").arr("item_list").at(0) }
            ?: return Result.failure(Exception("Douyin item not found"))

        val images = item.arr("images").orEmpty().mapIndexedNotNull { i, img ->
            img.arr("url_list")?.lastOrNull().content()?.let { Scrape.image(it, "Photo ${i + 1}") }
        }
        val items = images.ifEmpty {
            val uri = item.obj("video").obj("play_addr").str("uri")
            listOfNotNull(uri?.let { Scrape.video("https://aweme.snssdk.com/aweme/v1/play/?video_id=$it&ratio=1080p&line=0") })
        }
        val cover = item.obj("video").obj("cover").arr("url_list")?.firstOrNull().content()
        return Scrape.result(items, item.str("desc"), cover ?: images.firstOrNull()?.url, mapOf("User-Agent" to Scrape.UA_ANDROID))
    }
}
