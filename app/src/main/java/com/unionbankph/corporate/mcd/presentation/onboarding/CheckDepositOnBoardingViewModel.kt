package com.unionbankph.corporate.mcd.presentation.onboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import javax.inject.Inject

class CheckDepositOnBoardingViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val settingsGateway: SettingsGateway
) : BaseViewModel() {

    private val _checkDepositState = MutableLiveData<CheckDepositOnBoardingState>()

    val state: LiveData<CheckDepositOnBoardingState> get() = _checkDepositState

    val isInitialLoad = BehaviorSubject.create<Boolean>()

    fun setFirstCheckDeposit(isFirstLaunch: Boolean) {
        settingsGateway.setFirstCheckDeposit(isFirstLaunch)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError {
                Timber.e(it, "isFirstCheckDeposit failed")
                _checkDepositState.value = ShowCheckDepositOnBoardingError(it)
            }
            .subscribe()
            .addTo(disposables)
    }

    fun isFirstCheckDeposit() {
        settingsGateway.isFirstCheckDeposit()
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    isInitialLoad.onNext(it)
                }, {
                    Timber.e(it, "isFirstCheckDeposit failed")
                    _checkDepositState.value = ShowCheckDepositOnBoardingError(it)
                }
            )
            .addTo(disposables)
    }
}

sealed class CheckDepositOnBoardingState

data class ShowCheckDepositOnBoardingError(val throwable: Throwable) : CheckDepositOnBoardingState()
