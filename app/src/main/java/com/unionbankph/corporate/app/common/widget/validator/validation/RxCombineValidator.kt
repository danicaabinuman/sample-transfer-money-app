package com.unionbankph.corporate.app.common.widget.validator.validation

import android.widget.EditText
import io.reactivex.Observable

class RxCombineValidator(private vararg val observables: Observable<RxValidationResult<EditText>>) {

    /**
     * Combiner for your validations observable, which allows change UI if all observables emmit <b>true</b>
     * (use-full for change button stateFrequent)
     */
    fun asObservable(): Observable<Boolean> {
        return Observable.combineLatest(observables) { arrays ->
            arrays.forEach {
                if (!(it as RxValidationResult<*>).isProper) {
                    return@combineLatest false
                }
            }
            return@combineLatest true
        }
    }
}
