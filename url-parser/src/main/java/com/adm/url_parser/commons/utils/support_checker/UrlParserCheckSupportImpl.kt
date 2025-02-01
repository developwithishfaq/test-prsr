package com.adm.url_parser.commons.utils.support_checker

class UrlParserCheckSupportImpl : UrlParserCheckSupport {
    override fun isFbLink(url: String): Boolean {
        return (url.contains(".facebook.com/watch/") && url.substringAfterLast("/watch/").length > 2) ||
                url.contains(".facebook.com/reel/") ||
                url.contains(".facebook.com/share/v/") ||
                url.contains(".facebook.com/share/r/")
    }

    override fun isInstaLink(url: String): Boolean {
        return url.contains(".instagram.com/p/") ||
                url.contains(".instagram.com/reel/") ||
                url.contains(".instagram.com/reels/")
    }

    override fun isLinkedInLink(url: String): Boolean {
        return false
    }

    override fun isTiktokLink(url: String): Boolean {
        return url.contains("vt.tiktok.com/") ||
                (url.contains(".tiktok.com/") && url.contains("/video/"))
    }

    override fun isTwitterLink(url: String): Boolean {
        return url.contains("x.com/") && url.contains("/status/")
    }

    override fun isBrazzerLink(url: String): Boolean {
        return url.contains(".brazzers.com/video/")
    }

    override fun isXhamsterDesiLink(url: String): Boolean {
        return url.contains("xhamster.desi/videos/")
    }

    override fun isInxxLink(url: String): Boolean {
        return url.contains("inxxx.com/v/")
    }

    override fun isPornHubLink(url: String): Boolean {
        return url.contains("pornhub.com/view_video.php?viewkey=")
    }

    override fun isDailymotionLink(url: String): Boolean {
        return url.contains("dailymotion.com/video/")
    }

    override fun isDailymotionMetaDataLink(url: String): Boolean {
        return url.contains("dailymotion.com/player/metadata/video/")
    }

}