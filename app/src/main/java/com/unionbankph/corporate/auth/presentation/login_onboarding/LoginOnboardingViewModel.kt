package com.unionbankph.corporate.auth.presentation.login_onboarding

import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class LoginOnboardingViewModel @Inject constructor(private val schedulerProvider: SchedulerProvider,
                                                   private val settingsGateway: SettingsGateway
) : BaseViewModel() {

    fun onClickedStartLaunch() {
        settingsGateway.setUdid()
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _uiState.value = Event(UiState.Loading) }
            .doFinally { _uiState.value = Event(UiState.Complete) }
            .subscribe(
                {
                    _uiState.value = Event(UiState.Success)
                }, {
                    Timber.e(it, "onClickedNext failed")
                    _uiState.value = Event(UiState.Error(it))
                }
            )
            .addTo(disposables)
    }
}