package com.unionbankph.corporate.request_payment_link.presentation.setup_payment_link.nominate_settlement_account

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
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class NominateSettlementViewModel @Inject constructor(
        private val accountGateway: AccountGateway,
        private val settingsGateway: SettingsGateway,
        private val dataBus: DataBus
) : BaseViewModel()  {
    private val _accountState = MutableLiveData<AccountState>()

    private val _accounts = MutableLiveData<MutableList<Account>>()

    val state: LiveData<AccountState> get() = _accountState

    val accounts: LiveData<MutableList<Account>> = _accounts

    private var getAccountsPaginated: Disposable? = null

    val pageable = Pageable()

    fun getCorporateUserOrganization(isInitialLoading: Boolean) {
        getAccountsPaginated?.dispose()
        getAccountsPaginated = accountGateway.getAccountsPaginated(pageable = pageable)
                .doOnSuccess {
                    pageable.apply {
                        totalPageCount = it.totalPages
                        isLastPage = !it.hasNextPage
                        increasePage()
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
                    _accountState.value = ShowAccountLoading
                }
                .doFinally {
                    if (!isInitialLoading) {
                        pageable.isLoadingPagination = false
                    }
                    _accountState.value = ShowAccountDismissLoading
                }
                .subscribe(
                        {
                            _accounts.value = it.first
                            getAccountsBalances(it.second)
                        }, {
                    Timber.e(it, "getCorporateUserOrganization Failed")
                    if (!isInitialLoading) {
                        pageable.errorPagination(it.message)
                    } else {
                        _accountState.value = ShowAccountError(it)
                    }
                })
                .addTo(disposables)
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

    fun getCorporateUserAccountDetail(id: String, position: Int) {
        accountGateway.getCorporateUserAccountDetail(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { setErrorStatePerItem(true, position) }
                .doOnSubscribe { showAccountDetailLoading(position) }
                .subscribe(
                        {
                            dataBus.accountDataBus.emmit(it)
                        }, {
                    Timber.e(it, "getCorporateUserAccountDetail Failed")
                    _accountState.value = ShowAccountDetailError(id)
                })
                .addTo(disposables)
    }

    fun getAccountsBalances(accounts: MutableList<Account>) {
        val accountIds = accounts.map { it.id.notNullable() }.toMutableList()
        val getAccountsBalances = GetAccountsBalances(accountIds)
        accountGateway.getAccountsBalances(getAccountsBalances)
                .doOnSuccess { updateBalances(it, accounts) }
                .doOnError { setErrorStateAllAccounts(accounts) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { showAccountsDetailLoading(accounts) }
                .subscribe(
                        {
                            _accounts.value = getAccounts()
                        }, {
                    Timber.e(it, "getAccountsBalances Failed")
                    _accountState.value = ShowAccountsDetailError
                })
                .addTo(disposables)
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


    fun showAccountsDetailLoading(accounts: MutableList<Account>) {
        accounts.forEach {
            it.isError = false
            it.isLoading = true
        }
        _accountState.value = ShowAccountLoadingAccountDetail
    }

    fun showAccountDetailLoading(position: Int) {
        val currentAccounts = getAccounts()
        currentAccounts[position].isError = false
        currentAccounts[position].isLoading = true
        _accountState.value = ShowAccountLoadingAccountDetail
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
                            ?.filter { it.name == "Balance and Transaction Reporting" }
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

    private fun setErrorStateAllAccounts(accounts: MutableList<Account>) {
        accounts.forEach {
            it.isError = true
            it.isLoading = false
        }
    }

    private fun setErrorStatePerItem(
            isError: Boolean,
            position: Int
    ) {
        val currentAccounts = getAccounts()
        currentAccounts[position].isError = isError
        currentAccounts[position].isLoading = false
    }

    fun getAccounts(): MutableList<Account> {
        return accounts.value.notNullable()
    }

    fun updateEachAccount(
            account: Account,
            accounts: MutableList<Account>
    ) {
        Flowable.just(accounts)
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

sealed class AccountState

object ShowAccountLoading : AccountState()

object ShowAccountDismissLoading : AccountState()

object ShowAccountLoadingAccountDetail : AccountState()

object ShowAccountDismissLoadingAccountDetail : AccountState()

data class ShowAccountDetailError(val id: String) : AccountState()

object ShowAccountsDetailError : AccountState()

data class ShowAccountError(val throwable: Throwable) : AccountState()