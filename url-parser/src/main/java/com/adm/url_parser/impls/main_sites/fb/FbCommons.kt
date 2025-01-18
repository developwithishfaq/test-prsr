package com.adm.url_parser.impls.main_sites.fb

object FbCommons {

    fun String.extractFbVideoId(): String {
        val regex =
            "\\d{11,}".toRegex()  // Regex to match numeric sequences longer than 10 characters
        return regex.findAll(this)
            .joinToString("") { it.value }  // Concatenate all matches into a single string
    }
}