package com.down.adm_parser.interview.models

data class MainData(
    val students: List<String> = emptyList(),
    val isRequesting: Boolean = false,
    val text: String = "",
    val list: List<String> = emptyList(),
)
