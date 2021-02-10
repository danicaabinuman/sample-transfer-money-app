package com.unionbankph.corporate.settings.presentation.fingerprint

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class BiometricViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val settingsGateway: SettingsGateway
) : BaseViewModel() {

    private val _biometricState = MutableLiveData<BiometricState>()

    val state: LiveData<BiometricState> get() = _biometricState

    fun setFingerPrint() {
        settingsGateway.setFingerprint()
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _biometricState.value = ShowBiometricLoading }
            .subscribe(
                {
                    _biometricState.value = ShowBiometricSuccess(it.fingerPrintToken!!)
                }, {
                    Timber.d(it, "setFingerPrint Failed")
                    _biometricState.value = ShowBiometricDismissLoading
                    _biometricState.value = ShowBiometricError(it)
                })
            .addTo(disposables)
    }

    fun setTokenFingerPrint(token: String) {
        settingsGateway.setFingerprintCredential(token)
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .doFinally { _biometricState.value = ShowBiometricDismissLoading }
            .subscribe(
                {
                    _biometricState.value = ShowBiometricSetToken
                }, {
                    Timber.d(it, "setTokenFingerPrint Failed")
                    _biometricState.value = ShowBiometricError(it)
                })
            .addTo(disposables)
    }

    fun getTokenFingerPrint() {
        settingsGateway.getFingerprintToken()
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    _biometricState.value = ShowBiometricGetToken(it)
                }, {
                    Timber.d(it, "getTokenFingerPrint Failed")
                    _biometricState.value = ShowBiometricError(it)
                })
            .addTo(disposables)
    }
}

sealed class BiometricState

object ShowBiometricLoading : BiometricState()

object ShowBiometricDismissLoading : BiometricState()

object ShowBiometricSetToken : BiometricState()

data class ShowBiometricSuccess(val token: String) : BiometricState()

data class ShowBiometricGetToken(val token: String) : BiometricState()

data class ShowBiometricError(val throwable: Throwable) : BiometricState()
