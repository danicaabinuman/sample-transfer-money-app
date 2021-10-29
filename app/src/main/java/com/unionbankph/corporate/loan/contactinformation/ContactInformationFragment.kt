package com.unionbankph.corporate.loan.contactinformation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.databinding.FragmentContactInformationBinding
import com.unionbankph.corporate.itemContactInformation
import com.unionbankph.corporate.loan.LoanActivity

class ContactInformationFragment: BaseFragment<FragmentContactInformationBinding, ContactInformationViewModel>(),
    ContactInformationHandler
{

    private val activity by lazyFast { getAppCompatActivity() as LoanActivity }

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

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)

        activity.setToolbarTitle(
            activity.binding.tvToolbar,
            ""
        )
    }

    private fun initObservers() {
        viewModel.apply {
            contactInformation.observe(viewLifecycleOwner, ::handleContactInformation)
        }

        binding.lifecycleOwner = this
        binding.handler = this
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

    override fun onNext() {
        findNavController().navigate(R.id.nav_to_personalInformationFragment)
    }

}

