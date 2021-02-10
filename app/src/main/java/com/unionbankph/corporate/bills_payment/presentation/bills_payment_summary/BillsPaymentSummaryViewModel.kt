package com.unionbankph.corporate.bills_payment.presentation.bills_payment_summary

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import com.unionbankph.corporate.settings.domain.constant.FeaturesEnum
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import javax.inject.Inject

class BillsPaymentSummaryViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val settingsGateway: SettingsGateway
) : BaseViewModel() {

    private val _billsPaymentFormState = MutableLiveData<BillsPaymentSummaryState>()

    val summaryState: LiveData<BillsPaymentSummaryState> get() = _billsPaymentFormState

    val contextualClassFeatureToggle = BehaviorSubject.create<Boolean>()

    init {
        isEnabledFeature()
    }

    private fun isEnabledFeature() {
        settingsGateway.isEnabledFeature(FeaturesEnum.TRANSACTION_CONTEXTUAL)
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    contextualClassFeatureToggle.onNext(it)
                }, {
                    contextualClassFeatureToggle.onNext(false)
                    Timber.e(it, "isEnabledFeature failed")
                }
            ).addTo(disposables)
    }

}

sealed class BillsPaymentSummaryState

data class ShowBillsPaymentSummaryError(val throwable: Throwable) : BillsPaymentSummaryState()
