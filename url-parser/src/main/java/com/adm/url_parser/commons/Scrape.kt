package com.adm.url_parser.commons

import com.adm.url_parser.commons.network.UrlParserNetworkClient
import com.adm.url_parser.models.MediaTypeData
import com.adm.url_parser.models.ParsedQuality
import com.adm.url_parser.models.ParsedVideo
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import org.jsoup.parser.Parser

/** Shared plumbing for the plain-HTTP scrapers (no android.* so they run in JVM tests). */
object Scrape {
    const val UA_DESKTOP =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
    const val UA_IPHONE =
        "Mozilla/5.0 (iPhone; CPU iPhone OS 17_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Mobile/15E148 Safari/604.1"
    const val UA_ANDROID =
        "Mozilla/5.0 (Linux; Android 13; SM-S911B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36"

    class Page(val url: String, val body: String)

    /** GET following redirects; [Page.url] is the final URL. Null on non-2xx or network error. */
    suspend fun get(
        url: String,
        ua: String = UA_DESKTOP,
        headers: Map<String, String> = emptyMap(),
    ): Page? = withContext(Dispatchers.IO) {
        try {
            val res = UrlParserNetworkClient.getClient().get(url) {
                header(HttpHeaders.UserAgent, ua)
                header(HttpHeaders.Accept, "text/html,application/xhtml+xml,application/json;q=0.9,*/*;q=0.8")
                header(HttpHeaders.AcceptLanguage, "en-US,en;q=0.9")
                headers.forEach { (k, v) -> header(k, v) }
            }
            if (res.status.isSuccess()) Page(res.request.url.toString(), res.bodyAsText()) else null
        } catch (_: Exception) {
            null
        }
    }

    /** The JSON object/array that starts at the first `{` or `[` after [marker] (string-aware brace match). */
    fun jsonAfter(text: String, marker: String): JsonElement? {
        val m = text.indexOf(marker)
        if (m < 0) return null
        val start = text.indexOfAny(charArrayOf('{', '['), m + marker.length)
        if (start < 0) return null
        var depth = 0
        var inStr = false
        var esc = false
        for (i in start until text.length) {
            val c = text[i]
            if (inStr) {
                if (esc) esc = false else if (c == '\\') esc = true else if (c == '"') inStr = false
                continue
            }
            when (c) {
                '"' -> inStr = true
                '{', '[' -> depth++
                '}', ']' -> if (--depth == 0) return parse(text.substring(start, i + 1))
            }
        }
        return null
    }

    /** Lenient parse; JS literals like `undefined` become null. */
    fun parse(json: String): JsonElement? = try {
        Json.parseToJsonElement(json.replace(Regex(""":\s*undefined\b"""), ":null"))
    } catch (_: Exception) {
        null
    }

    fun metaContent(html: String, property: String): String? =
        Regex("""<meta[^>]+(?:property|name)=["']${Regex.escape(property)}["'][^>]+content=["']([^"']+)["']""")
            .find(html)?.groupValues?.get(1)?.let { Parser.unescapeEntities(it, true) }

    fun video(url: String, name: String = "HD") = ParsedQuality(url = url, name = name, mediaType = MediaTypeData.Video)
    fun image(url: String, name: String = "Image") = ParsedQuality(url = url, name = name, mediaType = MediaTypeData.Image)

    fun result(
        qualities: List<ParsedQuality>,
        title: String? = null,
        thumbnail: String? = null,
        headers: Map<String, String>? = null,
    ): Result<ParsedVideo?> {
        val distinct = qualities.filter { it.url.startsWith("http") }.distinctBy { it.url }
        return if (distinct.isEmpty()) Result.failure(Exception("No media found"))
        else Result.success(ParsedVideo(distinct, title?.take(150), thumbnail, headers = headers))
    }
}

// Null-safe JSON navigation.
fun JsonElement?.obj(key: String): JsonObject? = (this as? JsonObject)?.get(key) as? JsonObject
fun JsonElement?.arr(key: String): JsonArray? = (this as? JsonObject)?.get(key) as? JsonArray
fun JsonElement?.str(key: String): String? =
    ((this as? JsonObject)?.get(key) as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotBlank() && it != "null" }
fun JsonElement?.int(key: String): Int? = str(key)?.toDoubleOrNull()?.toInt()
fun JsonElement?.at(index: Int): JsonElement? = (this as? JsonArray)?.getOrNull(index)
fun JsonElement?.content(): String? = (this as? JsonPrimitive)?.contentOrNull
