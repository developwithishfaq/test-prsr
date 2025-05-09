package com.adm.url_parser.impls.main_sites.ted

import android.util.Log
import com.adm.url_parser.commons.network.ParserRequestTypes
import com.adm.url_parser.commons.network.UrlParserNetworkClient
import com.adm.url_parser.impls.main_sites.ted.model.TedResponseGraphQl
import com.adm.url_parser.interfaces.ApiLinkScrapper
import com.adm.url_parser.models.MediaTypeData
import com.adm.url_parser.models.ParsedQuality
import com.adm.url_parser.models.ParsedVideo
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

class TedScrapper : ApiLinkScrapper {
    override suspend fun scrapeLink(url: String): Result<ParsedVideo?> {
        val model = withContext(Dispatchers.IO) {
            val videoId = getVideoId(url) ?: ""
            Log.d("cvv", "scrape: called $url")
            try {
                val response =
                    UrlParserNetworkClient.getClient().post("https://www.ted.com/graphql") {
                        contentType(ContentType.Application.Json)
                        // headers
                        headers {
                            append(HttpHeaders.Accept, "*/*")
                            append("client-id", "Zenith production")
                            append("x-operation-name", "shareLinks")
                            append("origin", "https://www.ted.com")
//                    append("referer", "https://www.ted.com/talks/xiye_bastida_your_inner_fire_is_your_greatest_strength")
                            append(
                                "user-agent",
                                "Mozilla/5.0 (Linux; Android 9; SM-G977N Build/LMY48Z) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/135.0.7049.111 Mobile Safari/537.36"
                            )
                            // Optional: other headers if you need to replicate the curl fully
                        }

                        // if you need cookies (use string from your curl if necessary)
                        header(HttpHeaders.Cookie, "_li_dcdm_c=.ted.com; ...etc...")
                        // GraphQL request body
                        setBody(
                            buildJsonObject {
                                put("operationName", "shareLinks")
                                putJsonObject("variables") {
//                            put("slug", "xiye_bastida_your_inner_fire_is_your_greatest_strength")
                                    put("slug", videoId)
                                    put("language", "en")
                                }
                                put(
                                    "query", """
                    query shareLinks(${'$'}slug: String!, ${'$'}language: String) {
                      videos(
                        slug: [${'$'}slug]
                        language: ${'$'}language
                        first: 1
                        isPublished: [true, false]
                        channel: ALL
                      ) {
                        nodes {
                          id
                          canonicalUrl
                          audioDownload
                          nativeDownloads {
                            low
                            medium
                            high
                            __typename
                          }
                          subtitledDownloads {
                            low
                            high
                            internalLanguageCode
                            languageName
                            __typename
                          }
                          __typename
                        }
                        __typename
                      }
                    }
                """.trimIndent()
                                )
                            }
                        )
                    }

                val responseBody = response.bodyAsText()


                Log.d("cvv", "scrape: $responseBody")
                val model = UrlParserNetworkClient.jsonHelper.decodeFromString<TedResponseGraphQl?>(
                    responseBody
                )
                val node = model?.data?.videos?.nodes?.getOrNull(0)
                val downloadingLinks = node?.nativeDownloads
                val qualities = mutableListOf<ParsedQuality>()
                if (downloadingLinks != null) {
                    val high = getModel(downloadingLinks.high, "High")
                    val medium = getModel(downloadingLinks.medium, "Medium")
                    val low = getModel(downloadingLinks.low, "Low")
                    if (high != null) {
                        qualities.add(high)
                    }
                    if (medium != null) {
                        qualities.add(medium)
                    }
                    if (low != null) {
                        qualities.add(low)
                    }
                }
                val modelLast = if (qualities.isNotEmpty()) {
                    ParsedVideo(
                        title = "Ted Video",
                        qualities = qualities,
                    )
                } else {
                    null
                }
                Log.d("cvv", "scrapeLink response ted(${modelLast?.qualities?.size}):$modelLast")
                modelLast
            } catch (e: Exception) {
                Log.d("cvv", "scrapeLink:${e.message}")
                null
            }
        }
        return if (model != null) {
            Result.success(model)
        } else {
            Result.failure(Exception("No data found"))
        }
    }

    private suspend fun getVideoId(url: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val lastPart: String =
                    url.substringAfter("https://www.ted.com/talks/").substringBefore("/")
                val videoId = if (lastPart.toLongOrNull() != null) {
                    val response = UrlParserNetworkClient.makeNetworkRequestString(
                        url,
                        ParserRequestTypes.Get
                    ).data ?: ""
                    val id =
                        response.substringAfter("<link rel=\"canonical\" href=\"")
                            .substringBefore("\"")
                    Log.d("cvv", "scrapeLink ($id):$response")
                    id.substringAfter("https://www.ted.com/talks/").substringBefore("/")
                } else {
                    lastPart
                }
                videoId
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun getModel(link: String?, name: String): ParsedQuality? {
        return link?.let {
            ParsedQuality(
                url = link,
                name = name,
                mediaType = MediaTypeData.Video
            )
        }
    }
}