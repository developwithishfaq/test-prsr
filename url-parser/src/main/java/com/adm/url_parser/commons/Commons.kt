package com.adm.url_parser.commons

object Commons {
    fun String.removeUnnecessarySlashes(): String {
        return this.replace("\\/", "/")
    }

    fun String.getTitleFromHtml(): String {
        return this.substringAfter("<title>").substringBefore("</title>")
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