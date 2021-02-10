package com.unionbankph.corporate.app.common.widget.edittext.autoformat.currencyedittext

import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.text.InputType
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import com.google.android.material.textfield.TextInputEditText
import com.unionbankph.corporate.R
import java.math.BigDecimal
import java.util.*

class CurrencyEditText(context: Context, attrs: AttributeSet?) : TextInputEditText(context, attrs) {
    private lateinit var currencySymbolPrefix: String
    private var textWatcher: CurrencyInputWatcher
    private var locale: Locale = Locale.getDefault()
    private var toolTipListener: ToolTipListener? = null
    private var hasToolTip: Boolean = false

    init {
        var useCurrencySymbolAsHint = false
        inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        var localeTag: String?
        val prefix: String
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CurrencyEditText,
            0, 0
        ).run {
            try {
                prefix = getString(R.styleable.CurrencyEditText_currencySymbol).orEmpty()
                localeTag = getString(R.styleable.CurrencyEditText_localeTag)
                useCurrencySymbolAsHint =
                    getBoolean(R.styleable.CurrencyEditText_useCurrencySymbolAsHint, false)
            } finally {
                recycle()
            }
        }
        currencySymbolPrefix = if (prefix.isBlank()) "" else "$prefix "
        if (useCurrencySymbolAsHint) hint = "$currencySymbolPrefix 0.00"
        if (isLollipopAndAbove() && !localeTag.isNullOrBlank()) locale =
            getLocaleFromTag(localeTag!!)
        textWatcher = CurrencyInputWatcher(this, currencySymbolPrefix, locale)
        addTextChangedListener(textWatcher)
        setOnTouchListener { v: View?, event: MotionEvent ->
            if (hasToolTip) {
                if (event.action == MotionEvent.ACTION_UP) {
                    if (event.rawX >= this.right -
                        this.compoundDrawables[2].bounds.width()
                    ) {
                        toolTipListener?.onClickedIcon()
                    }
                }
                return@setOnTouchListener false
            }
            return@setOnTouchListener false
        }
    }

    fun setLocale(locale: Locale) {
        this.locale = locale
        invalidateTextWatcher()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun setLocale(localeTag: String) {
        locale = getLocaleFromTag(localeTag)
        invalidateTextWatcher()
    }

    fun setCurrencySymbol(currencySymbol: String?, useCurrencySymbolAsHint: Boolean = false) {
        currencySymbolPrefix = "$currencySymbol "
        if (useCurrencySymbolAsHint) hint = "$currencySymbolPrefix 0.00"
        invalidateTextWatcher()
        if (text.toString().isNotEmpty()) {
            val splittedAmount = text.toString().split(" ")
            if (splittedAmount.size == 2 && splittedAmount[1] != "0.00") {
                setText(("$currencySymbolPrefix ${splittedAmount[1]}"))
            }
        }
    }

    private fun invalidateTextWatcher() {
        removeTextChangedListener(textWatcher)
        textWatcher = CurrencyInputWatcher(this, currencySymbolPrefix, locale)
        addTextChangedListener(textWatcher)
    }

    fun getNumericValue(): Double {
        return parseMoneyValueWithLocale(
            locale,
            text.toString(),
            textWatcher.decimalFormatSymbols.groupingSeparator.toString(),
            currencySymbolPrefix
        ).toDouble()
    }

    fun getExactValue(): String {
        val splittedEditText = text.toString().split(" ")
        if (splittedEditText.size > 1) {
            return splittedEditText[1].replace(",", "")
        }
        return ""
    }

    fun getNumericValueBigDecimal(): BigDecimal {
        return BigDecimal(
            parseMoneyValueWithLocale(
                locale,
                text.toString(),
                textWatcher.decimalFormatSymbols.groupingSeparator.toString(),
                currencySymbolPrefix
            ).toString()
        )
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(text, type)
        getText()?.length?.let { setSelection(it) }
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        if (focused) {
            removeTextChangedListener(textWatcher)
            addTextChangedListener(textWatcher)
            if (text.toString().isEmpty()) setText(currencySymbolPrefix)
        } else {
            removeTextChangedListener(textWatcher)
            if (text.toString() == currencySymbolPrefix) setText("")
        }
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        if (::currencySymbolPrefix.isInitialized.not()) return
        val symbolLength = currencySymbolPrefix.length
        if (selEnd < symbolLength && text.toString().length >= symbolLength) {
            setSelection(symbolLength)
        } else {
            super.onSelectionChanged(selStart, selEnd)
        }
    }

    fun clearText() {
        removeTextChangedListener(textWatcher)
        text?.clear()
        hint = "$currencySymbolPrefix 0.00"
        this.clearFocus()
    }

    fun setToolTipListener(toolTipListener: ToolTipListener?) {
        this.toolTipListener = toolTipListener
        this.hasToolTip = true
    }

    interface ToolTipListener {
        fun onClickedIcon()
    }
}