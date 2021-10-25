package com.unionbankph.corporate.instapay_qr.presentation.instapay_qr_scanner

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.getDisposableSingleObserver
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.instapay_qr.domain.model.SuccessQRReference
import com.unionbankph.corporate.instapay_qr.domain.model.form.GenerateFTForm
import com.unionbankph.corporate.instapay_qr.domain.usecase.GenerateFTUseCase
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class InstapayQrScannerViewModel
@Inject constructor(
    private val generateFTUseCase: GenerateFTUseCase
) : BaseViewModel(){

    private val _successQrReference = MutableLiveData<SuccessQRReference>()
    val successQrReference: LiveData<SuccessQRReference>
    get() = _successQrReference

    fun submitQrScanned(qrString: String){
        generateFT(
            GenerateFTForm().apply {
                this.qrContent = qrString
            }
        )
    }

    private fun generateFT(generateFTForm: GenerateFTForm){

        generateFTUseCase.execute(
            getDisposableSingleObserver(
                {
                    _successQrReference.value = it
                }, {
                    Timber.e(it, "generateFT")
                    _uiState.value = Event(UiState.Error(it))
                }
            ),
            doOnSubscribeEvent = {
                _uiState.value = Event(UiState.Loading)
            },
            doFinallyEvent = {
                _uiState.value = Event(UiState.Complete)
            },
            params = generateFTForm
        ).addTo(disposables)
    }
}