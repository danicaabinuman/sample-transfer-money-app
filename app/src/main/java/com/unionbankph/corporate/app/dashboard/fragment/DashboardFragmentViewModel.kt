package com.unionbankph.corporate.app.dashboard.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.account.data.gateway.AccountGateway
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.account.domain.form.GetAccountsBalances
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.platform.bus.data.DataBus
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.sme.GenericMenuItem
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.helper.ConstantHelper
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.corporate.domain.gateway.CorporateGateway
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class DashboardFragmentViewModel
@Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val accountGateway: AccountGateway,
    private val settingsGateway: SettingsGateway,
    private val corporateGateway: CorporateGateway,
    private val dataBus: DataBus
) : BaseViewModel() {

    private val _dashboardViewState = MutableLiveData<DashboardViewState>()
    val dashboardViewState: LiveData<DashboardViewState> get() = _dashboardViewState

    private var getAccountsPaginated: Disposable? = null

    val pageable = Pageable()

    init {
        _dashboardViewState.value = DashboardViewState(
            name = "Hello",
            accountButtonText = "",
            isScreenRefreshed = true,
            isOnTrialMode = false,
            hasLoans = false,
            hasEarnings = false,
            hasInitialFetchError = false,
            megaMenuList = mutableListOf(),
            accounts = mutableListOf()
        )
    }

    fun setScreenIsOnTrialMode(isTrialMode: Boolean) {
        _dashboardViewState.value = _dashboardViewState.value?.also {
            it.isOnTrialMode = isTrialMode
        }
    }

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
            .map { Pair(pageable.combineList(dashboardViewState.value?.accounts, it.results), it.results) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                if (!isInitialLoading) {
                    pageable.isLoadingPagination = true
                    _dashboardViewState.postValue(_dashboardViewState.value)
                }
            }
            .doFinally {
                if (!isInitialLoading) {
                    pageable.isLoadingPagination = false
                    _dashboardViewState.postValue(_dashboardViewState.value)
                }
            }
            .subscribe(
                { pair ->
                    _dashboardViewState.value = _dashboardViewState.value?.also {
                        it.accounts = pair.first
                        it.isScreenRefreshed = false
                        it.hasInitialFetchError = false
                    }
                    getAccountsBalances(pair.second)
                }, { throwable ->
                    Timber.e(throwable, "getCorporateUserOrganization Failed")
                    if (!isInitialLoading) {
                        pageable.errorPagination(throwable.message)
                        _dashboardViewState.postValue(_dashboardViewState.value)
                    } else {
                        _dashboardViewState.value = _dashboardViewState.value?.also {
                            it.hasInitialFetchError = true
                            it.errorMessage = throwable.message
                        }
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
                        val permissionCollection = ConstantHelper.Object.getAccountPermission(
                            account.id, it
                        )
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
                    _dashboardViewState.postValue(_dashboardViewState.value)
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
                    _dashboardViewState.value = _dashboardViewState.value?.also {
                        it.accounts = getAccounts()
                    }
                }, {
                    Timber.e(it, "getAccountsBalances Failed")
                    _dashboardViewState.postValue(_dashboardViewState.value)
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
        _dashboardViewState.postValue(_dashboardViewState.value)
    }

    fun showAccountDetailLoading(position: Int) {
        val currentAccounts = getAccounts()
        currentAccounts[position].isError = false
        currentAccounts[position].isLoading = true
        _dashboardViewState.postValue(_dashboardViewState.value)
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
        return _dashboardViewState.value?.accounts.notNullable()
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
                { fetchedAccounts ->
                    _dashboardViewState.value = _dashboardViewState.value?.also {
                        it.accounts = fetchedAccounts
                    }
                }, {
                    Timber.e(it, "updateEachAccount")
                }
            ).addTo(disposables)
    }

    fun setDashboardName() {
        corporateGateway.getCorporateUser()
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                { corpUser ->
                    Timber.d(corpUser.toString())
                    _dashboardViewState.value = _dashboardViewState.value?.also {
                        it.name = corpUser.firstName
                    }
                }, {
                    Timber.e(it)
                }
            ).addTo(disposables)
    }

    fun setMenuItems(menuMenuItems: MutableList<GenericMenuItem>) {
        _dashboardViewState.value = _dashboardViewState.value?.also {
            it.megaMenuList = menuMenuItems
        }
    }

    fun enableMenuItem(menuId: String, isEnabled: Boolean) {
        _dashboardViewState.value = _dashboardViewState.value?.also { it ->
            it.megaMenuList.find { it.id == menuId }.also {
                it?.isVisible = true
                it?.isEnabled = isEnabled
            }
        }
    }

    fun showHideMenuItem(menuId: String, isShow: Boolean) {
        _dashboardViewState.value = _dashboardViewState.value?.also { it ->
            it.megaMenuList.find { it.id == menuId }.also {
                it?.isVisible = isShow
            }
        }
    }

    fun refreshedLoad() {
        _dashboardViewState.value = _dashboardViewState.value?.also {
            it.isScreenRefreshed = true
            it.accounts = mutableListOf()
            it.hasInitialFetchError = false
        }
        pageable.resetPagination()
    }

    fun resetLoad() {
        pageable.resetLoad()
    }

    fun getDashboardMegaMenu() {
        settingsGateway.getDashboardMegaMenu()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    it.forEachIndexed { _, item ->
                        showHideMenuItem(item.featureCode?: "", item.isEnabled ?: false)
                    }
                }, {
                    Timber.e(it, "getDashboardMegaMenu Failed")
                    _dashboardViewState.postValue(_dashboardViewState.value)
                })
            .addTo(disposables)
    }
}