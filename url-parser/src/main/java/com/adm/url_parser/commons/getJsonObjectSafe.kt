package com.adm.url_parser.commons

import org.json.JSONArray
import org.json.JSONObject

fun JSONObject.getJsonObjectSafe(text: String): JSONObject? {
    return try {
        getJSONObject(text)
    } catch (_: Exception) {
        null
    }
}

fun JSONArray.getAtSafe(index: Int): JSONObject? {
    return try {
        this.getJSONObject(index)
    } catch (_: Exception) {
        null
    }
}

fun JSONObject.getJsonArraySafe(text: String): JSONArray? {
    return try {
        getJSONArray(text)
    } catch (_: Exception) {
        null
    }
}