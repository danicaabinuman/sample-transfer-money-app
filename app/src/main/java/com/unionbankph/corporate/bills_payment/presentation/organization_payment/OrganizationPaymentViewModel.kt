package com.unionbankph.corporate.bills_payment.presentation.organization_payment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.approval.data.model.Transaction
import com.unionbankph.corporate.bills_payment.data.gateway.BillsPaymentGateway
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.general.data.model.TransactionFilterForm
import com.unionbankph.corporate.settings.data.constant.PermissionNameEnum
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import javax.inject.Inject

class OrganizationPaymentViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val billsPaymentGateway: BillsPaymentGateway,
    private val settingsGateway: SettingsGateway
) : BaseViewModel() {

    private val _transactions = MutableLiveData<MutableList<Transaction>>()

    val transactions: LiveData<MutableList<Transaction>> get() = _transactions

    private val _testTransactions = MutableLiveData<MutableList<Transaction>>()

    val testTransactions: LiveData<MutableList<Transaction>> get() = _testTransactions

    private val _hasCreateTransaction = MutableLiveData<Boolean>()

    val hasCreateTransaction: LiveData<Boolean> get() = _hasCreateTransaction

    private var getOrganizationPaymentsDisposable: Disposable? = null

    val transactionFilterForm = BehaviorSubject.createDefault(
        TransactionFilterForm()
    )

    val hasFilter = BehaviorSubject.create<Boolean>()

    val pageable = Pageable()

    init {
        checkProductPermissions()
    }

    fun getOrganizationPayments(pageable: Pageable, isInitialLoading: Boolean) {
        getOrganizationPaymentsDisposable?.dispose()
        getOrganizationPaymentsDisposable =
            billsPaymentGateway.getOrganizationPayments(
                transactionFilterForm.value?.startDate,
                transactionFilterForm.value?.endDate,
                transactionFilterForm.value?.statuses,
                transactionFilterForm.value?.biller?.serviceId,
                pageable
            )
                .doOnSuccess {
                    pageable.apply {
                        totalPageCount = it.totalPages
                        isLastPage = !it.hasNextPage
                        increasePage()
                    }
                }
                .map { pageable.combineList(_transactions.value, it.results) }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .doOnSubscribe { showLoading(isInitialLoading) }
                .doFinally { dismissLoading(isInitialLoading) }
                .subscribe(
                    {
                        _transactions.value = it
                    }, {
                        Timber.e(it, "getOrganizationPayments Failed")
                        if (!isInitialLoading) {
                            pageable.errorPagination(it.message)
                        } else {
                            _uiState.value = Event(UiState.Error(it))
                        }
                    }
                )
        getOrganizationPaymentsDisposable?.addTo(disposables)
    }

    fun getOrganizationPaymentsTestData() {
        billsPaymentGateway.getOrganizationPaymentsTestData()
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    _testTransactions.value = it.results
                }, {
                    Timber.e(it, "getOrganizationPaymentsTestData")
                    _uiState.value = Event(UiState.Error(it))
                }
            )
            .addTo(disposables)
    }

    private fun checkProductPermissions() {
        settingsGateway.hasPermissionChannel(
            PermissionNameEnum.BILLS_PAYMENT.value,
            Constant.Permissions.CODE_BP_CREATEBILLSPAYMENT
        )
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    _hasCreateTransaction.value = it.id != null
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
