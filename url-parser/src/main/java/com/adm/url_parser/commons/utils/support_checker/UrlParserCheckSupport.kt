package com.adm.url_parser.commons.utils.support_checker

interface UrlParserCheckSupport {
    fun isFbLink(url: String): Boolean
    fun isInstaLink(url: String): Boolean
    fun isLinkedInLink(url: String): Boolean
    fun isTiktokLink(url: String): Boolean
    fun isTwitterLink(url: String): Boolean
    fun isBrazzerLink(url: String): Boolean
    fun isXhamsterDesiLink(url: String): Boolean
    fun isInxxLink(url: String): Boolean
    fun isPornHubLink(url: String): Boolean
    fun isDailymotionLink(url: String): Boolean
    fun isDailymotionMetaDataLink(url: String): Boolean
}