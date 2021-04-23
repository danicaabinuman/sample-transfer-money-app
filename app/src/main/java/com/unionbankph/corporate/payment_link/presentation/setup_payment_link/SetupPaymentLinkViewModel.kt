package com.unionbankph.corporate.payment_link.presentation.setup_payment_link

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.getDisposableSingleObserver
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.payment_link.domain.model.form.CreateMerchantForm
import com.unionbankph.corporate.payment_link.domain.model.response.CreateMerchantResponse
import com.unionbankph.corporate.payment_link.domain.model.response.ValidateMerchantByOrganizationResponse
import com.unionbankph.corporate.payment_link.domain.usecase.CreateMerchantUseCase
import com.unionbankph.corporate.payment_link.domain.usecase.ValidateMerchantUseCase
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class SetupPaymentLinkViewModel
@Inject constructor(
    private val createMerchantUseCase: CreateMerchantUseCase,
    private val validateMerchantUseCase: ValidateMerchantUseCase
) : BaseViewModel() {

    private val _createMerchantResponse = MutableLiveData<CreateMerchantResponse>()
    private val _validateMerchantResponse = MutableLiveData<ValidateMerchantByOrganizationResponse>()

    val createMerchantResponse: LiveData<CreateMerchantResponse>
        get() =
            _createMerchantResponse

    val validateMerchantResponse: LiveData<ValidateMerchantByOrganizationResponse>
        get() = _validateMerchantResponse

    fun createMerchant(form: CreateMerchantForm){

        createMerchantUseCase.execute(
            getDisposableSingleObserver(
                {
                    _createMerchantResponse.value = it
                }, {
                    Timber.e(it, "createMerchant")
                    _uiState.value = Event(UiState.Error(it))
                }
            ),
            doOnSubscribeEvent = {
                _uiState.value = Event(UiState.Loading)
            },
            doFinallyEvent = {
                _uiState.value = Event(UiState.Complete)
            },
            params = form
        ).addTo(disposables)
    }

    fun validateMerchant(){
        validateMerchantUseCase.execute(
            getDisposableSingleObserver(
                {
                    _validateMerchantResponse.value = it
                }, {
                    Timber.e(it, "validateMerchant")
                    _uiState.value = Event(UiState.Error(it))
                }
            ),
            doOnSubscribeEvent = {
                _uiState.value = Event(UiState.Loading)
            },
            doFinallyEvent = {
                _uiState.value = Event(UiState.Complete)
            }
        ).addTo(disposables)
    }
}
