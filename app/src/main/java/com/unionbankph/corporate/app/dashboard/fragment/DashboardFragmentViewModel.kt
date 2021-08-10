package com.unionbankph.corporate.app.dashboard.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.account.presentation.account_list.AccountState
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.common.data.form.Pageable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class DashboardFragmentViewModel @Inject constructor() : BaseViewModel() {

    private val _dashboardViewState = MutableLiveData<DashboardViewState>()
    val dashboardViewState = _dashboardViewState

    val pageable = Pageable()

    init {
        _dashboardViewState.value = DashboardViewState(
            name = "Hello",
            actionList = mutableListOf()
        )
    }

    fun setActionItems(dashboardActionItems: MutableList<ActionItem>) {

        _dashboardViewState.value = _dashboardViewState.value?.also {
            it.name = "Set List ${dashboardActionItems.size}"
            it.actionList = dashboardActionItems
        }
    }

    fun setName() {
        Observable.just("HEHE")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe ({ value ->
                _dashboardViewState.value = _dashboardViewState.value?.also {
                    it.name = value
                }
            }) {
                Timber.e(it)
            }.addTo(disposables)
    }
}