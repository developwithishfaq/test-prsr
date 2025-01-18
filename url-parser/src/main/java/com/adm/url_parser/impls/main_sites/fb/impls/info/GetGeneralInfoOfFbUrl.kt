package com.adm.url_parser.impls.main_sites.fb.impls.info

import com.adm.url_parser.impls.main_sites.fb.models.FbGeneralInfo

interface GetGeneralInfoOfFbUrl {
    suspend fun getGeneralInfo(url: String): FbGeneralInfo
}