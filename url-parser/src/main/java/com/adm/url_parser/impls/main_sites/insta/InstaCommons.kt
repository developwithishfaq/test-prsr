package com.adm.url_parser.impls.main_sites.insta

import org.json.JSONObject

object InstaCommons {

    fun String.extractReelId(): String? {
        val regex = """https://www\.instagram\.com/reel[s]?/([a-zA-Z0-9_-]+)/?.*""".toRegex()
        val matchResult = regex.find(this)
        return matchResult?.groups?.get(1)?.value?.replace("reels", "reel")
    }

    fun String.getInstagramUrlId(): String {
        return if (contains("reel/")) {
            substringAfter("reel/").substringBefore("/")
        } else if (contains("reels/")) {
            substringAfter("reels/").substringBefore("/")
        } else if (contains("p/")) {
            substringAfter("p/").substringBefore("/")
        } else {
            this
        }
    }

    fun JSONObject.getStringSafe(key: String) = try {
        getString(key)
    } catch (_: Exception) {
        null
    }


    fun String.purifyFrom00253D(): String {
        return replace("u00253D", "%3D")
            .replace("u00252", "%2")
    }

    fun String.purifyInstagramUrl(includeEmbded: Boolean = true): String {
        val newUrl = if (contains("/p/")) {
            replace("/p/", "/reel/")
        } else {
            this
        }.replace("reels", "reel")
        val regex = if (newUrl.contains("reels")) {
            "(https://www.instagram.com/reels/[^/?]+)".toRegex()
        } else {
            "(https://www.instagram.com/reel/[^/?]+)".toRegex()
        }
        val match = regex.find(newUrl)
        return (match?.value
            ?: newUrl) + if (includeEmbded) {
            "/embed/"
        } else {
            ""
        }
    }
}