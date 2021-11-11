package com.unionbankph.corporate.user_creation.presentation.nominate_password

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.auth.data.AuthGateway
import com.unionbankph.corporate.auth.data.model.*
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.user_creation.data.form.UcNominatePasswordForm
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class UcNominatePasswordViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val authGateway: AuthGateway
) : BaseViewModel() {

    private val _navigateResult = MutableLiveData<Event<UserCreationAuth>>()

    val navigateResult: LiveData<Event<UserCreationAuth>> get() = _navigateResult

    fun onClickedNext(password: String, secretToken: String, requestId: String) {
        val form = UcNominatePasswordForm().apply {
            this.password = password
            this.secretToken = secretToken
            this.requestId = requestId
        }
        nominatePassword(form)
    }

    private fun nominatePassword(form: UcNominatePasswordForm) {
        Log.e("form.secretToken",""+form.secretToken)
        Log.e("form.requestId",""+form.requestId)
        authGateway.userCreationNominatePassword(form)
            .flatMap { cacheUserDetails(it).toSingle { it } }
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

    private fun cacheUserDetails(auth: UserCreationAuth?): Completable {
        val userDetails = UserCreationDetails(
            auth?.token,
            auth?.role,
            auth?.corporateUser,
            auth?.approvalGroups,
            auth?.isPolicyAgreed,
            auth?.isTrusted,
            auth?.readMcdTerms,
            auth?.trialDaysRemaining,
            auth?.trialMode,
        )
        return Observable.just(userDetails)
            .flatMapCompletable {
                authGateway.saveCredential(userDetails)
            }
    }
}