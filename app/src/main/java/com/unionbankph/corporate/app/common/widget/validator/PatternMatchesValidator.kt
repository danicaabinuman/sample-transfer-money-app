package com.unionbankph.corporate.app.common.widget.validator

import android.text.TextUtils
import android.widget.EditText
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidationResult
import com.unionbankph.corporate.app.common.widget.validator.validation.Validator
import io.reactivex.Observable
import java.util.regex.Pattern

open class PatternMatchesValidator : Validator<EditText> {
    private var invalidValueMessage: String? = null
    private var pattern: Pattern? = null

    constructor() {
        this.invalidValueMessage = DEFAULT_MESSAGE
        this.pattern = android.util.Patterns.EMAIL_ADDRESS
    }

    constructor(invalidValueMessage: String) {
        this.invalidValueMessage = invalidValueMessage
        this.pattern = android.util.Patterns.EMAIL_ADDRESS
    }

    constructor(invalidValueMessage: String, pattern: Pattern) {
        this.invalidValueMessage = invalidValueMessage
        this.pattern = pattern
    }

    constructor(invalidValueMessage: String, pattern: String) {
        this.invalidValueMessage = invalidValueMessage
        this.pattern = Pattern.compile(pattern)
    }

    override fun validate(text: String, item: EditText): Observable<RxValidationResult<EditText>> {
        return Observable.just(validatePattern(item, text))
    }

    private fun validatePattern(view: EditText, value: String): RxValidationResult<EditText> {
        return if (nonEmptyAndMatchPattern(value)) {
            RxValidationResult.createSuccess(view)
        } else RxValidationResult.createImproper(view, invalidValueMessage!!)
    }

    private fun nonEmptyAndMatchPattern(value: String): Boolean {
        return !TextUtils.isEmpty(value) && pattern!!.matcher(value).matches()
    }

    companion object {

        private val DEFAULT_MESSAGE = "Invalid value"
    }
}
