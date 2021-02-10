package com.unionbankph.corporate.fund_transfer.presentation.beneficiary_detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.fund_transfer.data.gateway.FundTransferGateway
import com.unionbankph.corporate.fund_transfer.data.model.BeneficiaryDetailDto
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class ManageBeneficiaryDetailViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val fundTransferGateway: FundTransferGateway
) : BaseViewModel() {

    private val _beneficiaryDetailState = MutableLiveData<BeneficiaryDetailState>()

    val beneficiaryDetailState: LiveData<BeneficiaryDetailState> get() = _beneficiaryDetailState

    fun getBeneficiaryDetail(id: String) {
        fundTransferGateway.getBeneficiaryDetail(id)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _beneficiaryDetailState.value = ShowBeneficiaryDetailLoading }
            .doOnSuccess { _beneficiaryDetailState.value = ShowBeneficiaryDetailDismissLoading }
            .subscribe(
                {
                    _beneficiaryDetailState.value = ShowBeneficiaryDetailGetBeneficiaryDetail(it)
                }, {
                    Timber.e(it, "getBeneficiaryDetail Failed")
                    _beneficiaryDetailState.value = ShowBeneficiaryDetailError(it)
                })
            .addTo(disposables)
    }

    fun deleteBeneficiary(id: String) {
        fundTransferGateway.deleteBeneficiary(id)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe {
                _beneficiaryDetailState.value = ShowBeneficiaryDetailProgressBarLoading
            }
            .doFinally {
                _beneficiaryDetailState.value = ShowBeneficiaryDetailDismissProgressBarLoading
            }
            .subscribe(
                {
                    _beneficiaryDetailState.value =
                        ShowBeneficiaryDetailDeleteBeneficiary(it.message ?: "")
                }, {
                    Timber.e(it, "deleteBeneficiary Failed")
                    _beneficiaryDetailState.value = ShowBeneficiaryDetailError(it)
                })
            .addTo(disposables)
    }
}

sealed class BeneficiaryDetailState

object ShowBeneficiaryDetailProgressBarLoading : BeneficiaryDetailState()

object ShowBeneficiaryDetailDismissProgressBarLoading : BeneficiaryDetailState()

object ShowBeneficiaryDetailLoading : BeneficiaryDetailState()

object ShowBeneficiaryDetailDismissLoading : BeneficiaryDetailState()

data class ShowBeneficiaryDetailGetBeneficiaryDetail(
    val data: BeneficiaryDetailDto
) : BeneficiaryDetailState()

data class ShowBeneficiaryDetailDeleteBeneficiary(val message: String) : BeneficiaryDetailState()

data class ShowBeneficiaryDetailError(val throwable: Throwable) : BeneficiaryDetailState()
