package com.unionbankph.corporate.mcd.presentation.camera

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.data.source.local.cache.CacheManager
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.mcd.presentation.constant.CheckDepositScreenEnum
import com.unionbankph.corporate.mcd.presentation.constant.CheckDepositTypeEnum
import id.zelory.compressor.Compressor
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class CheckDepositCameraViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val cacheManager: CacheManager,
    private val context: Context
) : BaseViewModel() {

    val screen = BehaviorSubject.createDefault(CheckDepositScreenEnum.FRONT_OF_CHECK.name)

    val checkDepositType = BehaviorSubject.createDefault(CheckDepositTypeEnum.FRONT_OF_CHECK.name)

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
                    saveCroppedPath(it.path)
                    _navigateNextStep.value = Event(it)
                }, {
                    Timber.e(it, "onPictureTaken")
                }
            ).addTo(disposables)
    }

    private fun saveCroppedPath(path: String) {
        cacheManager.put(
            if (checkDepositType.value == CheckDepositTypeEnum.FRONT_OF_CHECK.name)
                CheckDepositTypeEnum.FRONT_OF_CHECK.name.toLowerCase()
            else
                CheckDepositTypeEnum.BACK_OF_CHECK.name.toLowerCase(),
            path
        )
    }
}
