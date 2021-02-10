package com.unionbankph.corporate.auth.presentation.password_recovery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.auth.data.AuthGateway
import com.unionbankph.corporate.auth.data.form.PasswordRecoveryForm
import com.unionbankph.corporate.auth.data.model.Auth
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class PasswordRecoveryViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val authGateway: AuthGateway
) : BaseViewModel() {

    private val _passwordRecoveryState = MutableLiveData<PasswordRecoveryState>()

    val state: LiveData<PasswordRecoveryState> get() = _passwordRecoveryState

    fun resetPass(passwordRecoveryForm: PasswordRecoveryForm) {
        authGateway.resetPass(passwordRecoveryForm)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _passwordRecoveryState.value = ShowPRecoveryLoading }
            .doFinally { _passwordRecoveryState.value = ShowPRecoveryDismissLoading }
            .subscribe({
                           _passwordRecoveryState.value = ShowPRecoverySuccess(it)
                       }, {
                           Timber.d(it, "Password Recovery Failed")
                           _passwordRecoveryState.value = ShowPRecoveryError(it)
                       })
            .addTo(disposables)
    }
}

sealed class PasswordRecoveryState

object ShowPRecoveryLoading : PasswordRecoveryState()

object ShowPRecoveryDismissLoading : PasswordRecoveryState()

data class ShowPRecoverySuccess(val auth: Auth) : PasswordRecoveryState()

data class ShowPRecoveryError(val throwable: Throwable) : PasswordRecoveryState()
