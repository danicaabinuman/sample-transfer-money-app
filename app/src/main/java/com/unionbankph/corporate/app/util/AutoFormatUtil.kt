package com.unionbankph.corporate.app.util

import com.unionbankph.corporate.account.data.model.Headers
import timber.log.Timber
import java.text.DecimalFormat

class AutoFormatUtil {

    fun getCharOccurance(input: String, c: Char): Int {
        var occurance = 0
        val chars = input.toCharArray()
        for (thisChar in chars) {
            if (thisChar == c) {
                occurance++
            }
        }
        return occurance
    }

    fun extractDigits(input: String): String {
        return input.replace("\\D+".toRegex(), "")
    }

    fun formatToStringWithoutDecimal(value: Double): String {
        val formatter = DecimalFormat(FORMAT_NO_DECIMAL)
        return formatter.format(value)
    }

    fun formatWithDecimal(price: Double, decimalFormat: String): String {
        val formatter = DecimalFormat(decimalFormat)
        return formatter.format(price)
    }

    fun formatWithDecimal(price: Double): String {
        val formatter = DecimalFormat(FORMAT_WITH_DECIMAL)
        return formatter.format(price)
    }

    fun formatWithTwoDecimalPlaces(amount: String?): String {
        if (amount == null || amount == "N/A") return "-"
        try {
            return formatWithFourDecimalPlaces(amount.toDouble(), FORMAT_WITH_TWO_DECIMAL)
        } catch (e: NullPointerException) {
            Timber.e(e, "formatWithTwoDecimalPlaces")
        }
        return "-"
    }

    fun formatWithTwoDecimalPlaces(amount: String?, currency: String?): String {
        if (amount == null || amount == "N/A") return "-"
        try {
            return "${currency ?: "PHP"} " + formatWithFourDecimalPlaces(
                amount.toDouble(),
                "#,##0.00"
            )
        } catch (e: NullPointerException) {
            Timber.e(e, "formatWithTwoDecimalPlaces")
        }
        return "-"
    }

    fun getBalance(amountType: String?, headers: MutableList<Headers>): Headers? {
        return headers.find { amountType == it.name }
    }

    private fun formatWithFourDecimalPlaces(price: Double?, format: String?): String {
        val formatter = DecimalFormat(format)
        return formatter.format(price)
    }

    companion object {

        const val FORMAT_NO_DECIMAL = "#,###"
        const val FORMAT_WITH_DECIMAL = "#,###.##"

        const val FORMAT_WITH_FOUR_DECIMAL = "#,##0.0000"
        const val FORMAT_WITH_TWO_DECIMAL = "#,##0.00"
        const val FORMAT_WITH_ONE_DECIMAL = "#,##0.0"
    }
}
