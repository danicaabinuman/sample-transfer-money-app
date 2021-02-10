package com.unionbankph.corporate.fund_transfer.presentation.beneficiary_selection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.PagedDto
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.fund_transfer.data.gateway.FundTransferGateway
import com.unionbankph.corporate.fund_transfer.data.model.Beneficiary
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class BeneficiaryViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val fundTransferGateway: FundTransferGateway
) : BaseViewModel() {

    private val _beneficiaryState = MutableLiveData<BeneficiaryState>()

    private val beneficiaryMasterMutableLiveData = MutableLiveData<PagedDto<Beneficiary>>()

    val state: LiveData<BeneficiaryState> get() = _beneficiaryState

    val beneficiaryMasterLiveData: LiveData<PagedDto<Beneficiary>> get() = beneficiaryMasterMutableLiveData

    private var getBeneficiariesDisposable: Disposable? = null

    fun getBeneficiaries(
        channelId: String,
        sourceId: String,
        pageable: Pageable,
        isInitialLoading: Boolean
    ) {
        getBeneficiariesDisposable?.dispose()
        getBeneficiariesDisposable =
            fundTransferGateway.getBeneficiaries(channelId, sourceId, pageable)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .doOnSubscribe {
                    if (isInitialLoading) {
                        _beneficiaryState.value = ShowBeneficiaryLoading
                    } else {
                        _beneficiaryState.value = ShowBeneficiaryEndlessLoading
                    }
                }
                .doFinally {
                    if (isInitialLoading) {
                        _beneficiaryState.value = ShowBeneficiaryDismissLoading
                    } else {
                        _beneficiaryState.value = ShowBeneficiaryDismissEndlessLoading
                    }
                }
                .subscribe(
                    {
                        beneficiaryMasterMutableLiveData.value = it
                    }, {
                        Timber.e(it, "getBeneficiaries Failed")
                        if (!isInitialLoading) {
                            pageable.errorPagination(it.message)
                        } else {
                            _beneficiaryState.value = ShowBeneficiaryError(it)
                        }
                    })
        getBeneficiariesDisposable?.addTo(disposables)
    }
}

sealed class BeneficiaryState

object ShowBeneficiaryLoading : BeneficiaryState()

object ShowBeneficiaryEndlessLoading : BeneficiaryState()

object ShowBeneficiaryDismissLoading : BeneficiaryState()

object ShowBeneficiaryDismissEndlessLoading : BeneficiaryState()

data class ShowBeneficiaryError(val throwable: Throwable) : BeneficiaryState()
