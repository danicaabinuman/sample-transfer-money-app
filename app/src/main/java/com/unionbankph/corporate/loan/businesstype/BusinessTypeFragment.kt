package com.unionbankph.corporate.loan.businesstype

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.databinding.FragmentBusinessTypeBinding
import com.unionbankph.corporate.loan.LoanActivity


class BusinessTypeFragment: BaseFragment<FragmentBusinessTypeBinding, BusinessTypeViewModel>(),
    BusinessTypeHandler
{

    private val activity by lazyFast { getAppCompatActivity() as LoanActivity }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentBusinessTypeBinding
        get() = FragmentBusinessTypeBinding::inflate

    override val viewModelClassType: Class<BusinessTypeViewModel>
        get() = BusinessTypeViewModel::class.java

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

        activity.apply {
            showProgress(false)
            setToolbarTitle(
                activity.binding.tvToolbar,
                ""
            )
        }
    }

    private fun initViews() {

        binding.apply {
        }
    }

    private fun initObservers() {
        viewModel.apply {
            businessType.observe(viewLifecycleOwner, {
            })
        }

        binding.lifecycleOwner = this
        binding.handler = this
        binding.viewModel = viewModel
    }

    override fun onBusinessType(type: Int) {
        binding.apply {
            type?.let {
            businessTypeRbIndividual.isChecked = it == 0
            businessTypeRbSole.isChecked = it == 1
            businessTypeRbPartnership.isChecked = it == 2
            businessTypeRbCorporation.isChecked = it == 3
            viewModel?.setBusinessType(type)
            }
        }
    }

    override fun onNext() {
        findNavController().navigate(R.id.nav_to_contactInformationFragment)
    }

}

