package com.unionbankph.corporate.app.common.extension

import android.content.Context
import com.google.gson.Gson
import com.unionbankph.corporate.loan.City
import com.unionbankph.corporate.loan.Province
import com.unionbankph.corporate.loan.Region

const val FILE_NAME_PROVINCE = "province.json"
const val FILE_NAME_CITY = "city.json"
const val FILE_NAME_REGION = "region.json"

fun Context.getProvinceJson(): Province {
    return Gson().fromJson(this.readFromJson(FILE_NAME_PROVINCE) ?: "")
}

fun Context.getCityJson(): City {
    return Gson().fromJson(this.readFromJson(FILE_NAME_CITY) ?: "")
}

fun Context.getRegionJson(): Region {
    return Gson().fromJson(this.readFromJson(FILE_NAME_REGION) ?: "")
}