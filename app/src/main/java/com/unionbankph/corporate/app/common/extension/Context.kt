package com.unionbankph.corporate.app.common.extension

import android.content.Context
import java.io.IOException

fun Context.readFromJson(fileName: String): String? {
    var jsonString: String? = null
    try {
        jsonString = assets.open(fileName).bufferedReader().use { it.readText() }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return jsonString
}