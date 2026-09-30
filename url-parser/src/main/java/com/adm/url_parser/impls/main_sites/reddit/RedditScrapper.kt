package com.adm.url_parser.impls.main_sites.reddit

import com.adm.url_parser.commons.Scrape
import com.adm.url_parser.commons.arr
import com.adm.url_parser.commons.at
import com.adm.url_parser.commons.int
import com.adm.url_parser.commons.obj
import com.adm.url_parser.commons.str
import com.adm.url_parser.interfaces.SiteScrapper
import com.adm.url_parser.models.ParsedQuality
import com.adm.url_parser.models.ParsedVideo
import kotlinx.serialization.json.JsonElement

/** `<post>.json?raw_json=1`: reddit_video, galleries (media_metadata) and single i.redd.it images. */
object RedditScrapper : SiteScrapper {
    override val name = "Reddit"

    override fun matches(url: String) = url.contains("reddit.com/") && url.contains("/comments/")

    override suspend fun scrapeLink(url: String): Result<ParsedVideo?> {
        val api = url.substringBefore("?").substringBefore("#").trimEnd('/') + ".json?raw_json=1"
        val json = Scrape.get(api)?.let { Scrape.parse(it.body) }
        val post = json.at(0).obj("data").arr("children").at(0).obj("data")
            ?: return Result.failure(Exception("Reddit post not found"))

        val title = post.str("title")
        val thumb = post.obj("preview").arr("images").at(0).obj("source").str("url")
        val items = mutableListOf<ParsedQuality>()

        val rv = redditVideo(post) ?: redditVideo(post.arr("crosspost_parent_list").at(0))
            ?: post.obj("preview").obj("reddit_video_preview")
        val videoUrl = rv.str("fallback_url")?.substringBefore("?")
        if (videoUrl != null) {
            val height = rv.int("height")
            // reddit serves audio as a separate file; the app muxes it in after download
            val audio = if (rv.str("is_gif") == "true") null else findAudio(videoUrl)
            items += Scrape.video(videoUrl, height?.let { "${it}p" } ?: "HD").copy(audioUrl = audio)
        } else {
            val meta = post.obj("media_metadata")
            val order = post.obj("gallery_data").arr("items")?.mapNotNull { it.str("media_id") } ?: meta?.keys?.toList().orEmpty()
            order.forEachIndexed { i, id ->
                val m = meta.obj(id)
                val animated = m.obj("s").str("mp4")
                if (animated != null) items += Scrape.video(animated, "GIF ${i + 1}")
                else items += Scrape.image("https://i.redd.it/$id.${m.str("m")?.substringAfter("/") ?: "jpg"}", "Image ${i + 1}")
            }
            post.str("url")?.takeIf { it.contains("i.redd.it") }?.let { items += Scrape.image(it) }
        }
        return Scrape.result(items, title, thumb ?: items.firstOrNull()?.url)
    }

    private fun redditVideo(post: JsonElement?) =
        post.obj("secure_media").obj("reddit_video") ?: post.obj("media").obj("reddit_video")

    private suspend fun findAudio(videoUrl: String): String? {
        val base = videoUrl.substringBeforeLast("/")
        return listOf("DASH_AUDIO_128.mp4", "DASH_AUDIO_64.mp4", "DASH_audio.mp4", "CMAF_AUDIO_128.mp4", "CMAF_AUDIO_64.mp4")
            .map { "$base/$it" }
            .firstOrNull { Scrape.get(it, headers = mapOf("Range" to "bytes=0-0")) != null }
    }
}
