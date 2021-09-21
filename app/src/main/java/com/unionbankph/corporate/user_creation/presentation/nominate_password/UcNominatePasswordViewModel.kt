package com.unionbankph.corporate.user_creation.presentation.nominate_password

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.auth.data.AuthGateway
import com.unionbankph.corporate.auth.data.model.UserCreationOTPVerified
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.user_creation.data.form.UcNominatePasswordForm
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class UcNominatePasswordViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val authGateway: AuthGateway
) : BaseViewModel() {

    private val _navigateResult = MutableLiveData<Event<UserCreationOTPVerified>>()

    val navigateResult: LiveData<Event<UserCreationOTPVerified>> get() = _navigateResult

    fun onClickedNext(password: String, secretToken: String) {
        val form = UcNominatePasswordForm().apply {
            this.password = password
            this.secretToken = secretToken
        }
        nominatePassword(form)
    }

    private fun nominatePassword(form: UcNominatePasswordForm) {
        authGateway.userCreationNominatePassword(form)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _uiState.value = Event(UiState.Loading) }
            .doFinally { _uiState.value = Event(UiState.Complete) }
            .subscribe (
                {
                    _navigateResult.value = Event(it)
                },
                {
                    Timber.e("uc nominatePassword err: %s", it.message)
                    _uiState.value = Event(UiState.Error(it))
                }
            ).addTo(disposables)
    }
}