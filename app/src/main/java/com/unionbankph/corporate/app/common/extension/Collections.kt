package com.unionbankph.corporate.app.common.extension

fun <T> MutableList<T>.toDistinctByKey(key: (T) -> String?): MutableList<T> {
    return distinctBy { Pair(it, key) }.toMutableList()
}

fun <T> MutableList<T>?.notNullable(): MutableList<T> {
    return this ?: mutableListOf()
}
