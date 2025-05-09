package com.adm.url_parser.impls.main_sites.pinterest

import android.util.Log
import kotlinx.serialization.Serializable

@Serializable
data class PinterestGraphQlResponse(
    val data: MainDataModel? = null
) {
    fun getTitle() = data?.v3GetPinQuery?.data?.autoAltText
    fun isVideo(): Boolean {
        val url = data?.v3GetPinQuery?.data?.videos?.videoList?.vHLSV4?.url
        val newUrl = if (url.isNullOrBlank()) {
            data?.v3GetPinQuery?.data?.storyPinData?.pages?.getOrNull(0)?.blocks?.getOrNull(0)
                ?.videoDataV2?.videoListMobile?.vHLSV3MOBILE?.url
        } else {
            url
        }
        Log.d("PinterestSupport", "isVideo: $newUrl")
        return newUrl != null
    }

    fun getVideoUrl(): String? {
        val url = data?.v3GetPinQuery?.data?.videos?.videoList?.vHLSV4?.url
        val newUrl = if (url.isNullOrBlank()) {
            data?.v3GetPinQuery?.data?.storyPinData?.pages?.getOrNull(0)?.blocks?.getOrNull(0)
                ?.videoDataV2?.videoListMobile?.vHLSV3MOBILE?.url
        } else {
            url
        }
        Log.d("PinterestSupport", "getVideoUrl: $newUrl")
        return newUrl
    }

    fun getImageUrl(): String? {
        return data?.v3GetPinQuery?.data?.images?.url
    }
}

@Serializable
data class MainDataModel(
    val v3GetPinQuery: V3Query? = null
)

@Serializable
data class V3Query(
    val data: InnerV3Data? = null
) {

}

@Serializable
data class InnerV3Data(
    val videos: Videos? = null,
    val images: ImageQl? = null,
    val autoAltText: String? = null,
    val storyPinData: StoryPinData? = null,
)

@Serializable
data class StoryPinData(
    val pages: List<StoryPage>? = null
)

@Serializable
data class StoryPage(
    val blocks: List<StoryBlocksModel>? = null
)

@Serializable
data class StoryBlocksModel(
    val videoDataV2: VideoDataV2? = null
)

@Serializable
data class VideoDataV2(
    val videoListMobile: VideoListMobile? = null
)

@Serializable
data class VideoListMobile(
    val vHLSV3MOBILE: HLSV4? = null
)

@Serializable
data class HLSV4(
    val url: String? = null
)

@Serializable
data class ImageQl(
    val url: String? = null
)

@Serializable
data class Videos(
    val videoList: VideoListObj? = null
)

@Serializable
data class VideoListObj(
    val vHLSV4: HlsVide? = null
)

@Serializable
data class HlsVide(
    val url: String,
    val height: Int,
    val width: Int,
)