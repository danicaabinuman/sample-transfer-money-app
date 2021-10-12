package com.unionbankph.corporate.loan.calculator

import android.view.LayoutInflater
import android.view.ViewGroup
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.databinding.FragmentLoansCalculatorBinding

class LoansCalculatorFragment :
    BaseFragment<FragmentLoansCalculatorBinding, LoansCalculatorViewModel>() {

    private val controller by lazyFast {
        LoansCalculatorController(applicationContext)
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentLoansCalculatorBinding
        get() = FragmentLoansCalculatorBinding::inflate

    override val viewModelClassType: Class<LoansCalculatorViewModel>
        get() = LoansCalculatorViewModel::class.java

    override fun onResume() {
        super.onResume()
        initViews()
        initObservers()
    }

    private fun initViews() {
        binding.apply {
            loansCalculatorErvCalculator.setController(controller)
            controller.setData("test")
        }
    }

    private fun initObservers() {
        viewModel.apply {

        }
        binding.lifecycleOwner = this
    }

}