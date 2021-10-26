package com.unionbankph.corporate.loan.calculator

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.dashboard.fragment.DashboardViewState
import javax.inject.Inject

class LoansCalculatorViewModel @Inject constructor(
) : BaseViewModel() {

    private val _formState = MutableLiveData<LoanCalculatorState>()
    val formState: LiveData<LoanCalculatorState> = _formState

    init {
        _formState.value = LoanCalculatorState(
            amount = 0f,
            interest = 0f,
            month = 0,
            monthlyPayment = 0f,
            totalAmountPayable = 0f
        )
    }

    fun setAmount(amount: Float?) {
        amount?.let {
            _formState.value?.apply {
                this.amount = it
                _formState.value = this
            }
        }
    }

    fun setMonths(months: Int?) {
        months?.let {
            _formState.value?.apply {
                this.month = it
                _formState.value = this
            }
        }
    }

    fun setTotalAmountPayable(totalAmountPayable: Float?) {
        totalAmountPayable?.let {
            _formState.value?.apply {
                this.totalAmountPayable = it
                _formState.value = this
            }
        }
    }

    fun setMonthlyPayment(monthlyPayment: Float?) {
        monthlyPayment?.let {
            _formState.value?.apply {
                this.monthlyPayment = it
                _formState.value = this
            }
        }
    }

    fun setPrincipal(principal: Float?) {
        principal?.let {
            _formState.value?.apply {
                this.principal = it
                _formState.value = this
            }
        }
    }

    fun setInterest(interest: Float?) {
        interest?.let {
            _formState.value?.apply {
                this.interest = it
                _formState.value = this
            }
        }
    }
}
