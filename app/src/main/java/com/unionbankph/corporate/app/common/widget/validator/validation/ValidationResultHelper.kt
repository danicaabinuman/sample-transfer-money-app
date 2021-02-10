package com.unionbankph.corporate.app.common.widget.validator.validation

import android.widget.EditText

object ValidationResultHelper {
    fun getFirstBadResultOrSuccess(
        results: List<RxValidationResult<EditText>>
    ): RxValidationResult<EditText> {
        var firstBadResult: RxValidationResult<EditText>? = null
        for (result in results) {
            if (!result.isProper) {
                firstBadResult = result
                break
            }
        }
        return firstBadResult ?: results[0]
    }
}
