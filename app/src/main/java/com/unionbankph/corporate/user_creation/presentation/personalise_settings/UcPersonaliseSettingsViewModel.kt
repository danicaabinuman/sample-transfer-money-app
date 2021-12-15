package com.unionbankph.corporate.user_creation.presentation.personalise_settings

import android.app.Notification
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.getDisposableSingleObserver
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.auth.data.AuthGateway
import com.unionbankph.corporate.auth.data.model.Auth
import com.unionbankph.corporate.auth.data.model.DemoOrgDetails
import com.unionbankph.corporate.auth.data.model.UserDetails
import com.unionbankph.corporate.auth.presentation.otp.ShowOTPDismissLoading
import com.unionbankph.corporate.auth.presentation.otp.ShowOTPError
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.constant.PromptTypeEnum
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import com.unionbankph.corporate.trial_account.presentation.TrialAccountViewModel
import com.unionbankph.corporate.user_creation.data.form.UcNominatePasswordForm
import com.unionbankph.corporate.user_creation.data.param.PersonalizeSettings
import com.unionbankph.corporate.user_creation.domain.SetPersonalSettingsUseCase
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class UcPersonaliseSettingsViewModel @Inject constructor(
    private val settingsUseCase: SetPersonalSettingsUseCase,
    private val authGateway: AuthGateway,
    private val settingsGateway: SettingsGateway,
    private val schedulerProvider: SchedulerProvider
) :
    BaseViewModel() {

    private val _getLocalSettings = MutableLiveData<Event<String>>()

    val getLocalSettings: LiveData<Event<String>> get() = _getLocalSettings

    private val _navigateToLocalSettings = MutableLiveData<Event<SettingsState>>()

    val navigateToLocalSettings: LiveData<Event<SettingsState>> get() = _navigateToLocalSettings

    private val _getDemoDetails = MutableLiveData<Event<SettingsState>>()

    val getDemoDetails: LiveData<Event<SettingsState>> get() = _getDemoDetails

    fun saveSettings(isChecked: Boolean, isCheckedTOTP: Boolean, promptType: PromptTypeEnum) {
        val param = PersonalizeSettings().apply {
            notification = isChecked
            totp = isCheckedTOTP
            promptTypeEnum = promptType
        }

        settingsUseCase.execute(
            getDisposableSingleObserver(
                {
                    it.let {
                        _getDemoDetails.value = Event(SetSettingsSuccess)
                    }
                }, {
                    _uiState.value = Event(UiState.Error(it))
                }
            ),
            doOnSubscribeEvent = {
                _uiState.value = Event(UiState.Loading)
            },
            doFinallyEvent = {
                _uiState.value = Event(UiState.Complete)
            },
            params = param
        ).addTo(disposables)
    }

    fun getDemoOrgDetails(id: String) {
        authGateway.userCreationGetDemoDetails(id)
            .flatMap { cacheDemoOrgDetails(it).toSingle { it } }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _uiState.value = Event(UiState.Loading) }
            .doFinally { _uiState.value = Event(UiState.Complete) }
            .subscribe (
                {
                    _navigateToLocalSettings.value = Event(SetSettingsSuccess)
                },
                {
                    Timber.e("uc getDemoOrgDetails err: %s", it.message)
                    _uiState.value = Event(UiState.Error(it))
                }
            ).addTo(disposables)
    }

    private fun cacheDemoOrgDetails(auth: Auth?): Completable {
        val demoOrgDetails = DemoOrgDetails(
            auth?.trialDaysRemaining,
            auth?.trialMode,

        )
        return Observable.just(demoOrgDetails)
            .flatMapCompletable {
                authGateway.saveDemoDetail(demoOrgDetails)
            }
    }

    fun getOrgID() {
        settingsGateway.getOrgID()
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    _getLocalSettings.value = Event(it)
                }, {
                    Timber.e("uc getOrgID err: %s", it.message)
                    _uiState.value = Event(UiState.Error(it))
                })
            .addTo(disposables)
    }

    sealed class SettingsState
    object SetSettingsSuccess : SettingsState()

}