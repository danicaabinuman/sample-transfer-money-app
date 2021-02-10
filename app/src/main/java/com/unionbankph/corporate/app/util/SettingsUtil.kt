package com.unionbankph.corporate.app.util

import android.app.Activity
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.util.DisplayMetrics
import com.unionbankph.corporate.R
import com.unionbankph.corporate.common.data.source.local.cache.CacheManager
import com.unionbankph.corporate.common.data.source.local.sharedpref.SharedPreferenceUtil
import javax.inject.Inject

/**
 * Created by herald25santos on 2019-07-09
 */
class
SettingsUtil @Inject constructor(
    private val context: Context,
    private val cacheManager: CacheManager,
    private val sharedPreferenceUtil: SharedPreferenceUtil
) {

    fun getUdId(): String =
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

    fun getDeviceName(): String =
        Settings.Global.getString(context.contentResolver, "device_name") ?: getDeviceNameIfNull()

    fun isTimeAutomatic(): Boolean {
        return Settings.Global.getInt(
            context.contentResolver, Settings.Global.AUTO_TIME, 0
        ) == 1
    }

    private fun isTablet(activity: Activity): Boolean {
        val metrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(metrics)
        val yInches = metrics.heightPixels / metrics.ydpi
        val xInches = metrics.widthPixels / metrics.xdpi
        val diagonalInches = Math.sqrt((xInches * xInches + yInches * yInches).toDouble())
        return diagonalInches >= 6.5
    }

    private fun getDeviceType(activity: Activity): String {
        return if (isTablet(activity)) {
            context.getString(R.string.device_tablet)
        } else {
            context.getString(R.string.device_android)
        }
    }

    private fun getUserAgent(): String {
        return System.getProperty("http.agent") ?: ""
    }

    fun getDefaultUserAgent(): String {
        return "${getUserAgent()} [${getDeviceName()}]"
    }

    private fun getDeviceNameIfNull(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.startsWith(manufacturer)) {
            capitalize(model)
        } else capitalize(manufacturer) + " " + model
    }

    private fun capitalize(str: String): String {
        if (TextUtils.isEmpty(str)) {
            return str
        }
        val arr = str.toCharArray()
        var capNext = true

        val phrase = StringBuilder()
        for (c in arr) {
            if (capNext && Character.isLetter(c)) {
                phrase.append(Character.toUpperCase(c))
                capNext = false
                continue
            } else if (Character.isWhitespace(c)) {
                capNext = true
            }
            phrase.append(c)
        }

        return phrase.toString()
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferenceUtil.isLoggedIn().get() &&
                cacheManager.get(CacheManager.ACCESS_TOKEN) != ""
    }

}
