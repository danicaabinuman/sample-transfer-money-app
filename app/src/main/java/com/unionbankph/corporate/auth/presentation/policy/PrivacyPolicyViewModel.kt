package com.unionbankph.corporate.auth.presentation.policy

import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.auth.data.AuthGateway
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.dao.data.form.AgreeTNCPrivacyForm
import com.unionbankph.corporate.dao.domain.interactor.DaoAgreement
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import javax.inject.Inject

class PrivacyPolicyViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val authGateway: AuthGateway,
    private val daoAgreement: DaoAgreement
) : BaseViewModel() {

    val isCheckedTNCAgreement = BehaviorSubject.createDefault(false)
    val isCheckedPrivacyAgreement = BehaviorSubject.createDefault(false)
    val isClickedNext = BehaviorSubject.createDefault(false)

    fun daoAgreement() {
        val agreeTNCPrivacyForm = AgreeTNCPrivacyForm().apply {
            hasAgreedToPrivatePolicy = isCheckedPrivacyAgreement.value
            hasAgreedToTermsAndCondition = isCheckedTNCAgreement.value
        }
        daoAgreement.execute(
            params = agreeTNCPrivacyForm,
            doOnSubscribeEvent = {
                _uiState.value = Event(UiState.Loading)
            },
            doFinallyEvent = {
                _uiState.value = Event(UiState.Complete)
            }
        ).subscribe(
            {
                _uiState.value = Event(UiState.Success)
            }, {
                _uiState.value = Event(UiState.Error(it))
            }
        ).addTo(disposables)
    }

    fun privacyPolicy() {
        authGateway.privacyPolicy()
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _uiState.value = Event(UiState.Loading) }
            .doFinally { _uiState.value = Event(UiState.Complete) }
            .subscribe(
                {
                    _uiState.value = Event(UiState.Success)
                }, {
                    Timber.e(it, "privacyPolicy Failed")
                    _uiState.value = Event(UiState.Error(it))
                })
            .addTo(disposables)
    }

    fun agreeMCDTerms() {
        authGateway.agreeMCDTerms()
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _uiState.value = Event(UiState.Loading) }
            .doFinally { _uiState.value = Event(UiState.Complete) }
            .subscribe(
                {
                    _uiState.value = Event(UiState.Success)
                }, {
                    Timber.e(it, "agreeMCDTerms Failed")
                    _uiState.value = Event(UiState.Error(it))
                })
            .addTo(disposables)
    }

    fun clearLoginCredential() {
        authGateway.clearLoginCredential()
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    _uiState.value = Event(UiState.Exit)
                }, {
                    Timber.e(it, "clearLoginCredential failed")
                    _uiState.value = Event(UiState.Error(it))
                })
            .addTo(disposables)
    }
}
