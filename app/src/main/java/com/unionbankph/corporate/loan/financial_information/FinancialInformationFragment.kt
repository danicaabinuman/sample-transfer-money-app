package com.unionbankph.corporate.loan.financial_information

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.navigation.fragment.findNavController
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account_setup.presentation.AccountSetupActivity
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.observe
import com.unionbankph.corporate.databinding.FragmentFinancialInformationBinding
import com.unionbankph.corporate.loan.LoanActivity


class FinancialInformationFragment :
    BaseFragment<FragmentFinancialInformationBinding, FinancialInformationViewModel>(),
    FinancialInformationHandler {

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

        binding.apply {

            //TODO - CLEANUP CODE (END DRAWABLE ISSUE NOT ROTATING WHEN CLICK)
            financialInfoActPob.setOnDismissListener {
                financialInfoTilPob.setEndIconDrawable(R.drawable.ic_vector_dropdown_down)
            }
            financialInfoActPob.setOnClickListener { v ->
                if (financialInfoActPob.isPopupShowing) {
                    financialInfoTilPob.setEndIconDrawable(R.drawable.ic_vector_dropdown_up)
                } else {
                    financialInfoTilPob.setEndIconDrawable(R.drawable.ic_vector_dropdown_down)
                }
            }
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
        if (requireActivity() is AccountSetupActivity) {
            findNavController().navigate(R.id.action_to_account_purpose)
        } else {
            findNavController().navigate(R.id.nav_to_addressFragment)
        }
    }
}