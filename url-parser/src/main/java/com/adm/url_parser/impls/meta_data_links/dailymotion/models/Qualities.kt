package com.adm.url_parser.impls.meta_data_links.dailymotion.models

import androidx.annotation.Keep
import kotlinx.serialization.Serializable


@Serializable
@Keep
data class Qualities(
    val auto: List<Auto>
)