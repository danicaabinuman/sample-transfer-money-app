package com.unionbankph.corporate.loan.personal_information

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.findNavController
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.convertToCalendar
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.showDatePicker
import com.unionbankph.corporate.common.presentation.constant.DateFormatEnum
import com.unionbankph.corporate.databinding.FragmentPersonalInformationBinding
import com.unionbankph.corporate.feature.loan.PersonalInformationField
import com.unionbankph.corporate.loan.LoanActivity
import java.time.LocalDate
import java.time.Period
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
class PersonalInformationFragment : BaseFragment<FragmentPersonalInformationBinding, PersonalInformationViewModel>(),
        PersonalInformationHandler
{

    private val activity by lazyFast { getAppCompatActivity() as LoanActivity }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentPersonalInformationBinding
        get() = FragmentPersonalInformationBinding::inflate

    override val viewModelClassType: Class<PersonalInformationViewModel>
        get() = PersonalInformationViewModel::class.java


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
            setProgressValue(1)
        }

        binding.apply {
            personalInformationTieDob.setOnClickListener {
                showDatePicker(
                    minDate = Calendar.getInstance().apply { set(Calendar.YEAR, 1900) },
                    maxDate = Calendar.getInstance(),
                    calendar = viewModel?.form?.value?.dob.convertToCalendar(),
                    callback = { _, year, monthOfYear, dayOfMonth ->
                        val dateString = viewUtil.getDateFormatByCalendar(
                            Calendar.getInstance().apply {
                                set(year, monthOfYear, dayOfMonth)
                            }, DateFormatEnum.DATE_FORMAT_MONTH_NAME_DATE_YEAR.value
                        )
                        if (getAge(year, monthOfYear, dayOfMonth) >= 18) {
                            viewModel?.onDataChange(dateString, PersonalInformationField.DOB)
                        } else {
                            viewModel?.onDataChange(resources.getString(R.string.hint_date_of_birth), PersonalInformationField.DOB)
                        }
                    }
                )
            }

            //TODO - CLEANUP CODE (END DRAWABLE ISSUE NOT ROTATING WHEN CLICK)
            personalInfoActPob.setOnDismissListener {
                personalInfoTilPob.setEndIconDrawable(R.drawable.ic_vector_dropdown_down)
            }
            personalInfoActPob.setOnClickListener { v ->
                if (personalInfoActPob.isPopupShowing) {
                    personalInfoTilPob.setEndIconDrawable(R.drawable.ic_vector_dropdown_up)
                } else {
                    personalInfoTilPob.setEndIconDrawable(R.drawable.ic_vector_dropdown_down)
                }
            }
            personalInfoActCivil.setOnDismissListener {
                personalInfoTilCivil.setEndIconDrawable(R.drawable.ic_vector_dropdown_down)
            }
            personalInfoActCivil.setOnClickListener { v ->
                if (personalInfoActCivil.isPopupShowing) {
                    personalInfoTilCivil.setEndIconDrawable(R.drawable.ic_vector_dropdown_up)
                } else {
                    personalInfoTilCivil.setEndIconDrawable(R.drawable.ic_vector_dropdown_down)
                }
            }
        }
    }

    fun getAge(year: Int, month: Int, dayOfMonth: Int): Int {
        return Period.between(
            LocalDate.of(year, month, dayOfMonth),
            LocalDate.now()
        ).years
    }

    override fun onResume() {
        super.onResume()

        binding.apply {
            personalInfoActPob.setAdapter(
                ArrayAdapter(
                    activity,
                    R.layout.dropdown_menu_popup_item,
                    resources.getStringArray(R.array.dummy_place_of_birth)
                )
            )
            personalInfoActCivil.setAdapter(
                ArrayAdapter(
                    activity,
                    R.layout.dropdown_menu_popup_item,
                    resources.getStringArray(R.array.dummy_civil_status)
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
        findNavController().navigate(R.id.nav_to_financialInformationFragment)
    }

}

