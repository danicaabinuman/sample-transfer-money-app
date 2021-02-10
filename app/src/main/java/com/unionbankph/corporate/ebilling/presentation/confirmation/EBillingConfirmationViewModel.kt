package com.unionbankph.corporate.ebilling.presentation.confirmation

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.ebilling.domain.form.EBillingForm
import io.reactivex.Single
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by herald on 10/28/20
 */
class EBillingConfirmationViewModel
@Inject constructor(
    private val context: Context
) : BaseViewModel() {

    private val _navigateGenerate = MutableLiveData<Event<EBillingForm>>()

    val navigateGenerate: LiveData<Event<EBillingForm>> get() = _navigateGenerate

    private val _eBillingForm = MutableLiveData<EBillingForm>()

    val eBillingForm: LiveData<EBillingForm> get() = _eBillingForm

    fun parseEBillingForm(formString: String?) {
        Single.just(formString)
            .map { JsonHelper.fromJson<EBillingForm>(formString) }
            .subscribe(
                {
                    _eBillingForm.value = it
                }, {
                    Timber.e(it, "parseEBillingForm")
                    _uiState.value = Event(UiState.Error(it))
                }
            ).addTo(disposables)
    }

    fun generateQRCode() {
        eBillingForm.value?.let {
            it.qrCodePath = "content"
            _navigateGenerate.value = Event(it)
        }
    }

}