package com.unionbankph.corporate.dao.presentation.preferred_branch

import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.showDatePicker
import com.unionbankph.corporate.app.common.platform.bus.event.InputSyncEvent
import com.unionbankph.corporate.app.common.widget.validator.validation.RxCombineValidator
import com.unionbankph.corporate.branch.data.model.Branch
import com.unionbankph.corporate.common.presentation.constant.DateFormatEnum
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.dao.presentation.DaoActivity
import com.unionbankph.corporate.databinding.FragmentDaoPreferredBranchBinding
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import io.reactivex.rxkotlin.addTo
import java.util.*

class DaoPreferredBranchFragment :
    BaseFragment<FragmentDaoPreferredBranchBinding, DaoPreferredBranchViewModel>(),
    DaoActivity.ActionEvent {

    private val daoActivity by lazyFast { getAppCompatActivity() as DaoActivity }

    override fun onViewModelBound() {
        super.onViewModelBound()
    }

    override fun onViewsBound() {
        super.onViewsBound()
        init()
        initBinding()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initEventBus()
        initClickListener()
    }

    private fun initEventBus() {
        eventBus.inputSyncEvent.flowable.subscribe {
            when (it.eventType) {
                InputSyncEvent.ACTION_INPUT_BRANCH -> {
                    val branch = JsonHelper.fromJson<Branch>(it.payload)
                    viewModel.branchInput.onNext(branch)
                }
            }
        }.addTo(disposables)
    }

    private fun initBinding() {
        viewModel.dateInput
            .subscribe {
                binding.tieDate.setText(
                    viewUtil.getDateFormatByCalendar(
                        it,
                        DateFormatEnum.DATE_FORMAT_DATE.value
                    )
                )
            }.addTo(disposables)
        viewModel.branchInput
            .subscribe {
                binding.tiePreferredBranch.setText(it.name)
            }.addTo(disposables)
    }

    private fun init() {
        daoActivity.showToolBarDetails()
        daoActivity.showButton(true)
        daoActivity.showProgress(true)
        daoActivity.setProgressValue(7)
        daoActivity.setToolBarDesc(formatString(R.string.title_preferred_branch_of_pickup))
        daoActivity.setActionEvent(this)
        validateForm()
    }

    private fun initClickListener() {
        binding.tiePreferredBranch.setOnClickListener {
            findNavController().navigate(R.id.action_branch_activity)
        }
        binding.tieDate.setOnClickListener {
            showDatePicker(
                minDate = Calendar.getInstance(),
                callback = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                    viewModel.dateInput.onNext(
                        Calendar.getInstance().apply {
                            set(year, monthOfYear, dayOfMonth)
                        }
                    )
                }
            )
        }
    }

    private fun validateForm() {
        val preferredBranchObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tiePreferredBranch
        )
        val tieDate = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tieDate
        )
        initSetError(preferredBranchObservable)
        initSetError(tieDate)
        RxCombineValidator(
            preferredBranchObservable,
            tieDate
        )
            .asObservable()
            .distinctUntilChanged()
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .doOnNext {
                viewModel.isValidFormInput.onNext(it)
            }
            .subscribe()
            .addTo(disposables)
    }

    private fun clearFormFocus() {
        viewUtil.dismissKeyboard(getAppCompatActivity())
        binding.constraintLayout.requestFocus()
        binding.constraintLayout.isFocusableInTouchMode = true
    }

    private fun showMissingFieldDialog() {
        showMaterialDialogError(
            message = formatString(R.string.msg_error_missing_fields)
        )
    }

    override fun onClickNext() {
        findNavController().navigate(R.id.action_business_registration_papers)
//        if (viewModel.hasValidForm()) {
//            clearFormFocus()
//            findNavController().navigate(R.id.action_company_information_step_three)
//        } else {
//            showMissingFieldDialog()
//            tie_preferred_branch.refresh()
//            tie_date.refresh()
//        }
    }

    override val layoutId: Int
        get() = R.layout.fragment_dao_preferred_branch

    override val viewModelClassType: Class<DaoPreferredBranchViewModel>
        get() = DaoPreferredBranchViewModel::class.java
}
