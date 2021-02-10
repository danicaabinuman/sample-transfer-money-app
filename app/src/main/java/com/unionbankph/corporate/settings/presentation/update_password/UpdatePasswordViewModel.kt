package com.unionbankph.corporate.settings.presentation.update_password

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.auth.data.AuthGateway
import com.unionbankph.corporate.auth.data.form.ActivationPasswordForm
import com.unionbankph.corporate.auth.data.form.ChangePasswordForm
import com.unionbankph.corporate.auth.data.form.NominatePasswordForm
import com.unionbankph.corporate.auth.data.form.ResetPasswordForm
import com.unionbankph.corporate.auth.data.form.ResetPasswordVerifyForm
import com.unionbankph.corporate.auth.data.model.Auth
import com.unionbankph.corporate.common.data.model.ApiError
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class UpdatePasswordViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val authGateway: AuthGateway,
    private val settingsGateway: SettingsGateway
) : BaseViewModel() {

    private val _updatePasswordState = MutableLiveData<UpdatePasswordState>()

    val state: LiveData<UpdatePasswordState> get() = _updatePasswordState

    fun resetPasswordNew(resetPasswordForm: ResetPasswordForm) {
        authGateway.resetPassVerify(
                ResetPasswordVerifyForm(
                    resetPasswordForm.passwordToken
                )
            )
            .flatMap {
                resetPasswordForm.resetPwTokenId = it.resetPWTokenId
                authGateway.resetPassNew(resetPasswordForm)
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _updatePasswordState.value = ShowUPasswordLoading }
            .doFinally { _updatePasswordState.value = ShowUPasswordDismissLoading }
            .subscribe(
                {
                    _updatePasswordState.value = ShowUPasswordUpdateSuccess
                }, {
                    Timber.e(it, "resetPasswordNew Failed")
                    _updatePasswordState.value = ShowUPasswordError(it)
                })
            .addTo(disposables)
    }

    fun nominatePassword(activationPasswordForm: ActivationPasswordForm, password: String) {
        authGateway.nominatePasswordActivation(activationPasswordForm)
            .flatMap {
                authGateway.nominatePassword(
                    NominatePasswordForm(
                        password = password,
                        passwordToken = it.passwordToken
                    )
                )
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _updatePasswordState.value = ShowUPasswordLoading }
            .doFinally { _updatePasswordState.value = ShowUPasswordDismissLoading }
            .subscribe(
                {
                    _updatePasswordState.value = ShowUPasswordSuccess(it)
                }, {
                    Timber.e(it, "resetPasswordNew Failed")
                    try {
                        val apiError = JsonHelper.fromJson<ApiError>(it.message)
                        _updatePasswordState.value = ShowUPasswordNominatePasswordError(apiError)
                    } catch (e: Exception) {
                        _updatePasswordState.value = ShowUPasswordError(it)
                    }
                })
            .addTo(disposables)
    }

    fun changePassword(changePasswordForm: ChangePasswordForm) {
        settingsGateway.changePassword(changePasswordForm)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _updatePasswordState.value = ShowUPasswordLoading }
            .doFinally { _updatePasswordState.value = ShowUPasswordDismissLoading }
            .subscribe(
                {
                    _updatePasswordState.value = ShowUPasswordUpdateSuccess
                }, {
                    Timber.e(it, "changePassword Failed")
                    _updatePasswordState.value = ShowUPasswordError(it)
                })
            .addTo(disposables)
    }
}

sealed class UpdatePasswordState

object ShowUPasswordLoading : UpdatePasswordState()

object ShowUPasswordDismissLoading : UpdatePasswordState()

object ShowUPasswordUpdateSuccess : UpdatePasswordState()

data class ShowUPasswordSuccess(val auth: Auth) : UpdatePasswordState()

data class ShowUPasswordNominatePasswordError(val apiError: ApiError) : UpdatePasswordState()

data class ShowUPasswordError(val throwable: Throwable) : UpdatePasswordState()
