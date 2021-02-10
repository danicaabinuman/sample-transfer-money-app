package com.unionbankph.corporate.app.common.widget.validator.validation

interface TypeOfChangeBuilder {

    fun onFocusChanged(): RxValidator
    fun onValueChanged(): RxValidator
    fun onSubscribe(): RxValidator
}
