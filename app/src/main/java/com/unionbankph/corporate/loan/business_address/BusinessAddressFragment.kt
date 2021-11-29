package com.unionbankph.corporate.loan.business_address

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.databinding.FragmentBusinessAddressBinding
import com.unionbankph.corporate.databinding.FragmentBusinessInformationBinding
import com.unionbankph.corporate.loan.LoanActivity

class BusinessAddressFragment: BaseFragment<FragmentBusinessAddressBinding, BusinessAddressViewModel>(),
    BusinessAddressHandler
{

    private val activity by lazyFast { getAppCompatActivity() as LoanActivity }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentBusinessAddressBinding
        get() = FragmentBusinessAddressBinding::inflate

    override val viewModelClassType: Class<BusinessAddressViewModel>
        get() = BusinessAddressViewModel::class.java

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

            activity.apply {
                showProgress(true)
                setProgressValue(5)
            }

        }
    }

    override fun onResume() {
        super.onResume()

        binding.apply {

            businessAddressActCity.setAdapter(
                ArrayAdapter(
                    activity,
                    R.layout.dropdown_menu_popup_item,
                    resources.getStringArray(R.array.dummy_data)
                )
            )
            businessAddressActProvince.setAdapter(
                ArrayAdapter(
                    activity,
                    R.layout.dropdown_menu_popup_item,
                    resources.getStringArray(R.array.dummy_data)
                )
            )
            businessAddressActRegion.setAdapter(
                ArrayAdapter(
                    activity,
                    R.layout.dropdown_menu_popup_item,
                    resources.getStringArray(R.array.dummy_data)
                )
            )
            businessAddressActEstablishment.setAdapter(
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
        //findNavController().navigate(R.id.nav_to_businessInformation)
    }


}