package com.unionbankph.corporate.dao.presentation.welcome_enter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.getDisposableSingleObserver
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.dao.domain.interactor.GetSignatoryDetails
import com.unionbankph.corporate.dao.domain.interactor.SaveTokenDeepLink
import com.unionbankph.corporate.dao.domain.model.SignatoryDetail
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

/**
 * Created by herald25santos on 3/17/20
 */
class DaoWelcomeEnterViewModel @Inject constructor(
    private val getSignatoryDetails: GetSignatoryDetails,
    private val saveTokenDeepLink: SaveTokenDeepLink
) : BaseViewModel() {

    val isLoadedScreen = BehaviorSubject.createDefault(false)

    private val _navigateNextStep = MutableLiveData<Event<Boolean>>()
    val navigateNextStep: LiveData<Event<Boolean>> get() = _navigateNextStep

    var input: Input = Input()

    var output: Output = Output()

    inner class Input {
        val isValidFormInput = BehaviorSubject.createDefault(false)
    }

    inner class Output {
        val _signatoriesDetail = MutableLiveData<SignatoryDetail>()
        val signatoryDetail: LiveData<SignatoryDetail> get() = _signatoriesDetail
    }

    fun hasValidForm() = input.isValidFormInput.value ?: false

    fun loadDeepLink(token: String) {
        if (isLoadedScreen.value == false) {
            saveTokenDeepLink(token)
        }
    }

    private fun saveTokenDeepLink(token: String) {
        saveTokenDeepLink.execute(params = token)
            .subscribe(
                {
                    onClickedSubmit()
                }, {
                    _uiState.value = Event(UiState.Error(it))
                }
            )
            .addTo(disposables)
    }

    fun onClickedSubmit(referenceNumber: String? = null) {
        getSignatoryDetails.execute(
            getDisposableSingleObserver(
                {
                    output._signatoriesDetail.value = it
                    _navigateNextStep.value = Event(
                        it.privatePolicy == true && it.termsAndCondition == true
                    )
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
            params = referenceNumber
        ).addTo(disposables)
    }

}
