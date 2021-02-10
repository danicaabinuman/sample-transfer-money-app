package com.unionbankph.corporate.app.common.widget.validator.validation

import android.widget.TextView

class RxValidationResult<T : TextView> private constructor(
    var item: T,
    val isProper: Boolean,
    val message: String
) {

    val validatedText: String
        get() = item.text.toString()

    override fun toString(): String {
        return "RxValidationResult{" +
                "itemValue=" + item.text.toString() +
                ", isProper=" + isProper +
                ", message='" + message + '\''.toString() +
                '}'.toString()
    }

    companion object {

        fun <T : TextView> createImproper(item: T, message: String): RxValidationResult<T> {
            return RxValidationResult(item, false, message)
        }

        fun <T : TextView> createSuccess(item: T): RxValidationResult<T> {
            return RxValidationResult(item, true, "")
        }
    }
}
