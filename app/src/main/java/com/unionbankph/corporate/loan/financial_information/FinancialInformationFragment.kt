package com.unionbankph.corporate.loan.financial_information

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.navigation.fragment.findNavController
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.databinding.FragmentFinancialInformationBinding
import com.unionbankph.corporate.loan.LoanActivity


class FinancialInformationFragment: BaseFragment<FragmentFinancialInformationBinding, FinancialInformationViewModel>(),
    FinancialInformationHandler
{

    private val activity by lazyFast { getAppCompatActivity() as LoanActivity }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentFinancialInformationBinding
        get() = FragmentFinancialInformationBinding::inflate

    override val viewModelClassType: Class<FinancialInformationViewModel>
        get() = FinancialInformationViewModel::class.java

    override fun onViewsBound() {
        super.onViewsBound()
        initViews()
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initObservers()
    }

    private fun initViews() {

        activity.apply {
            showProgress(true)
            setProgressValue(2)
        }
    }

    override fun onResume() {
        super.onResume()

        binding.apply {
            financialInfoActPob.setAdapter(
                ArrayAdapter(
                    activity,
                    R.layout.dropdown_menu_popup_item,
                    resources.getStringArray(R.array.dummy_data)
                )
            )
        }
    }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)

        activity.setToolbarTitle(
            activity.binding.tvToolbar,
            ""
        )
    }

    fun initObservers() {
        viewModel.apply {

        }

        binding.lifecycleOwner = this
        binding.handler = this
        binding.viewModel = viewModel
    }

    override fun onNext() {

        findNavController().navigate(R.id.nav_to_addressFragment)
    }
}