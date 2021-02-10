package com.unionbankph.corporate.bills_payment.presentation.bills_payment_confirmation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.auth.data.model.Auth
import com.unionbankph.corporate.bills_payment.data.form.BillsPaymentForm
import com.unionbankph.corporate.bills_payment.data.gateway.BillsPaymentGateway
import com.unionbankph.corporate.bills_payment.data.model.BillerField
import com.unionbankph.corporate.bills_payment.data.model.BillsPaymentVerify
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class BillsPaymentConfirmationViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val billsPaymentGateway: BillsPaymentGateway
) : BaseViewModel() {

    private val _billsPaymentConfirmationState = MutableLiveData<BillsPaymentConfirmationState>()

    val confirmationState: LiveData<BillsPaymentConfirmationState> get() = _billsPaymentConfirmationState

    fun billsPayment(billsPaymentForm: BillsPaymentForm) {
        billsPaymentGateway.billsPayment(billsPaymentForm)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe {
                _billsPaymentConfirmationState.value =
                    ShowBillsPaymentConfirmationLoading
            }
            .doFinally {
                _billsPaymentConfirmationState.value =
                    ShowBillsPaymentConfirmationDismissLoading
            }
            .subscribe(
                {
                    if (it.requestId != null) {
                        _billsPaymentConfirmationState.value =
                            ShowBillsPaymentConfirmationSuccess(
                                Auth(
                                    mobileNumber = it.mobileNumber,
                                    requestId = it.requestId,
                                    otpType = it.otpType
                                )
                            )
                    } else {
                        _billsPaymentConfirmationState.value =
                            ShowBillsPaymentConfirmationSkipOTPSuccess(
                                it
                            )
                    }
                }, {
                    Timber.e(it, "fundTransferUBP failed")
                    _billsPaymentConfirmationState.value =
                        ShowBillsPaymentConfirmationError(
                            it
                        )
                }).addTo(disposables)
    }

    fun getBillerFields(billerId: String) {
        billsPaymentGateway.getBillerFields(billerId)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe {
                _billsPaymentConfirmationState.value =
                    ShowBillsPaymentConfirmationLoading
            }
            .doFinally {
                _billsPaymentConfirmationState.value =
                    ShowBillsPaymentConfirmationDismissLoading
            }
            .subscribe(
                {
                    _billsPaymentConfirmationState.value =
                        ShowBillsPaymentConfirmationGetBillerFields(
                            it
                        )
                }, {
                    Timber.e(it, "getOrgDetails failed")
                    _billsPaymentConfirmationState.value =
                        ShowBillsPaymentConfirmationError(
                            it
                        )
                })
            .addTo(disposables)
    }
}

sealed class BillsPaymentConfirmationState

object ShowBillsPaymentConfirmationLoading : BillsPaymentConfirmationState()

object ShowBillsPaymentConfirmationDismissLoading : BillsPaymentConfirmationState()

data class ShowBillsPaymentConfirmationSuccess(val auth: Auth) : BillsPaymentConfirmationState()

data class ShowBillsPaymentConfirmationSkipOTPSuccess(val billsPaymentVerify: BillsPaymentVerify) :
    BillsPaymentConfirmationState()

data class ShowBillsPaymentConfirmationGetBillerFields(val billerFields: MutableList<BillerField>) :
    BillsPaymentConfirmationState()

data class ShowBillsPaymentConfirmationError(val throwable: Throwable) :
    BillsPaymentConfirmationState()
