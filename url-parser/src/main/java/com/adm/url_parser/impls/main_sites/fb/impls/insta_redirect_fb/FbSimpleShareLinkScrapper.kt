package com.adm.url_parser.impls.main_sites.fb.impls.insta_redirect_fb

import android.util.Log
import com.adm.url_parser.commons.network.ParserRequestTypes
import com.adm.url_parser.commons.network.UrlParserNetworkClient
import com.adm.url_parser.commons.network.UrlParserNetworkResponse
import com.adm.url_parser.impls.main_sites.insta.InstaDownloaderMain
import com.adm.url_parser.interfaces.ApiLinkScrapper
import com.adm.url_parser.models.MediaTypeData
import com.adm.url_parser.models.ParsedQuality
import com.adm.url_parser.models.ParsedVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

class FbSimpleShareLinkScrapper(
    private val downloaderMain: InstaDownloaderMain
) : ApiLinkScrapper {

    companion object {
        private const val TAG = "FbRedirectedShareLinkScrapper"
    }

    override suspend fun scrapeLink(url: String): Result<ParsedVideo?> {
        Log.d(TAG, "scrapeLink: url=$url")
        return withContext(Dispatchers.IO) {
            try {

                // ── Step 1: Hit the original redirected share URL ─────────────
                val firstHtml = fetchHtml(url)
                    ?: return@withContext Result.failure(
                        Exception("Empty response for url=$url")
                    )

                // ── Step 2: Extract actorId + storyFBID ──────────────────────
                // FB uses "storyFBID" on some posts and "story_fbid" on others
                val storyFBID = firstHtml
                    .substringAfter("\"storyFBID\":\"", missingDelimiterValue = "")
                    .substringBefore("\"")
                    .toLongOrNull()
                    ?: firstHtml
                        .substringAfter("\"story_fbid\":\"", missingDelimiterValue = "")
                        .substringBefore("\"")
                        .toLongOrNull()


                // ActorId: try base64 storyID first, fall back to "id" field adjacent to "story_fbid"
                // Format 1 (base64): "S:_I100007023257832:VK:4316473115347483"
                // Format 2 (direct): "story_fbid":"1831925424170660","id":"100020596597523"
                val actorId = firstHtml
                    .substringAfter("\"storyID\":\"", missingDelimiterValue = "")
                    .substringBefore("\"")
                    .decodeBase64ActorId()
                    ?: firstHtml
                        .substringAfter("\"story_fbid\":\"", missingDelimiterValue = "")
                        .substringAfter("\"id\":\"", missingDelimiterValue = "")
                        .substringBefore("\"")
                        .toLongOrNull()

                Log.d(TAG, "actorId=$actorId storyFBID=$storyFBID")

                if (actorId == null || storyFBID == null) {
                    return@withContext Result.failure(
                        Exception("Parsing failed: actorId=$actorId storyFBID=$storyFBID for url=$url")
                    )
                }

                // ── Step 3: Hit the resolved FB post URL ─────────────────────
                val secondUrl = "https://www.facebook.com/$actorId/posts/$storyFBID"
                Log.d(TAG, "secondUrl=$secondUrl")

                val secondHtml = fetchHtml(
                    url = secondUrl,
                    headers = mapOf(
                        "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8"
                    )
                ) ?: return@withContext Result.failure(
                    Exception("Empty response for secondUrl=$secondUrl")
                )

                // ── Step 4: Case A — Cross-posted Instagram Reel ─────────────
                val instagramReelId = secondHtml
                    .substringAfter(
                        "https:\\/\\/www.instagram.com\\/reel\\/",
                        missingDelimiterValue = ""
                    )
                    .substringBefore("\\/\"")
                    .takeIf { it.isNotBlank() && it.length <= 20 && it.none { c -> c.isWhitespace() } }

                if (!instagramReelId.isNullOrBlank()) {
                    val reelUrl = "https://www.instagram.com/reel/$instagramReelId/"
                    Log.d(TAG, "Case A — Instagram reel: reelUrl=$reelUrl")
                    val model = downloaderMain.scrapeLink(reelUrl)
                    Log.d(TAG, "Case A — Instagram reel: Model=$model")
                    return@withContext model
                }

                // ── Step 5: Case B — Native Facebook Photo / Video post ───────
                val ogImageUrl = secondHtml
                    .substringAfter("og:image\" content=\"", missingDelimiterValue = "")
                    .substringBefore("\"")
                    .replace("&amp;", "&")
                    .takeIf { it.startsWith("http") }

                if (!ogImageUrl.isNullOrBlank()) {
                    val mediaType = secondHtml.detectFbMediaType()
                    val title = secondHtml.extractFbTitle()
                    val model = ParsedVideo(
                        qualities = listOf(
                            ParsedQuality(
                                url = ogImageUrl,
                                name = "HD",
                                mediaType = mediaType
                            )
                        ),
                        title = title,
                        thumbnail = ogImageUrl
                    )
                    Log.d(TAG, "Case B — Native FB post: Model=$model")
                    return@withContext Result.success(model)
                }

                // ── Step 6: Nothing extracted ─────────────────────────────────
                Log.w(TAG, "No content extracted from secondUrl=$secondUrl")
                Result.failure(Exception("Could not extract any media from url=$url"))

            } catch (e: CancellationException) {
                // Always re-throw CancellationException so coroutines cancel cleanly
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error for url=$url", e)
                Result.failure(e)
            }
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private suspend fun fetchHtml(
        url: String,
        headers: Map<String, String> = emptyMap()
    ): String? {
        val response = UrlParserNetworkClient.makeNetworkRequestString(
            url = url,
            requestType = ParserRequestTypes.Get,
            headers = headers
        )
        return when (response) {
            is UrlParserNetworkResponse.Failure -> {
                Log.e(TAG, "Network failure for url=$url error=${response.error}")
                null
            }

            else -> response.data?.takeIf { it.isNotBlank() }
        }
    }

    /**
     * FB storyID is base64-encoded.
     * Decodes to formats like:
     *   "S:_I100007023257832:VK:4316473115347483"  (groups/pages)
     *   "S:_I100020596597523:1831925424170660"      (personal profiles)
     * Actor ID always follows "_I" up to the next ":"
     *
     * If base64 decode fails or "_I" is missing, caller falls back to
     * extracting the "id" field adjacent to "story_fbid" in the HTML.
     */
    private fun String.decodeBase64ActorId(): Long? {
        if (isBlank()) return null
        return try {
            val decoded = String(
                android.util.Base64.decode(this, android.util.Base64.DEFAULT)
            )
            decoded
                .substringAfter("_I", missingDelimiterValue = "")
                .substringBefore(":")
                .toLongOrNull()
        } catch (_: IllegalArgumentException) {
            Log.e(TAG, "Base64 decode failed for storyID=$this")
            null
        }
    }

    /**
     * Determines media type from FB HTML __typename field.
     * "Photo" → Image  |  "Video" → Video  |  fallback → Image
     */
    private fun String.detectFbMediaType(): MediaTypeData {
        val typename = substringAfter(
            "\"__typename\":\"",
            missingDelimiterValue = ""
        ).substringBefore("\"")

        return when {
            typename.contains("Video", ignoreCase = true) -> MediaTypeData.Video
            typename.contains("Audio", ignoreCase = true) -> MediaTypeData.Audio
            else -> MediaTypeData.Image
        }
    }

    /**
     * Extracts post title from og:title, falling back to og:image:alt.
     */
    private fun String.extractFbTitle(): String? {
        return substringAfter("og:title\" content=\"", missingDelimiterValue = "")
            .substringBefore("\"")
            .replace("&amp;", "&")
            .takeIf { it.isNotBlank() }
            ?: substringAfter("og:image:alt\" content=\"", missingDelimiterValue = "")
                .substringBefore("\"")
                .takeIf { it.isNotBlank() }
    }
}