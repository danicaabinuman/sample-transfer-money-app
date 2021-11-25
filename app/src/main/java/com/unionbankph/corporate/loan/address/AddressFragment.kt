package com.unionbankph.corporate.loan.address

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.navigation.fragment.findNavController
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.databinding.FragmentAddressBinding
import com.unionbankph.corporate.loan.LoanActivity

class AddressFragment: BaseFragment<FragmentAddressBinding, AddressViewModel>(),
    AddressHandler
{

    private val activity by lazyFast { getAppCompatActivity() as LoanActivity }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentAddressBinding
        get() = FragmentAddressBinding::inflate

    override val viewModelClassType: Class<AddressViewModel>
        get() = AddressViewModel::class.java

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
                setProgressValue(3)
            }

            //TODO - CLEANUP CODE (END DRAWABLE ISSUE NOT ROTATING WHEN CLICK)
            addressInfoActCityPresent.setOnDismissListener {
                addressInfoTilCityPresent.setEndIconDrawable(R.drawable.ic_vector_dropdown_down)
            }
            addressInfoActCityPresent.setOnClickListener { v ->
                if (addressInfoActCityPresent.isPopupShowing) {
                    addressInfoTilCityPresent.setEndIconDrawable(R.drawable.ic_vector_dropdown_up)
                } else {
                    addressInfoTilCityPresent.setEndIconDrawable(R.drawable.ic_vector_dropdown_down)
                }
            }

            addressInfoActRegionPresent.setOnDismissListener {
                addressInfoTilRegionPresent.setEndIconDrawable(R.drawable.ic_vector_dropdown_down)
            }
            addressInfoActRegionPresent.setOnClickListener { v ->
                if (addressInfoActRegionPresent.isPopupShowing) {
                    addressInfoTilRegionPresent.setEndIconDrawable(R.drawable.ic_vector_dropdown_up)
                } else {
                    addressInfoTilRegionPresent.setEndIconDrawable(R.drawable.ic_vector_dropdown_down)
                }
            }

            addressInfoActCityPermanent.setOnDismissListener {
                addressInfoTilCityPermanent.setEndIconDrawable(R.drawable.ic_vector_dropdown_down)
            }
            addressInfoActCityPermanent.setOnClickListener { v ->
                if (addressInfoActCityPermanent.isPopupShowing) {
                    addressInfoTilCityPermanent.setEndIconDrawable(R.drawable.ic_vector_dropdown_up)
                } else {
                    addressInfoTilCityPermanent.setEndIconDrawable(R.drawable.ic_vector_dropdown_down)
                }
            }

            addressInfoActRegionPermanent.setOnDismissListener {
                addressInfoTilRegionPermanent.setEndIconDrawable(R.drawable.ic_vector_dropdown_down)
            }
            addressInfoActRegionPermanent.setOnClickListener { v ->
                if (addressInfoActRegionPermanent.isPopupShowing) {
                    addressInfoTilRegionPermanent.setEndIconDrawable(R.drawable.ic_vector_dropdown_up)
                } else {
                    addressInfoTilRegionPermanent.setEndIconDrawable(R.drawable.ic_vector_dropdown_down)
                }
            }

        }
    }

    override fun onResume() {
        super.onResume()
        binding.apply {

            addressInfoActCityPresent.setAdapter(
                ArrayAdapter(
                    activity,
                    R.layout.dropdown_menu_popup_item,
                    resources.getStringArray(R.array.dummy_data)
                )
            )
            addressInfoActRegionPresent.setAdapter(
                ArrayAdapter(
                    activity,
                    R.layout.dropdown_menu_popup_item,
                    resources.getStringArray(R.array.dummy_data)
                )
            )
            addressInfoActCityPermanent.setAdapter(
                ArrayAdapter(
                    activity,
                    R.layout.dropdown_menu_popup_item,
                    resources.getStringArray(R.array.dummy_data)
                )
            )
            addressInfoActRegionPermanent.setAdapter(
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
        findNavController().navigate(R.id.nav_to_businessInformationFragment)
    }


}