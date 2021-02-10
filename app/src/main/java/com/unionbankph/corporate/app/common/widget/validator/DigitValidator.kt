package com.unionbankph.corporate.app.common.widget.validator

import android.text.TextUtils
import android.widget.EditText
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidationResult
import com.unionbankph.corporate.app.common.widget.validator.validation.Validator
import io.reactivex.Observable

class DigitValidator : Validator<EditText> {
    private var digitOnlyMessage: String? = null

    constructor() {
        digitOnlyMessage = DEFAULT_MESSAGE
    }

    constructor(digitOnlyMessage: String) {
        this.digitOnlyMessage = digitOnlyMessage
    }

    override fun validate(text: String, item: EditText): Observable<RxValidationResult<EditText>> {
        return if (TextUtils.isDigitsOnly(text)) {
            Observable.just(RxValidationResult.createSuccess(item))
        } else Observable.just(RxValidationResult.createImproper(item, digitOnlyMessage!!))
    }

    companion object {
        private val DEFAULT_MESSAGE = "Digits only"
    }
}
