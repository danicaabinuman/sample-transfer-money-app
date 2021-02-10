package com.unionbankph.corporate.app.common.widget.validator

import android.widget.EditText
import android.widget.TextView
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidationResult
import com.unionbankph.corporate.app.common.widget.validator.validation.Validator
import io.reactivex.Observable

class SameAsValidator : Validator<EditText> {
    private var sameAsMessage: String? = null
    private var anotherTextView: TextView? = null

    constructor() {
        sameAsMessage = DEFAULT_MESSAGE
    }

    constructor(anotherTextView: TextView, message: String) {
        this.anotherTextView = anotherTextView
        this.sameAsMessage = message
    }

    override fun validate(text: String, item: EditText): Observable<RxValidationResult<EditText>> {
        return if (anotherTextView?.text.toString() == text) {
            Observable.just(RxValidationResult.createSuccess(item))
        } else Observable.just(RxValidationResult.createImproper(item, sameAsMessage!!))
    }

    companion object {
        val DEFAULT_MESSAGE = "Must be the same"
    }
}
