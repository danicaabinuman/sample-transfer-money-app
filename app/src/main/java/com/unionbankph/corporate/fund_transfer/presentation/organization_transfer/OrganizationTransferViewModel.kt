package com.unionbankph.corporate.fund_transfer.presentation.organization_transfer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.combineWith
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.approval.data.model.Transaction
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.fund_transfer.data.gateway.FundTransferGateway
import com.unionbankph.corporate.general.data.model.TransactionFilterForm
import com.unionbankph.corporate.settings.data.constant.PermissionNameEnum
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.Singles
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import javax.inject.Inject

class OrganizationTransferViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val fundTransferGateway: FundTransferGateway,
    private val settingsGateway: SettingsGateway
) : BaseViewModel() {

    private val _hasViewBeneficiaryPermission = MutableLiveData<Boolean>()

    val hasViewBeneficiaryPermission: LiveData<Boolean> get() = _hasViewBeneficiaryPermission

    private val _hasScheduledTransferPermission = MutableLiveData<Boolean>()

    val hasScheduledTransferPermission: LiveData<Boolean> get() = _hasScheduledTransferPermission

    private val _hasCreateTransaction = MutableLiveData<Boolean>()

    val hasCreateTransaction: LiveData<Boolean> get() = _hasCreateTransaction

    val onlyCreateTransaction = hasScheduledTransferPermission
        .combineWith(hasViewBeneficiaryPermission) { t1, t2 ->
            !t1.notNullable() && !t2.notNullable()
        }

    private val _transactions = MutableLiveData<MutableList<Transaction>>()

    val transactions: LiveData<MutableList<Transaction>> get() = _transactions

    private val _testTransactions = MutableLiveData<MutableList<Transaction>>()

    val testTransactions: LiveData<MutableList<Transaction>> get() = _testTransactions

    private var getOrganizationTransfersDisposable: Disposable? = null

    val transactionFilterForm = BehaviorSubject.createDefault(
        TransactionFilterForm()
    )

    val hasFilter = BehaviorSubject.create<Boolean>()

    val pageable = Pageable()

    init {
        checkProductPermissions()
    }

    fun getOrganizationTransfers(isInitialLoading: Boolean) {
        getOrganizationTransfersDisposable?.dispose()
        getOrganizationTransfersDisposable = fundTransferGateway.getOrganizationTransfers(
            transactionFilterForm.value?.startDate,
            transactionFilterForm.value?.endDate,
            transactionFilterForm.value?.channels,
            transactionFilterForm.value?.statuses,
            pageable
        )
            .doOnSuccess {
                pageable.apply {
                    totalPageCount = it.totalPages
                    isLastPage = !it.hasNextPage
                    increasePage()
                }
            }
            .map { pageable.combineList(transactions.value, it.results) }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { showLoading(isInitialLoading) }
            .doFinally { dismissLoading(isInitialLoading) }
            .subscribe(
                {
                    _transactions.value = it
                }, {
                    Timber.e(it, "getOrganizationTransfers Failed")
                    if (!isInitialLoading) {
                        pageable.errorPagination(it.message)
                    } else {
                        _uiState.value = Event(UiState.Error(it))
                    }
                }
            )
        getOrganizationTransfersDisposable?.addTo(disposables)
    }

    fun getOrganizationTransfersTestData() {
        fundTransferGateway.getOrganizationTransfersTestData()
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    _testTransactions.value = it.results
                }, {
                    Timber.e(it, "getOrganizationTransfersTestData failed")
                    _uiState.value = Event(UiState.Error(it))
                })
            .addTo(disposables)
    }

    private fun checkProductPermissions() {
        Singles.zip(
            settingsGateway.hasPermissionChannel(
                PermissionNameEnum.FUND_TRANSFER.value,
                Constant.Permissions.CODE_FT_CREATETRANSACTIONS
            ),
            settingsGateway.hasDeleteScheduledTransferPermission(),
            settingsGateway.hasViewBeneficiaryPermission()
        )
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    _hasCreateTransaction.value = it.first.id != null
                    _hasScheduledTransferPermission.value = it.second
                    _hasViewBeneficiaryPermission.value = it.third
                }, {
                    Timber.e(it, "checkProductPermissions failed")
                    _uiState.value = Event(UiState.Error(it))
                }
            ).addTo(disposables)
    }

    private fun showLoading(isInitialLoading: Boolean) {
        if (!isInitialLoading) {
            pageable.isLoadingPagination = true
        }
        _uiState.value = Event(UiState.Loading)
    }

    private fun dismissLoading(isInitialLoading: Boolean) {
        if (!isInitialLoading) {
            pageable.isLoadingPagination = false
        }
        _uiState.value = Event(UiState.Complete)
    }

}
