package com.adm.url_parser.impls.main_sites.fb.impls.info

import android.util.Log
import com.adm.url_parser.commons.network.ParserRequestTypes
import com.adm.url_parser.commons.network.UrlParserNetworkClient
import com.adm.url_parser.impls.main_sites.fb.models.FbGeneralInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//https:/video.fkhi6-1.fna.fbcdn.net/o1/v/t2/f2/m69/AQOWSRbyw_A1fDDCdIEvYY0Oiw9J3Xdb2y6o5EudAqV7pbbhJ25N-O_iu4aWnLAMgz18tv1UtPcue9gX40ZwgGcl.mp4?strext=1&_nc_cat=102&_nc_oc=AdjMZ9C-sLdbVweugOyPs5t-mljQEyle6xdcZ78N01N89Z0jbNC7LHVNXbxzfwdhebw&_nc_sid=9ca052&_nc_ht=video.fkhi6-1.fna.fbcdn.net&_nc_ohc=oRRUOqMNh3MQ7kNvgFUJeon&efg=eyJ2ZW5jb2RlX3RhZyI6ImRhc2hfcjJldmV2cDktcjFnZW4ydnA5X3EzMCIsInZpZGVvX2lkIjo1NzIwNzY5ODIyMjM5MDgsImNsaWVudF9uYW1lIjoidW5rbm93biIsIm9pbF91cmxnZW5fYXBwX2lkIjowLCJ1cmxnZW5fc291cmNlIjoid3d3In0%3D&ccb=9-4&_nc_zt=28&oh=00_AYA6MS6vJtbZRKRgy9poSV4ihQlwOmh7cfg1pWfjro3hOw&oe=6791D261
//https://video.xx.fbcdn.net/o1/v/t2/f2/m69/AQOWSRbyw_A1fDDCdIEvYY0Oiw9J3Xdb2y6o5EudAqV7pbbhJ25N-O_iu4aWnLAMgz18tv1UtPcue9gX40ZwgGcl.mp4?strext=1&_nc_cat=1&_nc_sid=9ca052&_nc_ht=video.xx.fbcdn.net&_nc_ohc=oRRUOqMNh3MQ7kNvgFxutVQ&efg=eyJ2ZW5jb2RlX3RhZyI6ImRhc2hfcjJldmV2cDktcjFnZW4ydnA5X3EzMCIsInZpZGVvX2lkIjo1NzIwNzY5ODIyMjM5MDgsImNsaWVudF9uYW1lIjoidW5rbm93biIsIm9pbF91cmxnZW5fYXBwX2lkIjowLCJ1cmxnZW5fc291cmNlIjoid3d3In0\u00253D&ccb=9-4&_nc_zt=28&oh=00_AYBv4TwLLDaeHYK9esHjnURH04zayEksBhyDIr78KvIosA&oe=6791D261
class GetGeneralInfoOfFbUrlImpl : GetGeneralInfoOfFbUrl {
    private val TAG = "GetGeneralInfoOfFbUrlImpl"
    override suspend fun getGeneralInfo(url: String): FbGeneralInfo {
        Log.d(TAG, "getGeneralInfo: url=$url")
        return withContext(Dispatchers.IO) {
            val response = UrlParserNetworkClient.makeNetworkRequestString(
                url = url,
                requestType = ParserRequestTypes.Get,
                headers = hashMapOf()
            )
            val result = response.data ?: ""
            Log.d(TAG, "Fb Share getOriginalLink:$response")
            Log.d(TAG, "Fb Share getOriginalLink Data:${response.data}")
            val href = result.substringAfter("<link rel=\"canonical\"")
                .substringAfter("href=\"")
                .substringBefore("\"")

            val userName = href.substringAfter(".facebook.com/").substringBefore("/")
            val videoId = href.replace(userName, "").getVideoId()
            Log.d(TAG, "Fb Share Link:${href}")
            Log.d(TAG, "Fb Share Video Id:${videoId}")
            Log.d(TAG, "Fb Share Username:${userName}")
            FbGeneralInfo(
                videoId = videoId,
                userName = userName
            )
        }
    }

    fun String.getVideoId(): String {
        val regex = Regex("""/(\d+)/""")
        val matchResult = regex.find(this)
        val number = matchResult?.groupValues?.get(1)
        return number ?: ""
    }
}