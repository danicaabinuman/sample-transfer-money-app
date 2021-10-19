package com.unionbankph.corporate.loan.contactinformation

import android.view.LayoutInflater
import android.view.ViewGroup
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.databinding.FragmentContactInformationBinding
import com.unionbankph.corporate.itemContactInformation

class ContactInformationFragment: BaseFragment<FragmentContactInformationBinding, ContactInformationViewModel>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentContactInformationBinding
        get() = FragmentContactInformationBinding::inflate

    override val viewModelClassType: Class<ContactInformationViewModel>
        get() = ContactInformationViewModel::class.java

    override fun onViewsBound() {
        super.onViewsBound()
        initViews()
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initObservers()
    }

    private fun initViews() {
    }

    private fun initObservers() {
        viewModel.apply {
            contactInformation.observe(viewLifecycleOwner, ::handleContactInformation)
        }

        binding.lifecycleOwner = this
        //binding.handler = this
        binding.viewModel = viewModel
    }

    fun handleContactInformation(mutableList: /*MutableList*/List<ContactInformation>?) {
        mutableList?.let { data ->
            binding.contactInformationErvData.withModels {
                data.forEachIndexed { index, contactInformation ->
                    itemContactInformation {
                        id(index.toString())
                        title(contactInformation.title)
                        image(contactInformation.image)
                        isSelected(contactInformation.isSelected)
                        onClickListener { model, parentView, clickedView, position ->
                            viewModel.setLoanType(index)
                        }
                    }
                }
            }
        }
    }

}

