package com.unionbankph.corporate.loan.applyloan

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.dashboard.fragment.ActionItem
import com.unionbankph.corporate.common.data.form.Pageable
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class LoansViewModel @Inject constructor(
    private val context: Context
) : BaseViewModel() {

    private val _commonQuestions = MutableLiveData<List<CommonQuestions>>()
    val commonQuestions: LiveData<List<CommonQuestions>> = _commonQuestions

    private val _loansViewState = MutableLiveData<LoansViewState>()
    val loansViewState: LiveData<LoansViewState> = _loansViewState

    private var getAccountsPaginated: Disposable? = null

    val pageable = Pageable()
    private var initialLoansActionList = mutableListOf<ActionItem>()

    init {

        initViewState()
        generateCommonQuestions()
    }

    fun generateCommonQuestions() {
        _loansViewState.value?.commonQuestions = CommonQuestions.generateCommonQuestions(context)
    }

    fun testData() {
        _loansViewState.value = _loansViewState.value
    }

    fun initViewState() {
        _loansViewState.value = LoansViewState(
            isScreenRefreshed = true,
            hasInitialFetchError = false,
            commonQuestions = listOf()
        )
    }

    fun setCommonQuestions(item: CommonQuestions?) {
        item?.let {
            _loansViewState.value?.commonQuestions?.get(item.id)?.expand =
                !item.expand //get(item.id)?.expand = !item.expand
        }
    }


}
