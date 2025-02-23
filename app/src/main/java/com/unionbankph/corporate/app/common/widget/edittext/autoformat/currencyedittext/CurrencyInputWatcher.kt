package com.unionbankph.corporate.app.common.widget.edittext.autoformat.currencyedittext

import android.annotation.SuppressLint
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.TextWatcher
import android.widget.EditText
import com.unionbankph.corporate.R
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.text.ParseException
import java.util.*
import kotlin.math.min

class CurrencyInputWatcher(
    private val editText: EditText,
    private val currencySymbol: String,
    locale: Locale
) : TextWatcher {

    private val context = editText.context

    companion object {
        const val MAX_NO_OF_DECIMAL_PLACES = 2
        const val FRACTION_FORMAT_PATTERN_PREFIX = "#,##0."
    }

    private var hasDecimalPoint = false
    private val wholeNumberDecimalFormat =
        (NumberFormat.getNumberInstance(locale) as DecimalFormat).apply {
            applyPattern("#,##0")
        }

    private val fractionDecimalFormat = (NumberFormat.getNumberInstance(locale) as DecimalFormat)

    val decimalFormatSymbols: DecimalFormatSymbols
        get() = wholeNumberDecimalFormat.decimalFormatSymbols

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        fractionDecimalFormat.isDecimalSeparatorAlwaysShown = true
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        hasDecimalPoint = s.toString().contains(decimalFormatSymbols.decimalSeparator.toString())
    }

    @SuppressLint("SetTextI18n")
    override fun afterTextChanged(s: Editable) {
        var newInputString = s.toString()
        if (newInputString.contains(".")) {
            val splittedDecimal = newInputString.split(".")
            val decimalCount = splittedDecimal[1].length
            if (decimalCount > 2) {
                newInputString = newInputString.substring(0, newInputString.length.minus(1))
            } else {
                setEditTextMaxLength(context.resources.getInteger(R.integer.max_length_amount_without_limit))
            }
        } else if (newInputString.length < context.resources.getInteger(R.integer.max_length_amount)) {
            setEditTextMaxLength(context.resources.getInteger(R.integer.max_length_amount))
        } else if (newInputString.length >= context.resources.getInteger(R.integer.max_length_amount) &&
            newInputString.last().toString() != ".") {
            setEditTextMaxLength(context.resources.getInteger(R.integer.max_length_amount))
            newInputString = newInputString.substring(0, newInputString.length.minus(1))
        }
        if (newInputString.length < currencySymbol.length) {
            editText.setText(currencySymbol)
            editText.setSelection(currencySymbol.length)
            return
        }

        if (newInputString == currencySymbol) {
            editText.setSelection(currencySymbol.length)
            return
        }

        editText.removeTextChangedListener(this)
        val startLength = editText.text.length
        try {
            val numberWithoutGroupingSeparator =
                parseMoneyValue(
                    newInputString,
                    decimalFormatSymbols.groupingSeparator.toString(),
                    currencySymbol
                )
            val parsedNumber = fractionDecimalFormat.parse(numberWithoutGroupingSeparator)!!
            val selectionStartIndex = editText.selectionStart
            if (hasDecimalPoint) {
                fractionDecimalFormat.applyPattern(
                    FRACTION_FORMAT_PATTERN_PREFIX +
                            getFormatSequenceAfterDecimalSeparator(numberWithoutGroupingSeparator)
                )
                editText.setText("$currencySymbol${fractionDecimalFormat.format(parsedNumber)}")
            } else {
                editText.setText("$currencySymbol${wholeNumberDecimalFormat.format(parsedNumber)}")
            }
            val endLength = editText.text.length
            val selection = selectionStartIndex + (endLength - startLength)
            if (selection > 0 && selection <= editText.text.length) {
                editText.setSelection(selection)
            } else {
                editText.setSelection(editText.text.length - 1)
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        editText.addTextChangedListener(this)
    }

    /**
     * @param number the original number to format
     * @return the appropriate zero sequence for the input number. e.g 156.1 returns "0",
     *  14.98 returns "00"
     */
    private fun getFormatSequenceAfterDecimalSeparator(number: String): String {
        val noOfCharactersAfterDecimalPoint =
            number.length - number.indexOf(decimalFormatSymbols.decimalSeparator) - 1
        return "0".repeat(min(noOfCharactersAfterDecimalPoint, MAX_NO_OF_DECIMAL_PLACES))
    }

    /**
     * set max length of the amount
     */
    private fun setEditTextMaxLength(length: Int) {
        val filterArray = arrayOfNulls<InputFilter>(1)
        filterArray[0] = LengthFilter(length)
        editText.filters = filterArray
    }

}