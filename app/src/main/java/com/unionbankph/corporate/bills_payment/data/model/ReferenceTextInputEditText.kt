package com.unionbankph.corporate.bills_payment.data.model

import android.widget.EditText
import com.google.android.material.textfield.TextInputEditText
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidationResult
import io.reactivex.Observable

data class ReferenceTextInputEditText(
    var rxValidationResult: Observable<RxValidationResult<EditText>>? = null,
    var textInputEditText: TextInputEditText? = null,
    var billerField: BillerField? = null
)
