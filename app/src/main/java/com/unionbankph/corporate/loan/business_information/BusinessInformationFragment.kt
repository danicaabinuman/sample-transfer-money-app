package com.unionbankph.corporate.loan.business_information

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.navigation.fragment.findNavController
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.convertToCalendar
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.showDatePicker
import com.unionbankph.corporate.common.presentation.constant.DateFormatEnum
import com.unionbankph.corporate.databinding.FragmentBusinessInformationBinding
import com.unionbankph.corporate.feature.loan.BusinessInformationField
import com.unionbankph.corporate.feature.loan.PersonalInformationField
import com.unionbankph.corporate.loan.LoanActivity
import java.util.*

class BusinessInformationFragment: BaseFragment<FragmentBusinessInformationBinding, BusinessInformationLoanViewModel>(),
    BusinessInformationHandler
{

    private val activity by lazyFast { getAppCompatActivity() as LoanActivity }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentBusinessInformationBinding
        get() = FragmentBusinessInformationBinding::inflate

    override val viewModelClassType: Class<BusinessInformationLoanViewModel>
        get() = BusinessInformationLoanViewModel::class.java

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
                setProgressValue(4)
            }

            businessInformationTieDob.setOnClickListener {
                showDatePicker(
                    minDate = Calendar.getInstance().apply { set(Calendar.YEAR, 1900) },
                    maxDate = Calendar.getInstance(),
                    calendar = viewModel?.form?.value?.dateStartedBusiness.convertToCalendar(),
                    callback = { _, year, monthOfYear, dayOfMonth ->
                        val dateString = viewUtil.getDateFormatByCalendar(
                            Calendar.getInstance().apply {
                                set(year, monthOfYear, dayOfMonth)
                            }, DateFormatEnum.DATE_FORMAT_MONTH_NAME_DATE_YEAR.value
                        )
                        viewModel?.onDataChange(dateString, BusinessInformationField.DATE_STARTED_BUSINESS)
                    }
                )
            }

            //TODO - CLEANUP CODE (END DRAWABLE ISSUE NOT ROTATING WHEN CLICK)
            businessInfoActIndustry.setOnDismissListener {
                businessInfoTilIndustry.setEndIconDrawable(R.drawable.ic_vector_dropdown_down)
            }
            businessInfoActIndustry.setOnClickListener { v ->
                if (businessInfoActIndustry.isPopupShowing) {
                    businessInfoTilIndustry.setEndIconDrawable(R.drawable.ic_vector_dropdown_up)
                } else {
                    businessInfoTilIndustry.setEndIconDrawable(R.drawable.ic_vector_dropdown_down)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        binding.apply {

            businessInfoActIndustry.setAdapter(
                ArrayAdapter(
                    activity,
                    R.layout.dropdown_menu_popup_item,
                    resources.getStringArray(R.array.dummy_data)
                )
            )

            businessInfoActOrganization.setAdapter(
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
        findNavController().navigate(R.id.nav_to_businessAddressFragment)
    }


}