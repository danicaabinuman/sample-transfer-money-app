package com.unionbankph.corporate.loan.applyloan

import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.dashboard.fragment.ActionItem
import com.unionbankph.corporate.app.dashboard.fragment.DashboardViewState
import com.unionbankph.corporate.common.data.form.Pageable
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class LoansViewModel @Inject constructor(
) : BaseViewModel() {

    private val _loansViewState = MutableLiveData<LoansViewState>()
    val loansViewState = _loansViewState

    private var getAccountsPaginated: Disposable? = null

    val pageable = Pageable()
    private var initialLoansActionList = mutableListOf<ActionItem>()

    init {
        _loansViewState.value = LoansViewState(
            isScreenRefreshed = true,
            hasInitialFetchError = false,
        )
    }



}
