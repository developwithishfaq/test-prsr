package com.down.adm_parser.interview.models

import androidx.annotation.Keep

@Keep
data class MainData(
    val students: List<String> = emptyList(),
    val isRequesting: Boolean = false,
    val text: String = "",
    val list: List<String> = emptyList(),
)
