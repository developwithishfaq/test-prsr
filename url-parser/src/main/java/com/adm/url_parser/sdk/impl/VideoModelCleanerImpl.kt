package com.adm.url_parser.sdk.impl

import com.adm.url_parser.commons.Commons.cleanTitle
import com.adm.url_parser.models.UrlParserResponse
import com.adm.url_parser.sdk.interfaces.VideoModelCleaner

class VideoModelCleanerImpl : VideoModelCleaner {
    override fun getCleanedVideoModel(videoModel: UrlParserResponse): UrlParserResponse {
        return if (videoModel.model != null) {
            videoModel.copy(
                model = videoModel.model.copy(
                    title = videoModel.model.title?.cleanTitle()
                )
            )
        } else {
            videoModel
        }
    }
}