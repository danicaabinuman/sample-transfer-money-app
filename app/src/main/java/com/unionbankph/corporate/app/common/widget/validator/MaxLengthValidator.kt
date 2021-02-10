package com.unionbankph.corporate.app.common.widget.validator

import android.text.TextUtils
import android.widget.EditText
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidationResult
import com.unionbankph.corporate.app.common.widget.validator.validation.Validator
import io.reactivex.Observable

class MaxLengthValidator
@JvmOverloads
constructor(private val properLength: Int, private val lengthMessage: String = DEFAULT_MESSAGE) :
    Validator<EditText>() {

    override fun validate(text: String, item: EditText): Observable<RxValidationResult<EditText>> {
        if (TextUtils.isEmpty(text)) {
            return Observable.just(RxValidationResult.createImproper(item, lengthMessage))
        }

        return if (text.trim { it <= ' ' }.length <= properLength) {
            Observable.just(RxValidationResult.createSuccess(item))
        } else Observable.just(RxValidationResult.createImproper(item, lengthMessage))
    }

    companion object {
        private val DEFAULT_MESSAGE = "Bad length"
    }
}
