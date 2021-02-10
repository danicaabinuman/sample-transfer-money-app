package com.unionbankph.corporate.mcd.presentation.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.mcd.data.gateway.CheckDepositGateway
import com.unionbankph.corporate.mcd.data.model.CheckDeposit
import com.unionbankph.corporate.mcd.domain.model.CheckDepositFilter
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import javax.inject.Inject

class CheckDepositViewModel @Inject constructor(
    val checkDepositGateway: CheckDepositGateway
) : BaseViewModel() {

    private var getCheckDepositsDisposable: Disposable? = null

    private val _state = MutableLiveData<CheckDepositState>()

    private val _checkDeposits = MutableLiveData<MutableList<CheckDeposit>>()

    private val _testCheckDeposits = MutableLiveData<MutableList<CheckDeposit>>()

    private val _clickFilter = MutableLiveData<Event<String>>()

    val state: LiveData<CheckDepositState> get() = _state

    val clickFilter: LiveData<Event<String>> get() = _clickFilter

    val checkDeposits: LiveData<MutableList<CheckDeposit>> = _checkDeposits

    val testCheckDeposits: LiveData<MutableList<CheckDeposit>> = _testCheckDeposits

    val checkDepositFilter = BehaviorSubject.create<CheckDepositFilter>()

    val checkDepositFilterCount = BehaviorSubject.createDefault(0L)

    val pageable = Pageable()

    fun getCheckDeposits(isInitialLoading: Boolean) {
        getCheckDepositsDisposable?.dispose()
        getCheckDepositsDisposable = checkDepositGateway.getCheckDeposits(
            pageable,
            checkDepositFilter.value?.checkNumber,
            checkDepositFilter.value?.amount,
            checkDepositFilter.value?.checkStartDate,
            checkDepositFilter.value?.checkEndDate,
            checkDepositFilter.value?.depositAccount?.accountNumber,
            checkDepositFilter.value?.status,
            checkDepositFilter.value?.startDateCreated,
            checkDepositFilter.value?.endDateCreated
        )
            .doOnSuccess {
                pageable.apply {
                    totalPageCount = it.totalPages
                    isLastPage = !it.hasNextPage
                    increasePage()
                }
            }
            .map { pageable.combineList(checkDeposits.value, it.results) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                if (isInitialLoading) {
                    _state.value = ShowCheckDepositLoading
                } else {
                    pageable.isLoadingPagination = true
                    _state.value = ShowCheckDepositEndlessLoading
                }
            }
            .doFinally {
                if (isInitialLoading) {
                    _state.value = ShowCheckDepositDismissLoading
                } else {
                    pageable.isLoadingPagination = false
                    _state.value = ShowCheckDepositDismissEndlessLoading
                }
            }
            .subscribe(
                {
                    _checkDeposits.value = it
                }, {
                    Timber.e(it, "getCheckDeposits failed")
                    if (!isInitialLoading) {
                        pageable.errorPagination(it.message)
                    } else {
                        _state.value = ShowCheckDepositError(it)
                    }
                }
            )
        getCheckDepositsDisposable?.addTo(disposables)
    }

    fun getCheckDepositsTestData() {
        checkDepositGateway.getCheckDepositsTestData()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    _testCheckDeposits.value = it.results
                }, {
                    Timber.e(it, "getCheckDepositsTestData failed")
                    _state.value = ShowCheckDepositError(it)
                })
            .addTo(disposables)
    }

    fun onApplyFilter(inputs: String?) {
        Single.fromCallable { inputs }
            .map {
                return@map JsonHelper.fromJson<CheckDepositFilter>(it)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    checkDepositFilter.onNext(it)
                    checkDepositFilterCount.onNext(it.filterCount)
                }, {
                    Timber.e(it, "onApplyFilter")
                }
            ).addTo(disposables)
    }

    fun onClickedFilter() {
        _clickFilter.value = Event(JsonHelper.toJson(checkDepositFilter.value))
    }
}

sealed class CheckDepositState

object ShowCheckDepositLoading : CheckDepositState()

object ShowCheckDepositEndlessLoading : CheckDepositState()

object ShowCheckDepositDismissLoading : CheckDepositState()

object ShowCheckDepositDismissEndlessLoading : CheckDepositState()

data class ShowCheckDepositError(val throwable: Throwable) : CheckDepositState()
