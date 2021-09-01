package com.unionbankph.corporate.fund_transfer.presentation.beneficiary_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.fund_transfer.data.gateway.FundTransferGateway
import com.unionbankph.corporate.fund_transfer.data.model.Beneficiary
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class ManageBeneficiaryViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val fundTransferGateway: FundTransferGateway,
    private val settingsGateway: SettingsGateway
) : BaseViewModel() {

    private val _beneficiaryState = MutableLiveData<ManageBeneficiaryState>()

    private val _beneficiaries = MutableLiveData<MutableList<Beneficiary>>()

    val beneficiaryState: LiveData<ManageBeneficiaryState> get() = _beneficiaryState

    val beneficiaries: LiveData<MutableList<Beneficiary>> get() = _beneficiaries

    private var getBeneficiariesDisposable: Disposable? = null

    init {
        hasBeneficiaryCreationPermission()
    }

    fun getBeneficiaries(pageable: Pageable, isInitialLoading: Boolean) {
        getBeneficiariesDisposable?.dispose()
        getBeneficiariesDisposable =
            fundTransferGateway.getBeneficiaries(null, null, pageable = pageable)
                .doOnSuccess {
                    pageable.apply {
                        totalPageCount = it.totalPages
                        isLastPage = !it.hasNextPage
                        increasePage()
                    }
                }
                .map { pageable.combineList(beneficiaries.value, it.results) }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .doOnSubscribe {
                    _beneficiaryState.value = if (isInitialLoading)
                        ShowManageBeneficiaryLoading
                    else
                        ShowManageBeneficiaryEndlessLoading
                }
                .doFinally {
                    _beneficiaryState.value = if (isInitialLoading)
                        ShowManageBeneficiaryDismissLoading
                    else
                        ShowManageBeneficiaryDismissEndlessLoading
                }
                .subscribe(
                    {
                        Timber.e("VIEWMODELL " + JsonHelper.toJson(it))
                        _beneficiaries.value = it
                    }, {
                        Timber.e(it, "getBeneficiaries Failed")
                        if (!isInitialLoading) {
                            pageable.errorPagination(it.message)
                        } else {
                            _beneficiaryState.value = ShowManageBeneficiaryError(it)
                        }
                    }
                )
        getBeneficiariesDisposable?.addTo(disposables)
    }

    fun getBeneficiariesTestData() {
        fundTransferGateway.getBeneficiariesTestData()
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    _beneficiaryState.value = ShowManageBeneficiaryGetTestDataList(it.results)
                }, {
                    Timber.e(it, "getBeneficiariesTestData failed")
                    _beneficiaryState.value = ShowManageBeneficiaryError(it)
                })
            .addTo(disposables)
    }

    private fun hasBeneficiaryCreationPermission() {
        settingsGateway.hasBeneficiaryCreationPermission()
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    _beneficiaryState.value =
                        ShowManageBeneficiaryGetBeneficiaryCreationPermission(it)
                }, {
                    Timber.e(it, "hasBeneficiaryCreationPermission failed")
                    _beneficiaryState.value = ShowManageBeneficiaryError(it)
                }).addTo(disposables)
    }
}

sealed class ManageBeneficiaryState

object ShowManageBeneficiaryLoading : ManageBeneficiaryState()

object ShowManageBeneficiaryEndlessLoading : ManageBeneficiaryState()

object ShowManageBeneficiaryDismissLoading : ManageBeneficiaryState()

object ShowManageBeneficiaryDismissEndlessLoading : ManageBeneficiaryState()

data class ShowManageBeneficiaryGetTestDataList(
    val data: MutableList<Beneficiary>
) : ManageBeneficiaryState()

data class ShowManageBeneficiaryGetBeneficiaryCreationPermission(
    val hasPermission: Boolean
) : ManageBeneficiaryState()

data class ShowManageBeneficiaryError(val throwable: Throwable) : ManageBeneficiaryState()
