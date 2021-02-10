package com.unionbankph.corporate.account.presentation.source_account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.account.data.gateway.AccountGateway
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.auth.data.model.RoleAccountPermissions
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.PermissionCollection
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import javax.inject.Inject

class SourceAccountViewModel @Inject constructor(
    private val accountGateway: AccountGateway,
    private val settingsGateway: SettingsGateway
) : BaseViewModel() {

    private val _accountState = MutableLiveData<SourceAccountState>()
    val state: LiveData<SourceAccountState> get() = _accountState

    private val _accounts = MutableLiveData<MutableList<Account>>()

    val accounts: LiveData<MutableList<Account>> = _accounts

    private var getAccountsPaginated: Disposable? = null

    var isSelectAll = BehaviorSubject.createDefault(true)

    var isFromEdit = BehaviorSubject.createDefault(false)

    var totalElements = BehaviorSubject.createDefault(0)

    var allAccountsSelected: Boolean = true

    var totalSelected: Int = 0

    var excludedAccountIds: MutableList<Int> = mutableListOf()

    var selectedAccounts: MutableList<Account> = mutableListOf()

    var originalAccounts: MutableList<Account> = mutableListOf()

    val pageable = Pageable().apply {
        size = 20
    }

    fun getSourceAccounts(
        isInitialLoading: Boolean,
        channelId: String,
        permissionId: String,
        accountsSelected: MutableList<Account>?
    ) {
        getAccountsPaginated?.dispose()
        getAccountsPaginated =
            accountGateway.getAccountsPaginated(
                channelId,
                permissionId,
                pageable = pageable
            )
                .doOnSuccess {
                    pageable.apply {
                        totalPageCount = it.totalPages
                        isLastPage = !it.hasNextPage
                        increasePage()
                    }
                    if (isInitialLoading) {
                        if (pageable.filter == null) {
                            if (it.results.size < 5) {
                                this.originalAccounts.addAll(it.results)
                            }
                            totalElements.onNext(it.totalElements)
                        }
                        if (accountsSelected != null) {
                            isFromEdit.onNext(true)
                            allAccountsSelected = false
                            excludedAccountIds = mutableListOf()
                            selectedAccounts = accountsSelected
                            totalSelected = accountsSelected.size
                        }
                    }
                    if (selectedAccounts.isNotEmpty()) {
                        selectedAccounts.forEach { currentAccount ->
                            it.results
                                .filter { it.id == currentAccount.id }
                                .forEach { responseAccount ->
                                    responseAccount.isSelected = true
                                }
                        }
                    } else {
                        if (totalSelected != 0) {
                            it.results.forEach {
                                it.isSelected = true
                            }
                        }
                        if (excludedAccountIds.isNotEmpty()) {
                            it.results
                                .filter { excludedAccountIds.contains(it.id) }
                                .forEach {
                                    it.isSelected = false
                                }
                        }
                    }
                    setPermissionEachAccount(it.results)
                }
                .map { Pair(pageable.combineList(accounts.value, it.results), it.results) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    if (!isInitialLoading) {
                        pageable.isLoadingPagination = true
                    }
                    _accountState.value = ShowSourceAccountLoading
                }
                .doFinally {
                    if (!isInitialLoading) {
                        pageable.isLoadingPagination = false
                    }
                    _accountState.value = ShowSourceAccountDismissLoading
                }
                .subscribe(
                    {
                        _accounts.value = it.first
                    }, {
                        Timber.e(it, "getSourceAccounts Failed")
                        _accountState.value = ShowSourceAccountError(it)
                    }
                )
        getAccountsPaginated?.addTo(disposables)
    }

    private fun setPermissionEachAccount(
        accounts: MutableList<Account>
    ) {
        settingsGateway.getPermissionCollection()
            .subscribe(
                {
                    accounts.forEach { account ->
                        val permissionCollection = mapPermission(account.id, it)
                        account.isLoading = false
                        account.isError = false
                        account.permissionCollection = permissionCollection
                        account.isViewableBalance = permissionCollection.hasAllowToBTRViewBalance
                    }
                }, {
                    Timber.e(it, "getPermissionCollection")
                }
            ).addTo(disposables)
    }

    private fun mapPermission(
        accountId: Int?,
        roleAccountPermissions: MutableList<RoleAccountPermissions>
    ): PermissionCollection {
        val permissionCollection = PermissionCollection()
        roleAccountPermissions
            .filter { it.accountId == accountId }
            .forEach {
                it.productPermissions
                    ?.forEach { productPermission ->
                        productPermission.permissions.forEach { permission ->
                            when (permission.code) {
                                Constant.Permissions.CODE_FT_CREATETRANSACTIONS_UNIONBANK
                                -> permissionCollection.hasAllowToCreateTransactionUBP =
                                    true
                                Constant.Permissions.CODE_FT_CREATETRANSACTIONS_PESONET
                                -> permissionCollection.hasAllowToCreateTransactionPesonet =
                                    true
                                Constant.Permissions.CODE_FT_CREATETRANSACTIONS_PDDTS
                                -> permissionCollection.hasAllowToCreateTransactionPDDTS =
                                    true
                                Constant.Permissions.CODE_FT_CREATETRANSACTIONS_SWIFT
                                -> permissionCollection.hasAllowToCreateTransactionSwift =
                                    true
                                Constant.Permissions.CODE_FT_CREATETRANSACTIONS_INSTAPAY
                                -> permissionCollection.hasAllowToCreateTransactionInstapay =
                                    true
                                Constant.Permissions.CODE_FT_CREATETRANSACTIONS_EON
                                -> permissionCollection.hasAllowToCreateTransactionEon =
                                    true
                                Constant.Permissions.CODE_FT_CREATETRANSACTIONS_OWN
                                -> permissionCollection.hasAllowToCreateTransactionOwn =
                                    true
                                Constant.Permissions.CODE_FT_CREATETRANSACTIONS
                                -> permissionCollection.hasAllowToCreateTransaction = true
                                Constant.Permissions.CODE_FT_CREATETRANSACTIONS_ADHOC
                                -> permissionCollection.hasAllowToCreateTransactionAdhoc =
                                    true
                                Constant.Permissions.CODE_FT_CREATETRANSACTIONS_BENEFICIARYMASTER
                                -> permissionCollection.hasAllowToCreateTransactionBeneficiaryMaster =
                                    true
                                Constant.Permissions.CODE_FT_VIEWTRANSACTIONS_VIEWDETAILS
                                -> permissionCollection.hasAllowToViewTransactionViewDetails =
                                    true
                                Constant.Permissions.CODE_FT_VIEWTRANSACTIONS
                                -> permissionCollection.hasAllowToViewTransaction = true
                                Constant.Permissions.CODE_FT_SCHEDULEDTRANSACTIONS
                                -> permissionCollection.hasAllowToCreateTransactionScheduled =
                                    true
                                Constant.Permissions.CODE_FT_DELETESCHEDULED
                                -> permissionCollection.hasAllowToViewTransactionDeleteScheduled =
                                    true
                                Constant.Permissions.CODE_FT_CHANNELS
                                -> permissionCollection.hasAllowToViewChannel = true
                                Constant.Permissions.CODE_BM_CREATEBENEFICIARYMASTER
                                -> permissionCollection.hasAllowToCreateBeneficiaryMaster =
                                    true
                                Constant.Permissions.CODE_BM_VIEWBENEFICIARYMASTER
                                -> permissionCollection.hasAllowToViewBeneficiaryMaster =
                                    true
                                Constant.Permissions.CODE_BTR_VIEWTRANSACTIONS
                                -> permissionCollection.hasAllowToBTRViewTransaction = true
                                Constant.Permissions.CODE_BTR_VIEWBALANCES
                                -> permissionCollection.hasAllowToBTRViewBalance = true
                                Constant.Permissions.CODE_BP_CREATEBILLSPAYMENT_ADHOC
                                -> permissionCollection.hasAllowToCreateBillsPaymentAdhoc =
                                    true
                                Constant.Permissions.CODE_BP_CREATEBILLSPAYMENT
                                -> permissionCollection.hasAllowToCreateBillsPayment = true
                                Constant.Permissions.CODE_BP_SCHEDULEDPAYMENTS
                                -> permissionCollection.hasAllowToScheduledBillsPayment =
                                    true
                                Constant.Permissions.CODE_BP_DELETESCHEDULEDPAYMENTS
                                -> permissionCollection.hasAllowToDeleteScheduledBillsPayment =
                                    true
                                Constant.Permissions.CODE_BP_VIEWBPHISTORY
                                -> permissionCollection.hasAllowToViewBillsPaymentHistory =
                                    true
                                Constant.Permissions.CODE_BP_CREATEBILLSPAYMENT_FREQUENT
                                -> permissionCollection.hasAllowToCreateBillsPaymentFrequent =
                                    true
                                Constant.Permissions.CODE_BP_VIEWBPHISTORY_DETAILS
                                -> permissionCollection.hasAllowToViewBillsPaymentHistoryDetails =
                                    true
                                Constant.Permissions.CODE_BP_CREATEFREQUENTBILLER
                                -> permissionCollection.hasAllowToCreateFrequentBiller =
                                    true
                                Constant.Permissions.CODE_BP_DELETEFREQUENTBILLER
                                -> permissionCollection.hasAllowToDeleteFrequentBiller =
                                    true
                            }
                        }
                    }
            }
        return permissionCollection
    }

    fun onSelectAccount(position: Int, isSelected: Boolean) {
        val account = getAccounts()[position]
        account.isSelected = !isSelected
        val countSelectedAccounts: Int
        if (isSelectAll.value.notNullable() && allAccountsSelected) {
            selectedAccounts = mutableListOf()
            val totalUnSelected = getAccounts().filter { !it.isSelected }.size
            countSelectedAccounts = totalElements.value.notNullable() - totalUnSelected
            if (isSelected) {
                excludedAccountIds.add(account.id.notNullable())
            } else {
                excludedAccountIds.remove(account.id.notNullable())
            }
        } else {
            val totalSelected = if (isSelected) {
                totalSelected.minus(1)
            } else {
                totalSelected.plus(1)
            }
            countSelectedAccounts = totalSelected
            excludedAccountIds = mutableListOf()
            if (!isSelected) {
                selectedAccounts.add(account)
            } else {
                selectedAccounts.removeAll { it.id == account.id }
            }
        }
        totalSelected = countSelectedAccounts
        val hasSelectedAtleastOne = totalSelected > 0
        val hasSelectedAccounts = isSelectAll.value.notNullable() || hasSelectedAtleastOne
        val isAllSelected = if (isSelectAll.value.notNullable()) {
            totalElements.value.notNullable() == totalSelected
        } else {
            totalElements.value == getAccounts().filter { it.isSelected }.size
        }
        _accountState.value = OnUpdateSelectionOfAccount(
            isAllSelected,
            hasSelectedAccounts,
            countSelectedAccounts
        )
    }

    fun setBeneficiaryAccounts(accountsSelected: MutableList<Account>?) {
        accountsSelected?.forEach {
            it.isViewableCheckBox = false
        }
        _accounts.value = accountsSelected
    }

    fun selectAllAccounts(selected: Boolean) {
        getAccounts().forEach {
            it.isSelected = selected
        }
        isSelectAll.onNext(selected)
        if (selected) {
            if (totalElements.value.notNullable() < 5) {
                allAccountsSelected = false
                selectedAccounts = originalAccounts
            } else {
                allAccountsSelected = true
                selectedAccounts = mutableListOf()
            }
            excludedAccountIds = mutableListOf()
            totalSelected = totalElements.value.notNullable()
        } else {
            allAccountsSelected = false
            selectedAccounts = mutableListOf()
            excludedAccountIds = mutableListOf()
            totalSelected = 0
        }
        val isAllSelected = if (isSelectAll.value.notNullable()) {
            totalElements.value.notNullable() == totalSelected
        } else {
            totalElements.value == getAccounts().filter { it.isSelected }.size
        }
        _accountState.value = OnUpdateSelectionOfAccount(
            isAllSelected,
            getAccounts().any { it.isSelected },
            totalElements.value.notNullable()
        )
    }

    fun onChooseSelectedAccounts() {
        val sourceAccountForm = SourceAccountForm()
        sourceAccountForm.allAccountsSelected = allAccountsSelected
        sourceAccountForm.totalSelected = totalSelected
        sourceAccountForm.excludedAccountIds = excludedAccountIds
        sourceAccountForm.selectedAccounts = selectedAccounts
        _accountState.value =
            OnClickSelectAccounts(JsonHelper.toJson(sourceAccountForm))
    }

    fun onSetSourceAccountForm(sourceAccountForm: SourceAccountForm) {
        this.excludedAccountIds = sourceAccountForm.excludedAccountIds
        this.selectedAccounts = sourceAccountForm.selectedAccounts
        if (sourceAccountForm.totalSelected == 0) {
            this.allAccountsSelected = false
        } else {
            this.allAccountsSelected = sourceAccountForm.allAccountsSelected
            this.totalSelected = sourceAccountForm.totalSelected
        }
        this.isSelectAll.onNext(allAccountsSelected)
    }

    fun isSelectedAllAccounts() = totalElements.value.notNullable() == totalSelected

    fun getAccounts(): MutableList<Account> {
        return accounts.value.notNullable()
    }

}

sealed class SourceAccountState

object ShowSourceAccountLoading : SourceAccountState()

object ShowSourceAccountDismissLoading : SourceAccountState()

data class OnClickSelectAccounts(val sourceAccounts: String) : SourceAccountState()

data class OnUpdateSelectionOfAccount(
    val isAllSelected: Boolean,
    val hasSelectedAccounts: Boolean,
    val countSelectedAccounts: Int
) : SourceAccountState()

data class ShowSourceAccountError(val throwable: Throwable) : SourceAccountState()
