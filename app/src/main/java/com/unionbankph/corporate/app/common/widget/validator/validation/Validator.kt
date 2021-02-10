package com.unionbankph.corporate.app.common.widget.validator.validation

import android.widget.TextView
import io.reactivex.Observable

abstract class Validator<T : TextView> {
    abstract fun validate(text: String, item: T): Observable<RxValidationResult<T>>
}
