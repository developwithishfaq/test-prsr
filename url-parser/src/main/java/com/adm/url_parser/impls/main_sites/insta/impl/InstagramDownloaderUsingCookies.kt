package com.adm.url_parser.impls.main_sites.insta.impl

import android.util.Log
import android.webkit.CookieManager
import com.adm.url_parser.commons.Commons.isThreadsMediaUrl
import com.adm.url_parser.models.MediaTypeData
import com.adm.url_parser.models.ParsedQuality
import com.adm.url_parser.models.ParsedVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Instagram media downloader that extracts media URLs from Instagram posts/reels
 * Uses WebView cookies for authentication
 */
class InstagramDownloaderUsingCookies {
    data class CookiesScrappingModel(
        val mediaTitle: String,
        val mediaUrl: String,
        val mediaType: MediaTypeData,
        val thumbnailUrl: String? = null,
    )

    companion object {
        private const val TAG = "InstagramDownloaderUsingCookies"
        private const val MOBILE_USER_AGENT =
            "Mozilla/5.0 (Linux; U; Android 9; en-us; SM-G977N Build/JOP24G) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 " +
                    "Chrome/95.0.4638.74 Mobile Safari/537.36"
    }

    /**
     * Download Instagram media from a given URL
     * @param reelUrl The Instagram post/reel or threads URL
     * @return ParsedQuality object or null if extraction fails
     */
    suspend fun scrapeLink(reelUrl: String): Result<ParsedVideo?> {
        return withContext(Dispatchers.IO) {
            try {
                val shortcode = extractShortcode(reelUrl)
                if (shortcode.isEmpty()) {
                    Log.e(TAG, "Invalid URL: $reelUrl")
                    return@withContext Result.failure(Exception("Invalid URL"))
                }

                val client = createOkHttpClient()
                val url =
                    if (reelUrl.isThreadsMediaUrl()) reelUrl else "https://www.instagram.com/reel/$shortcode/"

                Log.d(TAG, "Fetching: $url")

                val request = buildRequest(url)
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    Log.e(TAG, "HTTP ${response.code}: ${response.message}")
                    return@withContext Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
                }

                val html = response.body.string()
                Log.d(TAG, "Response received: ${html.length} characters")

                if (!isValidHtml(html)) {
                    Log.e(TAG, "Invalid HTML response")
                    return@withContext Result.failure(Exception("Invalid HTML response"))
                }

                val result = extractMediaFromHtml(html)
                Log.d(TAG, "Result from InstagramDownloaderUsingCookies=${result.getOrNull()}")
                result

            } catch (e: Exception) {
                Log.e(TAG, "Error downloading media: ${e.message}", e)
                Result.failure(Exception("Error downloading media: ${e.message}"))
            }
        }
    }

    /**
     * Extract media data from HTML with parallel extraction
     */
    private suspend fun extractMediaFromHtml(html: String): Result<ParsedVideo?> = coroutineScope {
        try {
            // Launch parallel extraction tasks
            val webInfoDeferred = async { extractFromWebInfo(html) }
            val videoDirectDeferred = async { extractVideoDirectly(html) }
            val imageDirectDeferred = async { extractImageDirectly(html) }
            val captionDeferred = async { extractCaption(html) }

            // Wait for all results
            val webInfoModel = webInfoDeferred.await()
            val videoDirectModel = videoDirectDeferred.await()
            val imageDirectModel = imageDirectDeferred.await()
            val caption = captionDeferred.await()

            // Determine the best model with priority: webInfo > videoDirect > imageDirect
            val model = when {
                webInfoModel != null -> webInfoModel
                videoDirectModel != null -> {
                    // If we have video but no thumbnail, use image as thumbnail
                    if (videoDirectModel.thumbnailUrl == null && imageDirectModel != null) {
                        videoDirectModel.copy(thumbnailUrl = imageDirectModel.mediaUrl)
                    } else {
                        videoDirectModel
                    }
                }

                imageDirectModel != null -> imageDirectModel
                else -> null
            }

            if (model != null && model.mediaUrl.isNotEmpty()) {
                // Use extracted caption if model's caption is generic
                val finalTitle = if (model.mediaTitle == "Unknown" && caption != null) {
                    caption
                } else {
                    model.mediaTitle
                }

                val parsedQuality = ParsedQuality(
                    model.mediaUrl,
                    name = "HD",
                    mediaType = model.mediaType
                )
                val videoModel = ParsedVideo(
                    qualities = listOf(parsedQuality),
                    title = finalTitle,
                    thumbnail = model.thumbnailUrl
                )
                Result.success(videoModel)
            } else {
                Log.w(TAG, "No media found in HTML")
                Result.failure(Exception("No media found in HTML"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting media: ${e.message}", e)
            Result.failure(Exception("No media found in HTML"))
        }
    }

    /**
     * Extract media from xdt_api__v1__media__shortcode__web_info JSON
     * Enhanced to extract thumbnail for videos
     */
    private fun extractFromWebInfo(html: String): CookiesScrappingModel? {
        val pattern =
            """"xdt_api__v1__media__shortcode__web_info":\s*\{[^}]*"items":\s*\[(\{.+?\})\]""".toRegex(
                RegexOption.DOT_MATCHES_ALL
            )

        val match = pattern.find(html) ?: return null

        val itemsJson = match.groupValues[1]
        Log.d(TAG, "Found xdt_api JSON data (${itemsJson.length} chars)")

        return parseMediaJson(itemsJson, html)
    }

    /**
     * Extract video URL directly from HTML
     */
    private fun extractVideoDirectly(html: String): CookiesScrappingModel? {
        val pattern = """"video_versions":\s*\[\s*\{[^}]*"url":\s*"([^"]+)"""".toRegex()

        val match = pattern.find(html) ?: return null

        val videoUrl = match.groupValues[1].cleanUrl()
        if (videoUrl.isEmpty()) return null
        Log.d(TAG, "✓ Found video URL (direct): $videoUrl")

        // Try to extract caption (will be replaced by parallel extraction if needed)
        val caption = "Unknown"

        // Try to extract thumbnail for video
        val thumbnailUrl = extractThumbnailUrl(html)
        Log.d(TAG, "✓ Thumbnail for video: ${thumbnailUrl ?: "not found"}")

        return CookiesScrappingModel(
            mediaUrl = videoUrl,
            mediaTitle = caption,
            mediaType = MediaTypeData.Video,
            thumbnailUrl = thumbnailUrl
        )
    }

    /**
     * Extract image URL directly from HTML
     */
    private fun extractImageDirectly(html: String): CookiesScrappingModel? {
        val pattern =
            """"image_versions2":\s*\{[^}]*"candidates":\s*\[\s*\{[^}]*"url":\s*"([^"]+)"""".toRegex()
        val match = pattern.find(html) ?: return null

        val imageUrl = match.groupValues[1].cleanUrl()
        Log.d(TAG, "✓ Found image URL (direct): $imageUrl")
        if (imageUrl.isEmpty()) return null

        return CookiesScrappingModel(
            mediaUrl = imageUrl,
            mediaTitle = "Unknown",
            mediaType = MediaTypeData.Image,
            thumbnailUrl = imageUrl // For images, thumbnail is the same
        )
    }

    /**
     * Extract thumbnail URL from HTML (for videos)
     */
    private fun extractThumbnailUrl(html: String): String? {
        // Try multiple patterns for thumbnail extraction
        val patterns = listOf(
            // Pattern 1: image_versions2 candidates (common in both Instagram and Threads)
            """"image_versions2":\s*\{[^}]*"candidates":\s*\[\s*\{[^}]*"url":\s*"([^"]+)"""".toRegex(),

            // Pattern 2: display_url (Instagram specific)
            """"display_url":\s*"([^"]+)"""".toRegex(),

            // Pattern 3: thumbnail_url
            """"thumbnail_url":\s*"([^"]+)"""".toRegex(),

            // Pattern 4: og:image meta tag
            """<meta\s+property="og:image"\s+content="([^"]+)"""".toRegex()
        )

        for (pattern in patterns) {
            val match = pattern.find(html)
            if (match != null) {
                val thumbnailUrl = match.groupValues[1].cleanUrl()
                if (thumbnailUrl.isNotEmpty() && thumbnailUrl.contains("http")) {
                    Log.d(TAG, "✓ Found thumbnail via pattern: ${pattern.pattern.take(50)}")
                    return thumbnailUrl
                }
            }
        }

        return null
    }

    /**
     * Create OkHttp client with WebView cookie support
     */
    private fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .cookieJar(WebViewCookieJar())
            .followRedirects(true)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Build HTTP request with proper headers
     */
    private fun buildRequest(url: String): Request {
        return Request.Builder()
            .url(url)
            .header("User-Agent", MOBILE_USER_AGENT)
            .header(
                "Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8"
            )
            .header("Accept-Language", "en")
            .header("Connection", "Keep-Alive")
            .header("sec-fetch-mode", "cors")
            .header("sec-fetch-site", "same-origin")
            .build()
    }

    /**
     * Extract shortcode from Instagram URL
     */
    private fun extractShortcode(url: String): String {
        return when {
            url.isThreadsMediaUrl() -> url
            url.contains("/reel/") -> url.substringAfter("/reel/").substringBefore("/")
            url.contains("/reels/") -> url.substringAfter("/reels/").substringBefore("/")
            url.contains("/p/") -> url.substringAfter("/p/").substringBefore("/")
            url.contains("/tv/") -> url.substringAfter("/tv/").substringBefore("/")
            else -> url
        }.trim()
    }

    /**
     * Validate HTML response
     */
    private fun isValidHtml(html: String): Boolean {
        return html.startsWith("<!DOCTYPE") || html.startsWith("<html")
    }

    /**
     * Extract caption from JSON with better logging
     */
    private fun extractCaptionFromJson(json: String): String? {
        Log.d(TAG, "Searching for caption in JSON (${json.length} chars)...")

        // Method 1: Try caption.text
        val captionPattern = """"caption":\s*\{[^}]*"text":\s*"([^"]+)"""".toRegex()
        captionPattern.find(json)?.let { match ->
            val caption = match.groupValues[1]
                .replace("\\n", "\n")
                .decodeUnicode()
                .trim()

            if (caption.isNotEmpty()) {
                Log.d(TAG, "✓ Found caption via caption.text: ${caption.take(50)}...")
                return caption
            }
        }

        // Method 2: Try accessibility_caption
        val accessibilityPattern = """"accessibility_caption":\s*"([^"]+)"""".toRegex()
        accessibilityPattern.find(json)?.let { match ->
            val caption = match.groupValues[1]
                .replace("\\n", "\n")
                .decodeUnicode()
                .trim()

            if (caption.isNotEmpty() && caption.length > 20) {
                Log.d(TAG, "✓ Found caption via accessibility_caption: ${caption.take(50)}...")
                return caption
            }
        }

        // Method 3: Try edge_media_to_caption
        val edgePattern = """"edge_media_to_caption":\s*\{[^}]*"text":\s*"([^"]+)"""".toRegex()
        edgePattern.find(json)?.let { match ->
            val caption = match.groupValues[1]
                .replace("\\n", "\n")
                .decodeUnicode()
                .trim()

            if (caption.isNotEmpty()) {
                Log.d(TAG, "✓ Found caption via edge_media_to_caption: ${caption.take(50)}...")
                return caption
            }
        }

        // Method 4: Try any "text" field with substantial content
        val anyTextPattern = """"text":\s*"([^"]{20,})"""".toRegex()
        anyTextPattern.find(json)?.let { match ->
            val caption = match.groupValues[1]
                .replace("\\n", "\n")
                .decodeUnicode()
                .trim()

            if (caption.isNotEmpty() &&
                !caption.contains("Photo by") &&
                !caption.contains("Video by") &&
                !caption.startsWith("@")
            ) {
                Log.d(TAG, "✓ Found caption via any text field: ${caption.take(50)}...")
                return caption
            }
        }

        Log.d(TAG, "✗ No caption found in JSON")
        return null
    }

    /**
     * Extract caption from full HTML with more methods
     */
    private fun extractCaption(html: String): String? {
        Log.d(TAG, "Searching for caption in HTML...")

        val patterns = listOf(
            // Method 1: JSON caption.text
            """"caption":\s*\{[^}]*"text":\s*"([^"]+)"""".toRegex() to "caption.text",

            // Method 2: accessibility_caption
            """"accessibility_caption":\s*"([^"]+)"""".toRegex() to "accessibility_caption",

            // Method 3: edge_media_to_caption
            """"edge_media_to_caption":\s*\{[^}]*"text":\s*"([^"]+)"""".toRegex() to "edge_media_to_caption",

            // Method 4: OG meta tags
            """<meta property="og:title" content="([^"]+)"""".toRegex() to "og:title",
            """<meta property="og:description" content="([^"]+)"""".toRegex() to "og:description",
            """<meta name="description" content="([^"]+)"""".toRegex() to "meta:description",

            // Method 5: Twitter meta tags
            """<meta name="twitter:title" content="([^"]+)"""".toRegex() to "twitter:title",
            """<meta name="twitter:description" content="([^"]+)"""".toRegex() to "twitter:description"
        )

        for ((pattern, source) in patterns) {
            val match = pattern.find(html)
            if (match != null) {
                val caption = match.groupValues[1]
                    .replace("\\n", "\n")
                    .replace("&quot;", "\"")
                    .replace("&amp;", "&")
                    .replace("&#39;", "'")
                    .replace("&#064;", "@")
                    .decodeUnicode()
                    .trim()

                // Filter out useless captions
                if (caption.isNotEmpty() &&
                    caption.length > 10 &&
                    !caption.startsWith("@") &&
                    !caption.contains("Instagram") &&
                    !caption.contains(" on Instagram:") &&
                    !caption.contains(" on Threads") &&
                    !caption.contains("Followers") &&
                    !caption.contains("Following")
                ) {
                    Log.d(TAG, "✓ Found caption via $source: ${caption.take(50)}...")
                    return caption
                } else {
                    Log.d(TAG, "✗ Caption from $source too short or invalid: $caption")
                }
            }
        }

        Log.d(TAG, "✗ No caption found anywhere in HTML")
        return null
    }

    /**
     * Parse media JSON to extract URLs and metadata including thumbnail
     */
    private fun parseMediaJson(json: String, html: String): CookiesScrappingModel? {
        try {
            // Determine media type - video takes priority
            val hasVideo = json.contains("video_versions")
            val hasCarousel = json.contains("carousel_media")

            val mediaType = when {
                hasVideo -> MediaTypeData.Video
                hasCarousel -> MediaTypeData.Image
                else -> MediaTypeData.Image
            }

            // Extract URL based on media type
            val url = if (mediaType == MediaTypeData.Video) {
                extractVideoUrl(json)
            } else {
                extractImageUrl(json)
            }

            if (url == null) {
                Log.w(TAG, "Could not extract URL from JSON")
                return null
            }

            // Extract thumbnail (especially important for videos)
            val thumbnailUrl = if (mediaType == MediaTypeData.Video) {
                extractThumbnailFromJson(json) ?: extractThumbnailUrl(html)
            } else {
                url // For images, thumbnail is the same as the image
            }

            // Extract caption
            val captionFromJson = extractCaptionFromJson(json)
            val captionFromHtml = extractCaption(html)
            val caption = captionFromJson ?: captionFromHtml ?: "Unknown"

            Log.d(TAG, "✓ Successfully parsed ${mediaType.name}: $url")
            if (thumbnailUrl != null) {
                Log.d(TAG, "✓ Thumbnail: $thumbnailUrl")
            }

            return CookiesScrappingModel(
                mediaUrl = url,
                mediaTitle = caption,
                mediaType = mediaType,
                thumbnailUrl = thumbnailUrl
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error parsing media JSON: ${e.message}", e)
            return null
        }
    }

    /**
     * Extract thumbnail from JSON
     */
    private fun extractThumbnailFromJson(json: String): String? {
        // Try to find image_versions2 which contains thumbnail candidates
        val pattern =
            """"image_versions2":\s*\{[^}]*"candidates":\s*\[\s*\{[^}]*"url":\s*"([^"]+)"""".toRegex()
        val match = pattern.find(json) ?: return null
        return match.groupValues[1].cleanUrl().takeIf { it.isNotEmpty() }
    }

    /**
     * Extract video URL from JSON
     */
    private fun extractVideoUrl(json: String): String? {
        val pattern = """"url":\s*"([^"]*\.mp4[^"]*)"""".toRegex()
        val match = pattern.find(json) ?: return null
        return match.groupValues[1].cleanUrl()
    }

    /**
     * Extract image URL from JSON
     */
    private fun extractImageUrl(json: String): String? {
        val pattern =
            """"image_versions2":\s*\{[^}]*"candidates":\s*\[\s*\{[^}]*"url":\s*"([^"]+)"""".toRegex()
        val match = pattern.find(json) ?: return null
        return match.groupValues[1].cleanUrl()
    }

    /**
     * Clean URL by removing escape characters and decoding
     */
    private fun String.cleanUrl(): String {
        return this
            .replace("\\/", "/")
            .replace("\\u0026", "&")
            .replace("&amp;", "&")
            .replace("\\u00253D", "=")
            .replace("\\u0025", "%")
            .replace("u00253D", "%3D")
            .replace("u00252", "%2")
            .replace("\\\"", "\"")
            .replace("\\\\", "")
            .trim()
    }

    /**
     * Decode unicode escape sequences
     */
    private fun String.decodeUnicode(): String {
        val pattern = """\\u([0-9a-fA-F]{4})""".toRegex()
        var result = this
        pattern.findAll(this).forEach { match ->
            val unicode = match.groupValues[1]
            val char = unicode.toInt(16).toChar()
            result = result.replace(match.value, char.toString())
        }
        return result
    }

    /**
     * CookieJar implementation that uses WebView cookies
     */
    class WebViewCookieJar : CookieJar {
        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            val cookiesString = CookieManager.getInstance().getCookie(url.toString())

            if (cookiesString.isNullOrEmpty()) {
                Log.d(TAG, "No cookies found for: ${url.host}")
                return emptyList()
            }

            Log.d(TAG, "Loading ${cookiesString.split(";").size} cookies for: ${url.host}")

            return cookiesString.split(";").mapNotNull { cookie ->
                Cookie.parse(url, cookie.trim())
            }
        }

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            // Let WebView manage cookies - no need to save them manually
        }
    }
}

