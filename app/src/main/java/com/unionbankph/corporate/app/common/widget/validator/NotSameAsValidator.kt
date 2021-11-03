package com.unionbankph.corporate.app.common.widget.validator

import android.widget.EditText
import android.widget.TextView
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidationResult
import com.unionbankph.corporate.app.common.widget.validator.validation.Validator
import io.reactivex.Observable

class NotSameAsValidator : Validator<EditText> {

    private var notSameMessage: String? = null
    private var anotherTextView: String? = null

    constructor() {
        notSameMessage = DEFAULT_MESSAGE
    }

    constructor(anotherTextView: String, message: String) {
        this.anotherTextView = anotherTextView
        this.notSameMessage = message
    }

    override fun validate(text: String, item: EditText): Observable<RxValidationResult<EditText>> {
        return if (anotherTextView?.lowercase() != text.lowercase()) {
            Observable.just(RxValidationResult.createSuccess(item))
        } else Observable.just(RxValidationResult.createImproper(item, notSameMessage!!))
    }

    companion object {
        val DEFAULT_MESSAGE = "ID details do not match."
    }
}