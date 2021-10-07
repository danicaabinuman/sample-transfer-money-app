package com.unionbankph.corporate.settings.presentation.general

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.settings.data.form.OTPTypeForm
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import com.unionbankph.corporate.settings.data.model.OTPTypeDto
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.zipWith
import timber.log.Timber
import javax.inject.Inject

class GeneralSettingsViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val settingsGateway: SettingsGateway
) : BaseViewModel() {

    private val _generalSettingsState = MutableLiveData<GeneralSettingsState>()

    val state: LiveData<GeneralSettingsState> get() = _generalSettingsState

    fun clearTokenFingerPrint() {
        settingsGateway.clearFingerprintCredential()
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    _generalSettingsState.value =
                        ShowGeneralSettingsClearToken
                }, {
                    Timber.d(it, "setFingerPrint Failed")
                    _generalSettingsState.value =
                        ShowGeneralSettingsError(
                            it
                        )
                })
            .addTo(disposables)
    }

    fun getTokenFingerPrint() {
        settingsGateway.getFingerprintToken()
            .zipWith(settingsGateway.hasTOTP())
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    _generalSettingsState.value =
                        ShowGeneralSettingsGetToken(
                            it.first,
                            it.second
                        )
                }, {
                    Timber.d(it, "setFingerPrint Failed")
                    _generalSettingsState.value =
                        ShowGeneralSettingsError(
                            it
                        )
                })
            .addTo(disposables)
    }

    fun getOTPTypes() {
        settingsGateway.getOTPTypes()
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _generalSettingsState.value =
                ShowGeneralSettingsLoading
            }
            .doFinally { _generalSettingsState.value =
                ShowGeneralSettingsDismissLoading
            }
            .subscribe(
                {
                    _generalSettingsState.value =
                        ShowGeneralSettingsGetOTPType(
                            it
                        )
                }, {
                    Timber.e(it, "getOTPTypes failed")
                    _generalSettingsState.value =
                        ShowGeneralSettingsError(
                            it
                        )
                })
            .addTo(disposables)
    }

    fun setOTPType(oTPTypeForm: OTPTypeForm) {
        settingsGateway.setOTPType(oTPTypeForm)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _generalSettingsState.value =
                ShowGeneralSettingsProgressLoading
            }
            .doFinally { _generalSettingsState.value =
                ShowGeneralSettingsProgressDismissLoading
            }
            .subscribe(
                {
                    _generalSettingsState.value =
                        ShowGeneralSettingsGetOTPType(
                            it
                        )
                }, {
                    Timber.e(it, "setOTPType failed")
                    _generalSettingsState.value =
                        ShowGeneralSettingsError(
                            it
                        )
                })
            .addTo(disposables)
    }

    fun hasTOTP() {
        settingsGateway.hasTOTP()
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    _generalSettingsState.value =
                        ShowLoginHasTOTPAccess(
                            it
                        )
                }, {
                    Timber.e(it, "hasTOTP Failed")
                    _generalSettingsState.value =
                        ShowGeneralSettingsError(
                            it
                        )
                })
            .addTo(disposables)
    }
}

sealed class GeneralSettingsState

object ShowGeneralSettingsLoading : GeneralSettingsState()

object ShowGeneralSettingsDismissLoading : GeneralSettingsState()

object ShowGeneralSettingsProgressLoading : GeneralSettingsState()

object ShowGeneralSettingsProgressDismissLoading : GeneralSettingsState()

object ShowGeneralSettingsResetTutorial : GeneralSettingsState()

object ShowGeneralSettingsResetDemo : GeneralSettingsState()

object ShowGeneralSettingsClearToken : GeneralSettingsState()

data class ShowGeneralSettingsGetOTPType(val otpTypeDto: OTPTypeDto) : GeneralSettingsState()

data class ShowGeneralSettingsGetToken(val token: String, val isTrustedDevice: Boolean) : GeneralSettingsState()

data class ShowLoginHasTOTPAccess(val isTrustedDevice: Boolean) : GeneralSettingsState()

data class ShowGeneralSettingsError(val throwable: Throwable) : GeneralSettingsState()
