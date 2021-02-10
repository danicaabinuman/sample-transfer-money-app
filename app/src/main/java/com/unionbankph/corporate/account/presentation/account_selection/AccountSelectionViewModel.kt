package com.unionbankph.corporate.account.presentation.account_selection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.account.data.gateway.AccountGateway
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.account.domain.form.GetAccountsBalances
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.platform.bus.data.DataBus
import com.unionbankph.corporate.auth.data.model.RoleAccountPermissions
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.PermissionCollection
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import javax.inject.Inject

class AccountSelectionViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val accountGateway: AccountGateway,
    private val settingsGateway: SettingsGateway,
    private val dataBus: DataBus
) : BaseViewModel() {

    private val _accountState = MutableLiveData<AccountSelectionState>()

    val state: LiveData<AccountSelectionState> get() = _accountState

    private val _accounts = MutableLiveData<MutableList<Account>>()

    val accounts: LiveData<MutableList<Account>> = _accounts

    val page = BehaviorSubject.create<String>()

    private var getAccountsPaginated: Disposable? = null

    val pageable = Pageable()

    fun getCorporateUserAccountDetail(id: String, position: Int) {
        accountGateway.getCorporateUserAccountDetail(id)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnError { setErrorStatePerItem(true, position) }
            .doOnSubscribe { showAccountDetailLoading(position) }
            .subscribe(
                {
                    dataBus.accountDataBus.emmit(it)
                }, {
                    Timber.e(it, "getCorporateUserAccountDetail Failed")
                    _accountState.value = ShowAccountSelectionDetailError(id)
                })
            .addTo(disposables)
    }

    fun showAccountDetailLoading(position: Int) {
        val currentAccounts = getAccounts()
        currentAccounts[position].isError = false
        currentAccounts[position].isLoading = true
        _accountState.value = ShowAccountSelectionLoadingAccountDetail
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

    fun getAccounts(): MutableList<Account> {
        return accounts.value.notNullable()
    }

    fun getAccountsPermission(
        isInitialLoading: Boolean,
        channelId: String? = null,
        permissionId: String? = null,
        sourceAccountId: String? = null,
        destinationId: String? = null,
        exceptCurrency: String? = null
    ) {
        getAccountsPaginated?.dispose()
        getAccountsPaginated =
            accountGateway.getAccountsPaginated(
                channelId,
                permissionId,
                exceptCurrency,
                pageable = pageable
            )
                .doOnSuccess {
                    pageable.apply {
                        totalPageCount = it.totalPages
                        isLastPage = !it.hasNextPage
                        increasePage()
                    }
                    if (sourceAccountId != null) {
                        it.results.remove(getAccounts().find { it.id.toString() == sourceAccountId })
                    }
                    val accountCurrentSelected = it.results
                        .find { it.id.toString() == destinationId }
                    if (accountCurrentSelected != null) {
                        accountCurrentSelected.isSelected = true
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
                    _accountState.value = ShowAccountSelectionLoading
                }
                .doFinally {
                    if (!isInitialLoading) {
                        pageable.isLoadingPagination = false
                    }
                    _accountState.value = ShowAccountSelectionDismissLoading
                }
                .subscribe(
                    {
                        _accounts.value = it.first
                        getAccountsBalances(it.second)
                    }, {
                        Timber.e(it, "getAccountsPermission failed")
                        if (!isInitialLoading) {
                            pageable.errorPagination(it.message)
                        } else {
                            _accountState.value = ShowAccountSelectionError(it)
                        }
                    }
                )
        getAccountsPaginated?.addTo(disposables)
    }

    private fun getAccountsBalances(accounts: MutableList<Account>) {
        val accountIds = accounts.map { it.id.notNullable() }.toMutableList()
        val getAccountsBalances = GetAccountsBalances(accountIds)
        accountGateway.getAccountsBalances(getAccountsBalances)
            .doOnSuccess { updateBalances(it, accounts) }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnError { setErrorStateAllAccounts(accounts) }
            .doOnSubscribe { showAccountsDetailLoading(accounts) }
            .subscribe(
                {
                    _accounts.value = getAccounts()
                }, {
                    Timber.e(it, "getAccountsBalances Failed")
                    _accountState.value = ShowAccountsSelectionDetailError
                })
            .addTo(disposables)
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

    private fun updateBalances(
        accounts: MutableList<Account>,
        requestedAccounts: MutableList<Account>
    ) {
        accounts.forEach { account ->
            val findAccount = getAccounts().find { it.id == account.id }
            if (findAccount != null) {
                findAccount.headers = account.headers
                findAccount.isLoading = false
                findAccount.isError = false
            }
        }
        if (accounts.size != requestedAccounts.size) {
            val requestedAccountsIds = requestedAccounts.map { it.id }.toMutableList()
            val accountsIds = accounts.map { it.id }.toMutableList()
            requestedAccountsIds.removeAll(accountsIds)
            requestedAccountsIds.forEach { id ->
                val item = getAccounts().find { it.id == id }
                item?.isError = true
                item?.isLoading = false
            }
        }
    }

    private fun setErrorStateAllAccounts(accounts: MutableList<Account>) {
        accounts.forEach {
            it.isError = true
            it.isLoading = false
        }
    }

    fun showAccountsDetailLoading(accounts: MutableList<Account>) {
        accounts.forEach {
            it.isError = false
            it.isLoading = true
        }
        _accountState.value = ShowAccountSelectionLoadingAccountDetail
    }

    private fun setErrorStatePerItem(
        isError: Boolean,
        position: Int
    ) {
        val currentAccounts = getAccounts()
        currentAccounts[position].isError = isError
        currentAccounts[position].isLoading = false
    }

    fun updateEachAccount(
        account: Account
    ) {
        Flowable.just(getAccounts())
            .flatMapIterable { it }
            .filter { it.id == account.id }
            .map {
                account.isLoading = false
                account.isError = false
                account.isSelected = it.isSelected
                account.isViewableBalance = it.isViewableBalance
                account.permissionCollection = it.permissionCollection
                account
            }.toList()
            .subscribe(
                {
                    _accounts.value = it
                }, {
                    Timber.e(it, "updateEachAccount")
                }
            ).addTo(disposables)
    }

}

sealed class AccountSelectionState

object ShowAccountSelectionLoading : AccountSelectionState()

object ShowAccountSelectionDismissLoading : AccountSelectionState()

object ShowAccountSelectionLoadingAccountDetail : AccountSelectionState()

object ShowAccountSelectionDismissLoadingAccountDetail : AccountSelectionState()

object ShowAccountsSelectionDetailError : AccountSelectionState()

data class ShowAccountSelectionDetailError(val id: String) : AccountSelectionState()

data class ShowAccountSelectionError(val throwable: Throwable) : AccountSelectionState()
