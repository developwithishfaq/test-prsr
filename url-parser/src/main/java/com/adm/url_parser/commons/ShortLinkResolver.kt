package com.adm.url_parser.commons

import java.net.URI

/** Expands share/short links (vm.tiktok.com, t.co, b23.tv, reddit /s/ …) to the real post URL. */
object ShortLinkResolver {
    private val SHORT_HOSTS = setOf(
        "vm.tiktok.com", "t.co", "fb.watch", "redd.it", "v.redd.it",
        "v.douyin.com", "v.iesdouyin.com", "b23.tv", "bili2233.cn", "xhslink.com", "t.snapchat.com",
        "l.likee.video", "dai.ly", "k.kwai.com", "s.snackvideo.com", "v.kuaishou.com",
    )

    fun isShortLink(url: String): Boolean {
        val uri = try { URI(url.trim()) } catch (_: Exception) { return false }
        val host = uri.host?.lowercase()?.removePrefix("www.") ?: return false
        return host in SHORT_HOSTS || (host.endsWith("reddit.com") && uri.path.orEmpty().contains("/s/"))
    }

    /** Final URL after HTTP redirects (and a meta-refresh, which t.co serves to browsers); null if not a short link or it fails. */
    suspend fun resolve(url: String): String? {
        if (!isShortLink(url)) return null
        val page = Scrape.get(url.trim()) ?: return null
        val metaRefresh = Regex("""http-equiv=["']?refresh["']?[^>]*url=([^"'>]+)""", RegexOption.IGNORE_CASE)
            .find(page.body)?.groupValues?.get(1)
        return (if (isShortLink(page.url) && metaRefresh != null) metaRefresh else page.url)
            .takeIf { it != url }
    }
}
