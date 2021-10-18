package com.unionbankph.corporate.loan.businesstype

import android.view.LayoutInflater
import android.view.ViewGroup
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.databinding.FragmentBusinessTypeBinding


class BusinessTypeFragment: BaseFragment<FragmentBusinessTypeBinding, BusinessTypeViewModel>(),
    BusinessTypeHandler
{

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

}

