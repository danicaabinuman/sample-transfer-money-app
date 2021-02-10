package com.unionbankph.corporate.app.common.widget.validator

import android.text.TextUtils
import android.widget.EditText
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidationResult
import com.unionbankph.corporate.app.common.widget.validator.validation.Validator
import io.reactivex.Observable

class NonEmptyValidator : Validator<EditText> {
    private var cannotBeEmptyMessage: String? = null

    constructor() {
        cannotBeEmptyMessage = DEFAULT_MESSAGE
    }

    constructor(cannotBeEmptyMessage: String) {
        this.cannotBeEmptyMessage = cannotBeEmptyMessage
    }

    override fun validate(text: String, item: EditText): Observable<RxValidationResult<EditText>> {
        return if (isEmpty(text)) {
            Observable.just(RxValidationResult.createImproper(item, cannotBeEmptyMessage!!))
        } else Observable.just(RxValidationResult.createSuccess(item))
    }

    private fun isEmpty(value: String): Boolean {
        return TextUtils.isEmpty(value) || value.trim { it <= ' ' }.isEmpty()
    }

    companion object {
        private val DEFAULT_MESSAGE = "Cannot be empty"
    }
}
