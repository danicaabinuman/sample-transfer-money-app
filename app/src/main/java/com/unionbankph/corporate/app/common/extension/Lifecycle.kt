package com.unionbankph.corporate.app.common.extension

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

fun <T : Any, L : LiveData<T>> LifecycleOwner.observe(liveData: L, body: (T?) -> Unit) {
    liveData.removeObservers(this)
    liveData.observe(this, Observer(body))
}

fun <T> MutableLiveData<T>.updateFields(actions: (MutableLiveData<T>) -> Unit) {
    actions(this)
    this.value = this.value
}