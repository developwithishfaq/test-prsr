package com.adm.url_parser

import com.adm.url_parser.commons.Scrape
import com.adm.url_parser.commons.ShortLinkResolver
import com.adm.url_parser.commons.str
import com.adm.url_parser.impls.main_sites.bilibili.BilibiliScrapper
import com.adm.url_parser.impls.main_sites.capcut.CapCutScrapper
import com.adm.url_parser.impls.main_sites.douyin.DouyinScrapper
import com.adm.url_parser.impls.main_sites.kwai.KwaiScrapper
import com.adm.url_parser.impls.main_sites.likee.LikeeScrapper
import com.adm.url_parser.impls.main_sites.ok.OkRuScrapper
import com.adm.url_parser.impls.main_sites.reddit.RedditScrapper
import com.adm.url_parser.impls.main_sites.rednote.RedNoteScrapper
import com.adm.url_parser.impls.main_sites.rumble.RumbleScrapper
import com.adm.url_parser.impls.main_sites.snapchat.SnapchatScrapper
import com.adm.url_parser.impls.main_sites.telegram.TelegramScrapper
import com.adm.url_parser.impls.main_sites.vimeo.VimeoScrapper
import com.adm.url_parser.impls.main_sites.vk.VkScrapper
import com.adm.url_parser.interfaces.SiteScrapper
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

class SiteScrappersTest {

    @Test
    fun jsonAfter_handlesBracesInsideStrings() {
        val html = """<script>window._ROUTER_DATA = {"a":"x}{\"y","b":[1,{"c":2}]};</script>"""
        assertEquals("x}{\"y", Scrape.jsonAfter(html, "_ROUTER_DATA").str("a"))
        assertEquals(null, Scrape.jsonAfter(html, "missing"))
        assertEquals("1", Scrape.jsonAfter("""s = {"v":undefined,"w":1}""", "s =").str("w"))
    }

    @Test
    fun shortLinks() {
        assertTrue(ShortLinkResolver.isShortLink("https://vm.tiktok.com/ZMabc/"))
        assertTrue(ShortLinkResolver.isShortLink("https://www.reddit.com/r/videos/s/AbC123"))
        assertTrue(ShortLinkResolver.isShortLink("https://b23.tv/xyz"))
        assertFalse(ShortLinkResolver.isShortLink("https://www.reddit.com/r/videos/comments/abc/t/"))
        assertFalse(ShortLinkResolver.isShortLink("not a url"))
    }

    @Test
    fun matchers() {
        val cases = mapOf(
            RedditScrapper to "https://www.reddit.com/r/nextfuckinglevel/comments/1abc/title/",
            TelegramScrapper to "https://t.me/durov/123",
            VimeoScrapper to "https://vimeo.com/channels/staffpicks/76979871",
            VkScrapper to "https://vkvideo.ru/video-22822305_456241864",
            OkRuScrapper to "https://ok.ru/video/1234567890",
            RumbleScrapper to "https://rumble.com/v4abcd-some-title.html",
            BilibiliScrapper to "https://www.bilibili.com/video/BV1GJ411x7h7",
            DouyinScrapper to "https://www.douyin.com/video/7300000000000000000",
            SnapchatScrapper to "https://www.snapchat.com/spotlight/W7_EDlXWTBiXAEEniNoMPwAAYaHd",
            RedNoteScrapper to "https://www.xiaohongshu.com/explore/65a1b2c3000000001e000000",
            LikeeScrapper to "https://likee.video/@user/video/7000000000000000000",
            CapCutScrapper to "https://www.capcut.com/template-detail/7000000000000000000",
            KwaiScrapper to "https://www.kwai.com/@user/video/5200000000000000000",
        )
        cases.forEach { (s, url) -> assertTrue("${s.name} should match $url", s.matches(url)) }
        assertTrue(BilibiliScrapper.matches("https://www.bilibili.tv/en/video/2046520226"))
        assertFalse(RumbleScrapper.matches("https://rumble.com/videos"))
        assertFalse(TelegramScrapper.matches("https://t.me/joinchat/AbCdEf"))
        assertFalse(RedditScrapper.matches("https://www.reddit.com/r/videos/"))
    }

    /** Hits the real sites: `LIVE_SCRAPE=1 gradlew :url-parser:testDebugUnitTest`. Prints one line per platform. */
    @Test
    fun live() = runBlocking {
        assumeTrue(System.getenv("LIVE_SCRAPE") != null)
        val targets: List<Pair<SiteScrapper, suspend () -> String?>> = listOf(
            RedditScrapper to { find("https://www.reddit.com/r/nextfuckinglevel/top.json?t=week&limit=10", """"permalink":\s*"(/r/[^"]+/comments/[^"]+)"""")?.let { "https://www.reddit.com$it" } },
            TelegramScrapper to { find("https://t.me/s/telegram", """data-post="(telegram/\d+)"""")?.let { "https://t.me/$it" } },
            VimeoScrapper to { "https://vimeo.com/76979871" },
            VkScrapper to { find("https://vkvideo.ru/", """(video-?\d+_\d+)""")?.let { "https://vkvideo.ru/$it" } },
            OkRuScrapper to { find("https://ok.ru/video", """/video/(\d{6,})""")?.let { "https://ok.ru/video/$it" } },
            RumbleScrapper to { "https://rumble.com/v7f9gx4-we-just-found-the-video..html" },
            BilibiliScrapper to { "https://www.bilibili.com/video/BV1GJ411x7h7" },
            DouyinScrapper to { System.getenv("DOUYIN_URL") },
            SnapchatScrapper to { find("https://www.snapchat.com/spotlight", """(https://www\.snapchat\.com/spotlight/[A-Za-z0-9_-]{20,})""") },
            RedNoteScrapper to { System.getenv("REDNOTE_URL") },
            LikeeScrapper to { System.getenv("LIKEE_URL") },
            CapCutScrapper to { find("https://www.capcut.com/templates", """(/template-detail/[^"&?]+)""")?.let { "https://www.capcut.com$it" } },
            KwaiScrapper to { "https://www.kwai.com/@KwaiBrasilOficial/video/5246843672400170048" },
        )
        val only = System.getenv("LIVE_ONLY")?.split(",") // e.g. LIVE_ONLY=OK.ru,CapCut
        targets.filter { only == null || it.first.name in only }.forEach { (scrapper, urlOf) ->
            val url = try { urlOf() } catch (e: Exception) { null }
            val line = if (url == null) "no sample url" else scrapper.scrapeLink(url).fold(
                { v -> "OK ${v?.qualities?.map { it.name }} title=${v?.title?.take(40)} first=${v?.qualities?.firstOrNull()?.url?.take(90)}" },
                { e -> "FAIL ${e.message}" },
            )
            println("LIVE ${scrapper.name.padEnd(9)} $url\n     -> $line")
        }
    }

    private suspend fun find(page: String, regex: String) =
        Scrape.get(page)?.body?.let { Regex(regex).find(it)?.groupValues?.get(1) }
}
