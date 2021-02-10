package com.unionbankph.corporate.mcd.presentation.filter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.getDisposableSingleObserver
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.extension.nullable
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.data.model.StateData
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.corporate.domain.interactor.GetTransactionStatuses
import com.unionbankph.corporate.mcd.domain.model.CheckDepositFilter
import com.unionbankph.corporate.settings.data.constant.PermissionNameEnum
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import com.unionbankph.corporate.settings.presentation.form.Selector
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import javax.inject.Inject

class CheckDepositFilterViewModel @Inject constructor(
    private val getTransactionStatuses: GetTransactionStatuses,
    private val settingsGateway: SettingsGateway
) : BaseViewModel() {

    private val _applyFilter = MutableLiveData<Event<String>>()

    private val _statuses = MutableLiveData<Event<MutableList<Selector>>>()

    val applyFilter: LiveData<Event<String>> get() = _applyFilter

    val statuses: LiveData<Event<MutableList<Selector>>> get() = _statuses

    private val _clickedDepositAccount = MutableLiveData<Event<String?>>()

    val clickedDepositAccount: LiveData<Event<String?>> get() = _clickedDepositAccount

    var input: Input = Input()

    val permissionId = BehaviorSubject.create<String>()

    inner class Input {
        var isValidForm = BehaviorSubject.createDefault(true)
        var checkNumber = BehaviorSubject.create<String>()
        var amount = BehaviorSubject.create<String>()
        var checkStartDate = BehaviorSubject.create<String>()
        var checkEndDate = BehaviorSubject.create<String>()
        var hasDepositAccount = BehaviorSubject.createDefault(false)
        var depositAccount = BehaviorSubject.create<Account>()
        var status = BehaviorSubject.create<String>()
        var statusToDisplay = BehaviorSubject.create<String>()
        val statusesState = BehaviorSubject.create<MutableList<StateData<Selector>>>()
        var startDateCreated = BehaviorSubject.create<String>()
        var endDateCreated = BehaviorSubject.create<String>()
    }

    init {
        getAccountMCDPermission()
    }

    private fun getAccountMCDPermission() {
        settingsGateway.hasPermissionChannel(
            PermissionNameEnum.CHECK_DEPOSIT.value,
            Constant.Permissions.CODE_RCD_MOBILE_CHECK
        )
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    if (it.id != null) {
                        permissionId.onNext(it.id.toString())
                    }
                }, {
                    Timber.e(it, "getAccountMCDPermission failed")
                }
            ).addTo(disposables)
    }

    fun loadInputs(inputs: String) {
        Single.fromCallable { inputs }
            .map {
                return@map JsonHelper.fromJson<CheckDepositFilter>(it)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    setupInputs(it)
                }, {
                    Timber.e(it, "loadInputs")
                }
            ).addTo(disposables)
    }

    fun onSelectedDepositAccount(account: Account) {
        input.hasDepositAccount.onNext(true)
        input.depositAccount.onNext(account)
    }

    private fun setupInputs(it: CheckDepositFilter) {
        it.checkNumber?.also { value -> input.checkNumber.onNext(value) }
        it.amount?.also { value -> input.amount.onNext(value) }
        it.checkStartDate?.also { value -> input.checkStartDate.onNext(value) }
        it.checkEndDate?.also { value -> input.checkEndDate.onNext(value) }
        it.depositAccount?.also { value ->
            input.hasDepositAccount.onNext(true)
            input.depositAccount.onNext(value)
        }
        it.status?.also { value -> input.status.onNext(value) }
        it.statusToDisplay?.also { value -> input.statusToDisplay.onNext(value) }
        it.statusesState?.also { value -> input.statusesState.onNext(value) }
        it.startDateCreated?.also { value -> input.startDateCreated.onNext(value) }
        it.endDateCreated?.also { value -> input.endDateCreated.onNext(value) }
    }

    fun setFreeTextFields(
        checkNumber: String?,
        amount: String?
    ) {
        checkNumber?.also { input.checkNumber.onNext(it) }
        amount?.also { input.amount.onNext(it) }
    }

    fun onClickedApplyFilter() {
        val checkDepositFilter = CheckDepositFilter().apply {
            checkNumber = input.checkNumber.value.nullable()
            amount = input.amount.value.nullable()
            checkStartDate = input.checkStartDate.value.nullable()
            checkEndDate = input.checkEndDate.value.nullable()
            depositAccount = if (input.hasDepositAccount.value.notNullable()) {
                input.depositAccount.value
            } else {
                null
            }
            status = input.status.value.nullable()
            statusToDisplay = input.statusToDisplay.value.nullable()
            statusesState = input.statusesState.value
            startDateCreated = input.startDateCreated.value.nullable()
            endDateCreated = input.endDateCreated.value.nullable()
            filterCount = getFilterCount()
        }
        _applyFilter.value = Event(JsonHelper.toJson(checkDepositFilter))
    }

    fun onClickedClearFilter() {
        input.checkNumber.onNext("")
        input.amount.onNext("")
        input.checkStartDate.onNext("")
        input.checkEndDate.onNext("")
        input.hasDepositAccount.onNext(false)
        input.depositAccount.onNext(Account())
        input.status.onNext("")
        input.statusToDisplay.onNext("")
        input.startDateCreated.onNext("")
        input.endDateCreated.onNext("")
    }

    fun onClickedStatuses() {
        if (statuses.value?.peekContent().notNullable().isNotEmpty()) {
            _statuses.value = Event(statuses.value?.peekContent().notNullable())
        } else {
            getTransactionStatuses.execute(
                getDisposableSingleObserver(
                    {
                        _statuses.value = Event(it)
                    }, {
                        Timber.e(it, "onClickedStatuses")
                        _uiState.value = Event(UiState.Error(it))
                    }
                ),
                doOnSubscribeEvent = {
                    _uiState.value = Event(UiState.Loading)
                },
                doFinallyEvent = {
                    _uiState.value = Event(UiState.Complete)
                },
                params = "10"
            ).addTo(disposables)
        }
    }

    private fun getFilterCount(): Long {
        var count = 0L
        input.checkNumber.value?.let {
            if (it.isNotEmpty()) {
                count++
            }
        }
        input.amount.value?.let {
            if (it.isNotEmpty()) {
                count++
            }
        }
        if (input.checkStartDate.value.notNullable().isNotEmpty() ||
            input.checkEndDate.value.notNullable().isNotEmpty()
        ) {
            count++
        }
        input.hasDepositAccount.value?.let {
            if (it) {
                count++
            }
        }
        input.status.value?.let {
            if (it.isNotEmpty()) {
                count++
            }
        }
        if (input.startDateCreated.value.notNullable().isNotEmpty() ||
            input.endDateCreated.value.notNullable().isNotEmpty()
        ) {
            count++
        }
        return count
    }

    fun onClickedDepositAccount() {
        _clickedDepositAccount.value = Event(input.depositAccount.value?.id.toString())
    }

}

