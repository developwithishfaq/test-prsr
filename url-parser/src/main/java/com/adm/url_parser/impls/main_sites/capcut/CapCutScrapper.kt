package com.adm.url_parser.impls.main_sites.capcut

import com.adm.url_parser.commons.Scrape
import com.adm.url_parser.commons.obj
import com.adm.url_parser.commons.str
import com.adm.url_parser.interfaces.SiteScrapper
import com.adm.url_parser.models.ParsedVideo
import kotlinx.serialization.json.JsonObject

/** Template page `__MODERN_ROUTER_DATA__` → loaderData[*].templateDetail.videoUrl (the template preview). */
object CapCutScrapper : SiteScrapper {
    override val name = "CapCut"

    override fun matches(url: String) =
        url.contains("capcut.com/") && (url.contains("/tv2/") || url.contains("/template-detail/") || url.contains("/t/"))

    override suspend fun scrapeLink(url: String): Result<ParsedVideo?> {
        val page = Scrape.get(url.trim(), ua = Scrape.UA_IPHONE) ?: return Result.failure(Exception("No page"))
        val detail = (Scrape.jsonAfter(page.body, "__MODERN_ROUTER_DATA__").obj("loaderData") as? JsonObject)
            ?.values?.firstNotNullOfOrNull { it.obj("templateDetail")?.takeIf { d -> d.str("videoUrl") != null } }
        return Scrape.result(
            listOfNotNull(detail.str("videoUrl")?.let { Scrape.video(it) }),
            detail.str("title"),
            detail.str("coverUrl"),
        )
    }
}
