package com.adm.url_parser.commons

import android.util.Log
import android.webkit.CookieManager

object Commons {
    fun String.removeUnnecessarySlashes(): String {
        return this.replace("\\/", "/")
    }

    fun String.getTitleFromHtml(): String {
        return this.substringAfter("<title>").substringBefore("</title>")
    }

    fun String.isAUrl(): Boolean {
        return startsWith("http") || contains("www.") || contains(".com") || contains(".pk")
    }
    fun String.isImageUrl(): Boolean {
        val lower = lowercase()
        return listOf(".jpg", ".jpeg", ".png", ".webp").any { lower.contains(it) }
    }

    fun String.isVideoUrl(): Boolean {
        val lower = lowercase()
        return listOf(".mp4", ".mov", ".mkv", ".webm").any { lower.contains(it) }
    }
    private fun removeUnicodeEscapeSequences(input: String): String {
        val regex = Regex("\\\\u[0-9a-fA-F]{4}")
        return regex.replace(input, "")
            .replace("\n+".toRegex(), replacement = "\n")
    }

    fun String.cleanTitle(): String {
        return removeUnicodeEscapeSequences(this)
    }

    fun findCookiesOfUrl(url: String = "https://www.instagram.com"): Boolean {
        val cookies = try {
            CookieManager.getInstance().getCookie(url)
        } catch (_: Exception) {
            null
        }
        Log.d("COOKIES", "Cookies for $url are= $cookies")
        return cookies != null
    }
    fun String.isThreadsDirectMp4Url(): Boolean {
        return this.contains("instagram.") &&
                this.contains("fbcdn.net") &&
                this.contains("/o1/v/") &&
                this.contains(".mp4")
    }
    fun String.isThreadsLink(): Boolean {
        return this.contains("threads.net/") || this.contains("threads.com/")
    }

    fun String.isThreadsPostUrl(): Boolean {
        return this.contains("threads.com/@") && this.contains("/post/")
    }
    fun String.isThreadsMediaUrl(): Boolean {
        val regex = Regex("""https?://(www\.)?threads\.com/@[^/]+/post/[^/]+/media.*""")
        return regex.matches(this)
    }
}