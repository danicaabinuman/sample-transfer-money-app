package com.unionbankph.corporate.app.common.widget.edittext

import android.text.InputFilter
import android.text.Spanned
import timber.log.Timber

class PercentageInputFilter constructor(
    private val min: Float,
    private val max: Float
) : InputFilter {

    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        try {
            // Get input
            val stringToMatch: String = dest.toString() + source.toString()
            val input = stringToMatch.toFloat()

            // Check if the input is in range.
            if (isInRange(min, max, input)) {
                // return null to accept the original replacement in case the format matches and text is in range.
                return null
            }
        } catch (nfe: NumberFormatException) {
        }
        return ""
    }

    private fun isInRange(
        min: Float,
        max: Float,
        input: Float
    ): Boolean {
        val isRange = input in min..max
        Timber.d("percentage - min: $min")
        Timber.d("percentage - max: $max")
        Timber.d("percentage - input: $input")
        Timber.d("percentage - isRange: $isRange")
        return isRange
    }
}
