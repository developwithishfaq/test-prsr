package com.adm.url_parser.commons

object Commons {
    fun String.removeUnnecessarySlashes(): String {
        return this.replace("\\/", "/")
    }
    fun String.getTitleFromHtml(): String{
        return this.substringAfter("<title>").substringBefore("</title>")
    }
}