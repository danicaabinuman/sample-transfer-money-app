package com.unionbankph.corporate.trial_account.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class TrialAccountViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val settingsGateway: SettingsGateway
) : BaseViewModel() {

    private val _trialAccountState = MutableLiveData<TrialAccountState>()

    val state: LiveData<TrialAccountState> get() = _trialAccountState

    fun getTrialDaysRemaining() {
        settingsGateway.getTrialModeDaysRemaining()
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    _trialAccountState.value =
                        ShowDaysRemaining(
                            it
                        )
                }, {
                    Timber.d(it, "getTrialDaysRemaining Failed")
                    _trialAccountState.value =
                        ShowTrialAccountError(
                            it
                        )
                })
            .addTo(disposables)
    }

    sealed class TrialAccountState

    object ShowTrialAccountStateLoading : TrialAccountState()

    object ShowTrialAccountStateDismissLoading : TrialAccountState()

    data class ShowDaysRemaining(val daysRemaining: String) : TrialAccountState()

    data class ShowTrialAccountError(val throwable: Throwable) : TrialAccountState()

}
