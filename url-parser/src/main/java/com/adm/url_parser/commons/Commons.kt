package com.adm.url_parser.commons

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
}