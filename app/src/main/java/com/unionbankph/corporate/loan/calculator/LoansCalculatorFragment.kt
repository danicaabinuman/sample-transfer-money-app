package com.unionbankph.corporate.loan.calculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.databinding.FragmentLoansCalculatorBinding
import com.unionbankph.corporate.loan.LoanActivity
import com.unionbankph.corporate.loan.applyloan.LoansViewState
import com.unionbankph.corporate.mcd.data.model.SectionedCheckDepositLogs
import com.unionbankph.corporate.notification.data.model.NotificationDto


class LoansCalculatorFragment :
    BaseFragment<FragmentLoansCalculatorBinding, LoansCalculatorViewModel>(),
    LoansCalculatorCallback
{

    private val activity by lazyFast { getAppCompatActivity() as LoanActivity }

    private val controller by lazyFast {
        LoansCalculatorController(applicationContext)
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentLoansCalculatorBinding
        get() = FragmentLoansCalculatorBinding::inflate

    override val viewModelClassType: Class<LoansCalculatorViewModel>
        get() = LoansCalculatorViewModel::class.java

    override fun onViewsBound() {
        super.onViewsBound()
        initViews()
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initObservers()
    }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)

        activity.setToolbarTitle(
            activity.binding.tvToolbar,
            getString(R.string.title_apply_for_a_loan)
        )
    }

    private fun initViews() {

        binding.loansCalculatorErvCalculator.setController(controller)
        controller.setLoansCallbacks(this)
    }

    private fun initObservers() {

        viewModel.apply {
            viewModel.formState.observe(viewLifecycleOwner, {
                controller.setData(it)
            })
        }
    }

    override fun onAmountChange(amount: Int) {
        viewModel.setAmount(amount)
    }

    override fun onMonthsChange(months: Int) {
        viewModel.setMonths(months)
    }

    override fun onTotalAmountPayable(totalAmountPayable: Int) {
        viewModel.setTotalAmountPayable(totalAmountPayable)
    }

    override fun onMonthlyPayment(monthlyPayment: Int) {
        viewModel.setMonthlyPayment(monthlyPayment)
    }

    override fun onPrincipal(principal: Int) {
        viewModel.setPrincipal(principal)
    }

    override fun onInterest(interest: Int) {
        viewModel.setInterest(interest)
    }

    override fun onApplyNow() {
        findNavController().navigate(R.id.nav_to_fewRemindersFragment)
    }
}