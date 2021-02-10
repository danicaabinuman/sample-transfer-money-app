package com.unionbankph.corporate.common.domain.mapper

abstract class Mapper<To, From> {

    val invalidInt = -1
    val invalidLong = (-1).toLong()
    val invalidDouble = -1.0
    val emptyString = ""

    abstract fun map(from: From): To

    abstract fun reverse(to: To): From

    fun map(list: MutableList<From>?): MutableList<To> {
        if (list != null) {
            val result = ArrayList<To>(list.size)
            list.mapTo(result) { map(it) }
            return result
        }
        return mutableListOf()
    }

    fun reverse(list: MutableList<To>?): MutableList<From> {
        if (list != null) {
            val result = ArrayList<From>(list.size)
            list.mapTo(result) { reverse(it) }
            return result
        }
        return mutableListOf()
    }
}
