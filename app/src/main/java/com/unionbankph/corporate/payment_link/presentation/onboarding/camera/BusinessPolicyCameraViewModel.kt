package com.unionbankph.corporate.payment_link.presentation.onboarding.camera

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import id.zelory.compressor.Compressor
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class BusinessPolicyCameraViewModel @Inject constructor(
    private val context: Context
) : BaseViewModel() {

    private val _navigateNextStep = MutableLiveData<Event<File>>()

    val navigateNextStep: LiveData<Event<File>> get() = _navigateNextStep

    val isFlashOn = BehaviorSubject.createDefault(false)

    fun onPictureTaken(file: File?) {
        Compressor(context)
            .setMaxWidth(3112)
            .setMaxHeight(3316)
            .setQuality(100)
            .compressToFileAsFlowable(file)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { _uiState.value = Event(UiState.Loading) }
            .doFinally { _uiState.value = Event(UiState.Complete) }
            .subscribe(
                {
                    _navigateNextStep.value = Event(it)
                }, {
                    Timber.e(it, "onPictureTaken")
                }
            ).addTo(disposables)
    }
}