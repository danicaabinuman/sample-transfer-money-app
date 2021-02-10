package com.unionbankph.corporate.dao.presentation.personal_info_4

import android.os.Bundle
import androidx.activity.addCallback
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.app.common.widget.edittext.DecimalDigitsInputFilter
import com.unionbankph.corporate.app.common.widget.edittext.ImeOptionEditText
import com.unionbankph.corporate.app.common.widget.edittext.PercentageInputFilter
import com.unionbankph.corporate.app.common.widget.validator.validation.RxCombineValidator
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.dao.domain.model.DaoHit
import com.unionbankph.corporate.dao.presentation.DaoActivity
import com.unionbankph.corporate.dao.presentation.result.DaoResultFragment
import com.unionbankph.corporate.settings.presentation.form.Selector
import com.unionbankph.corporate.settings.presentation.single_selector.SingleSelectorTypeEnum
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_dao_personal_information_step_4.*
import javax.annotation.concurrent.ThreadSafe

class DaoPersonalInformationStepFourFragment :
    BaseFragment<DaoPersonalInformationStepFourViewModel>(R.layout.fragment_dao_personal_information_step_4),
    DaoActivity.ActionEvent, ImeOptionEditText.OnImeOptionListener {

    private val daoActivity by lazyFast { getAppCompatActivity() as DaoActivity }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initDaoActivity()
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel = ViewModelProviders.of(
            this,
            viewModelFactory
        )[DaoPersonalInformationStepFourViewModel::class.java]
        viewModel.uiState.observe(viewLifecycleOwner, EventObserver {
            when (it) {
                is UiState.Loading -> {
                    showProgressAlertDialog(this::class.java.simpleName)
                }
                is UiState.Complete -> {
                    dismissProgressAlertDialog()
                }
                is UiState.Error -> {
                    handleOnError(it.throwable)
                }
            }
        })
        viewModel.navigateNextStep.observe(viewLifecycleOwner, EventObserver {
            daoActivity.setPersonalInformationStepFourInput(it)
            if (viewModel.isEditMode.value == true) {
                requireActivity().onBackPressed()
            } else {
                findNavController().navigate(R.id.action_jumio_verification_fragment)
            }
        })
        viewModel.navigateResult.observe(viewLifecycleOwner, EventObserver {
            navigationDaoResult(it)
        })
        arguments?.getBoolean(EXTRA_IS_EDIT, false)?.let {
            viewModel.isEditMode.onNext(it)
        }
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

    private fun initBinding() {
        viewModel.loadDaoForm(daoActivity.viewModel.defaultDaoForm())
        if (daoActivity.viewModel.hasPersonalInformationFourInput.hasValue() &&
            !viewModel.isLoadedScreen.hasValue()
        ) {
            daoActivity.viewModel.input4.let {
                viewModel.setExistingPersonalInformationStepFour(it)
            }
        }
        viewModel.input.occupationInput
            .subscribe {
                setOtherTextInputEditText(
                    it.id,
                    til_occupation_others,
                    tie_occupation_others
                )
                tie_occupation.setText(it.value)
            }.addTo(disposables)
        viewModel.input.otherOccupationInput
            .subscribe {
                tie_occupation_others.setTextNullable(it)
            }.addTo(disposables)
        viewModel.input.sourceOfFundsInput
            .subscribe {
                tie_source_of_funds.setText(it.value)
            }.addTo(disposables)
        viewModel.input.percentOwnershipInput
            .subscribe {
                tie_percent_ownership.setTextNullable(it)
            }.addTo(disposables)
        viewModel.isEditMode
            .subscribe {
                if (it) {
                    daoActivity.setButtonName(formatString(R.string.action_save))
                }
            }.addTo(disposables)
    }

    private fun init() {
        validateForm()
        initImeOption()
        tie_percent_ownership.filters = arrayOf(
            PercentageInputFilter(
                1f,
                100f
            ),
            DecimalDigitsInputFilter(3,4)
        )
    }

    private fun initImeOption() {
        val imeOptionEditText = ImeOptionEditText()
        imeOptionEditText.addEditText(
            tie_occupation_others,
            tie_percent_ownership
        )
        imeOptionEditText.setOnImeOptionListener(this)
        imeOptionEditText.startListener()
    }

    private fun initEventBus() {
        eventBus.inputSyncEvent.flowable.subscribe {
            when (it.eventType) {
                SingleSelectorTypeEnum.OCCUPATION.name -> {
                    val selector = JsonHelper.fromJson<Selector>(it.payload)
                    viewModel.input.occupationInput.onNext(selector)
                }
                SingleSelectorTypeEnum.SOURCE_OF_FUNDS.name -> {
                    val selector = JsonHelper.fromJson<Selector>(it.payload)
                    viewModel.input.sourceOfFundsInput.onNext(selector)
                }
            }
        }.addTo(disposables)
    }

    private fun initClickListener() {
        tie_occupation.setOnClickListener {
            navigateSingleSelector(
                SingleSelectorTypeEnum.OCCUPATION.name,
                hasSearch = true,
                isPaginated = true
            )
        }
        tie_source_of_funds.setOnClickListener {
            navigateSingleSelector(
                SingleSelectorTypeEnum.SOURCE_OF_FUNDS.name,
                hasSearch = false,
                isPaginated = false
            )
        }
    }

    private fun initDaoActivity() {
        daoActivity.setToolBarDesc(formatString(R.string.title_financial_information))
        daoActivity.showToolBarDetails()
        daoActivity.showButton(true)
        daoActivity.setEnableButton(true)
        daoActivity.showProgress(true)
        daoActivity.setActionEvent(this)
        daoActivity.setProgressValue(4)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            updateFreeTextFields()
            if (viewModel.hasFormChanged()) {
                daoActivity.showGoBackBottomSheet()
            } else {
                daoActivity.popBackStack()
            }
        }
    }

    private fun refreshFields() {
        tie_occupation.refresh()
        tie_occupation_others.refresh()
        tie_source_of_funds.refresh()
        tie_percent_ownership.refresh()
    }

    private fun validateForm() {
        val occupationObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = tie_occupation
        )
        val otherOccupationObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = tie_occupation_others
        )
        val sourceOfFundsObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = tie_source_of_funds
        )
        val percentOwnershipObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = tie_percent_ownership
        )
        initSetError(occupationObservable)
        initSetError(otherOccupationObservable)
        initSetError(sourceOfFundsObservable)
        initSetError(percentOwnershipObservable)
        RxCombineValidator(
            occupationObservable,
            otherOccupationObservable,
            sourceOfFundsObservable,
            percentOwnershipObservable
        )
            .asObservable()
            .distinctUntilChanged()
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .doOnNext {
                viewModel.input.isValidFormInput.onNext(it)
            }
            .subscribe()
            .addTo(disposables)
    }

    private fun showMissingFieldDialog() {
        showMaterialDialogError(
            message = formatString(R.string.msg_error_missing_fields)
        )
    }

    private fun navigateSingleSelector(
        selectorType: String,
        hasSearch: Boolean,
        isPaginated: Boolean
    ) {
        val action =
            DaoPersonalInformationStepFourFragmentDirections.actionSelectorActivity(
                selectorType,
                hasSearch,
                isPaginated
            )
        findNavController().navigate(action)
    }

    private fun navigationDaoResult(daoHit: DaoHit) {
        val action =
            DaoPersonalInformationStepFourFragmentDirections.actionDaoResultFragment(
                daoHit.referenceNumber,
                DaoResultFragment.TYPE_REACH_OUT_HIT,
                daoHit.businessName,
                daoHit.preferredBranch,
                daoHit.preferredBranchEmail
            )
        findNavController().navigate(action)
    }

    private fun setOtherTextInputEditText(
        dropDownId: String?,
        til: TextInputLayout,
        tie: TextInputEditText
    ) {
        if (OTHERS_CODE == dropDownId) {
            til.visibility(true)
            tie.clear()
        } else {
            til.visibility(false)
            tie.setText(Constant.EMPTY)
        }
    }

    private fun clearFormFocus() {
        constraint_layout.post {
            viewUtil.dismissKeyboard(getAppCompatActivity())
            constraint_layout.requestFocus()
            constraint_layout.isFocusableInTouchMode = true
        }
    }

    override fun onClickNext() {
        clearFormFocus()
        if (viewModel.hasValidForm()) {
            updateFreeTextFields()
            viewModel.onClickedNext()
        } else {
            showMissingFieldDialog()
            refreshFields()
        }
    }

    private fun updateFreeTextFields() {
        viewModel.setPreTextValues(
            tie_occupation_others.getTextNullable(),
            tie_percent_ownership.getTextNullable()
        )
    }

    @ThreadSafe
    companion object {
        const val OTHERS_CODE = "NV"
        const val EXTRA_IS_EDIT = "isEdit"
    }
}
