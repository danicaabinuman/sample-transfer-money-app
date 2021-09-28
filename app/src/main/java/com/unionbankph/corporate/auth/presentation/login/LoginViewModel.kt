package com.unionbankph.corporate.auth.presentation.login

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.flatMapIf
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.auth.data.AuthGateway
import com.unionbankph.corporate.auth.data.form.LoginFingerprintForm
import com.unionbankph.corporate.auth.data.form.LoginForm
import com.unionbankph.corporate.auth.data.model.Auth
import com.unionbankph.corporate.auth.data.model.UserDetails
import com.unionbankph.corporate.common.domain.exception.NoConnectivityException
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import com.unionbankph.corporate.settings.domain.constant.FeaturesEnum
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.zipWith
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import javax.inject.Inject

class LoginViewModel @Inject constructor(
    private val context: Context,
    private val schedulerProvider: SchedulerProvider,
    private val authGateway: AuthGateway,
    private val settingsGateway: SettingsGateway
) : BaseViewModel() {

    private val _loginState = MutableLiveData<LoginState>()

    val state: LiveData<LoginState> get() = _loginState

    private val _startLogin = MutableLiveData<Event<Boolean>>()

    val startLogin: LiveData<Event<Boolean>> get() = _startLogin

    val isTrustedDeviceOutput = BehaviorSubject.createDefault(false)

    val enableBiometricLogin = BehaviorSubject.createDefault(false)

    val token = BehaviorSubject.createDefault("")

    val fullName = BehaviorSubject.create<String>()

    val emailAddress = BehaviorSubject.create<String>()

    val cdaoFeature = BehaviorSubject.createDefault(true)

    fun hasFingerPrintAndTOTP() {
        settingsGateway.getFingerprintToken()
            .zipWith(settingsGateway.getFingerprintFullname()) { t1, t2 -> Pair(t1, t2) }
            .zipWith(settingsGateway.getFingerprintEmail()) { t1, t2 -> Pair(t1, t2) }
            .zipWith(settingsGateway.hasTOTP()) { t1, t2 -> Pair(t1,t2) }
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    token.onNext(it.first.first.first)
                    fullName.onNext(it.first.first.second)
                    emailAddress.onNext(it.first.second)
                    isTrustedDeviceOutput.onNext(it.second)
                    enableBiometricLogin.onNext(it.second && token.value?.isNotEmpty() ?: false)

                }, {
                    Timber.e(it, "hasFingerPrint Failed")
                    _loginState.value = ShowLoginError(it)
                })
            .addTo(disposables)
    }

    fun getEnabledFeatures() {
        settingsGateway.getEnabledFeatures()
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    isEnabledFeature()
                }, {
                    Timber.e(it, "getEnabledFeatures Failed")
                    if (it is NoConnectivityException) {
                        _loginState.value = ShowLoginConnectivityError(it)
                    } else {
                        isEnabledFeature()
                    }
                })
            .addTo(disposables)
    }

    fun isEnabledFeature() {
        settingsGateway.isEnabledFeature(FeaturesEnum.CDAO_OVERALL)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    cdaoFeature.onNext(it)
                    _startLogin.value = Event(true)
                }, {
                    Timber.e(it, "isEnabledFeature Failed")
                    cdaoFeature.onNext(true)
                    _startLogin.value = Event(true)
                })
            .addTo(disposables)
    }

    fun login(loginForm: LoginForm) {
        authGateway.login(loginForm)
            .flatMapIf(
                { it.requestId == null },
                { userLoginSkipOTP(it).toSingle { it } }
            )
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _loginState.value = ShowLoginLoading }
            .doFinally { _loginState.value = ShowLoginDismissLoading }
            .subscribe(
                {
                    it?.let {
                        if (it.requestId != null) {
                            _loginState.value = ShowLoginDismissLoading
                            if (it.isPolicyAgreed == null) {
                                _loginState.value = ShowLoginSuccess(it)
                            } else {
                                _loginState.value = ShowLoginNoPrivacyPolicy
                            }
                        } else {
                            if (it.isPolicyAgreed == null) {
                                _loginState.value = ShowLoginNoPrivacyPolicy
                            } else {
                                if (it.isPolicyAgreed != null && it.isPolicyAgreed == true) {
                                    _loginState.value = ShowLoginSkipOTPSuccess
                                } else {
                                    _loginState.value = ShowLoginNoPrivacyPolicy
                                }
                            }
                        }
                    }

                }, {
                    Timber.e(it, "Login Failed")
                    _loginState.value = ShowLoginError(it)
                })
            .addTo(disposables)
    }

    private fun userLoginSkipOTP(auth: Auth?): Completable {
        val userDetails = UserDetails(
            auth?.token,
            auth?.role,
            auth?.corporateUser,
            auth?.approvalGroups,
            auth?.isPolicyAgreed,
            auth?.isTrusted,
            auth?.readMcdTerms
        )
        return Observable.just(userDetails)
            .flatMapCompletable {
                authGateway.saveCredential(userDetails)
            }
    }

    fun userLoginFingerPrint(loginFingerprintForm: LoginFingerprintForm) {
        authGateway.loginFingerprint(loginFingerprintForm)
            .flatMapCompletable { authGateway.saveCredential(it) }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _loginState.value = ShowLoginLoading }
            .doFinally { _loginState.value = ShowLoginDismissLoading }
            .subscribe(
                {
                    _loginState.value = ShowLoginFingerprintSuccess
                }, {
                    Timber.e(it, "userLoginFingerPrint Failed")
                    _loginState.value = ShowLoginError(it)
                })
            .addTo(disposables)
    }

    fun refreshNotificationTokenIfNull() {
        settingsGateway.hasNotificationToken()
            .flatMapCompletable {
                if (!it) {
                    settingsGateway.setNotificationToken()
                } else {
                    Completable.complete()
                }
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    _loginState.value = ShowLoginHasNotificationToken
                }, {
                    Timber.e(it, "hasTOTP Failed")
                    _loginState.value = ShowLoginError(it)
                })
            .addTo(disposables)
    }
}

sealed class LoginState

object ShowLoginLoading : LoginState()

object ShowLoginDismissLoading : LoginState()

object ShowLoginFingerprintSuccess : LoginState()

object ShowLoginSkipOTPSuccess : LoginState()

object ShowLoginNoPrivacyPolicy : LoginState()

object ShowLoginHasNotificationToken : LoginState()

data class ShowLoginSuccess(val auth: Auth) : LoginState()

data class ShowLoginUserGetEmailAddress(val email: String) : LoginState()

data class ShowLoginError(val throwable: Throwable) : LoginState()

data class ShowLoginConnectivityError(val throwable: Throwable) : LoginState()
