package com.unionbankph.corporate.mcd.presentation.preview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.mcd.data.gateway.CheckDepositGateway
import com.unionbankph.corporate.mcd.data.model.CheckDepositUpload
import com.unionbankph.corporate.mcd.presentation.constant.CheckDepositTypeEnum
import com.unionbankph.corporate.mcd.presentation.preview.CheckDepositPreviewActivity.Companion.BACK_CHECK
import com.unionbankph.corporate.common.data.source.local.cache.CacheManager
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class CheckDepositPreviewViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val checkDepositGateway: CheckDepositGateway,
    private val cacheManager: CacheManager
) : BaseViewModel() {

    private val _checkDepositUpload = MutableLiveData<CheckDepositUpload>()

    val checkDepositUpload: LiveData<CheckDepositUpload> get() = _checkDepositUpload

    val id = BehaviorSubject.create<String>()

    fun setCheckDepositUpload(checkDepositUploadString: String) {
        Single.fromCallable {
                JsonHelper.fromJson<CheckDepositUpload>(checkDepositUploadString)
            }.subscribeOn(schedulerProvider.newThread()).observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    id.onNext(it.id.notNullable())
                }, {
                    Timber.e(it, "setCheckDepositUpload failed")
                    _uiState.value = Event(UiState.Error(it))
                }
            ).addTo(disposables)
    }

    fun uploadCheckDeposit(file: File, fileKey: String) {
        val id = if (fileKey == BACK_CHECK) {
            id.value
        } else {
            ""
        }
        checkDepositGateway.checkDepositUploadFile(file, fileKey, id)
            .doOnSuccess {
                saveCroppedPath(file.path, fileKey)
                if (fileKey == BACK_CHECK) {
                    deleteRawFiles()
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                _uiState.value = Event(UiState.Loading)
            }
            .doFinally {
                _uiState.value = Event(UiState.Complete)
            }
            .subscribe(
                {
                    _checkDepositUpload.value = it
                }, {
                    Timber.e(it, "checkDeposit failed")
                    _uiState.value = Event(UiState.Error(it))
                }
            )
            .addTo(disposables)
    }

    private fun deleteRawFiles() {
        File(cacheManager.get(CheckDepositTypeEnum.FRONT_OF_CHECK.name)).delete()
        File(cacheManager.get(CheckDepositTypeEnum.BACK_OF_CHECK.name)).delete()
        cacheManager.get(cacheManager.get(CheckDepositTypeEnum.FRONT_OF_CHECK.name))
        cacheManager.get(cacheManager.get(CheckDepositTypeEnum.BACK_OF_CHECK.name))
    }

    private fun saveCroppedPath(path: String, fileKey: String) {
        cacheManager.put(
            if (fileKey == BACK_CHECK)
                CheckDepositTypeEnum.CROPPED_BACK_OF_CHECK.name.toLowerCase()
            else
                CheckDepositTypeEnum.CROPPED_FRONT_OF_CHECK.name.toLowerCase(),
            path
        )
    }
}
