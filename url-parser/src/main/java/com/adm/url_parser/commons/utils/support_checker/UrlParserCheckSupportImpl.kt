package com.adm.url_parser.commons.utils.support_checker

class UrlParserCheckSupportImpl : UrlParserCheckSupport {
    override fun isFbLink(url: String): Boolean {
        return (url == "https://m.facebook.com/watch/").not() &&
                (
                        url.contains(".facebook.com/watch/") ||
                                url.contains(".facebook.com/reel/") ||
                                url.contains(".facebook.com/share/v/") ||
                                url.contains("facebook.com/share/r/") ||
                                (
                                        url.contains("facebook.com") &&
                                                url.contains("/videos/")
                                        )
                        )
    }

    override fun isThreadsLink(url: String): Boolean {
        return url.contains("threads.net/") || url.contains("threads.com/")
    }

    override fun isFbRedirectInstaLink(url: String): Boolean {
        return url.contains("facebook.com/share/")
    }

    override fun isInstaLink(url: String): Boolean {
        return url.contains("instagram.com/p/") ||
                url.contains("instagram.com/reel/") ||
                url.contains("instagram.com/reels/")
    }

    //    https://www.linkedin.com/posts/top-edge-technologie_topedgetechnologies-nowhiring-webdeveloper-activity-7318248483960905729-MDQ_?utm_source=social_share_send&utm_medium=android_app&rcm=ACoAACbslWMBNEhIdSLFUGqn_XdEndA-WRbzDq4&utm_campaign=copy_link
    override fun isLinkedInLink(url: String): Boolean {
        return url.contains("linkedin.com/posts/")
    }

    override fun isTiktokLink(url: String): Boolean {
        return url.contains("vt.tiktok.com/") ||
                (url.contains(".tiktok.com/") && url.contains("/video/"))
    }

    override fun isPinterestUrl(url: String): Boolean {
        return url.contains("https://www.pinterest.com/pin/") || url.contains("https://pin.it/")
    }

    override fun isTedLink(url: String): Boolean {
        return url.contains("https://www.ted.com/talks")
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

    override fun isXVideosComUrl(url: String): Boolean {
        return url.contains("xvideos.com/video")

    }

    override fun isXnxUrl(url: String): Boolean {
        return url.contains(".xnxx.com/video-")
    }

    override fun isXnxHealth(url: String): Boolean {
        return url.contains("xnxx.health/video-")
    }

    override fun isInxxLink(url: String): Boolean {
        return url.contains("inxxx.com/v/")
    }

    override fun isPornHubLink(url: String): Boolean {
        return url.contains("pornhub.com/view_video.php?viewkey=") || url.contains("pornhub.com/interstitial?viewkey=")
    }

    override fun isDailymotionLink(url: String): Boolean {
        return url.contains("dailymotion.com/video/")
    }

    override fun isDailymotionMetaDataLink(url: String): Boolean {
        return url.contains("dailymotion.com/player/metadata/video/")
    }

}