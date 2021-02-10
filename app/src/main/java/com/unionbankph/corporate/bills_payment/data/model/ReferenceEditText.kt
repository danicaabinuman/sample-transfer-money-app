package com.unionbankph.corporate.bills_payment.data.model

import android.widget.EditText
import com.google.android.material.textfield.TextInputEditText
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidationResult
import io.reactivex.Observable

data class ReferenceEditText(
    var rxValidationResult: Observable<RxValidationResult<EditText>>? = null,
    var billerField: BillerField? = null,
    var textInputEditText: TextInputEditText? = null,
    var editText: EditText? = null
)
