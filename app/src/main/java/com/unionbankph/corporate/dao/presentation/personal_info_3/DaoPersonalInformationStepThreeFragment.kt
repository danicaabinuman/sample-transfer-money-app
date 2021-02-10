package com.unionbankph.corporate.dao.presentation.personal_info_3

import android.os.Bundle
import androidx.activity.addCallback
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.app.common.widget.edittext.ImeOptionEditText
import com.unionbankph.corporate.app.common.widget.validator.validation.RxCombineValidator
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.constant.EditTextStyleEnum
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.dao.domain.model.DaoHit
import com.unionbankph.corporate.dao.presentation.DaoActivity
import com.unionbankph.corporate.dao.presentation.result.DaoResultFragment
import com.unionbankph.corporate.settings.presentation.form.Selector
import com.unionbankph.corporate.settings.presentation.single_selector.SingleSelectorTypeEnum
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_dao_personal_information_step_3.*
import javax.annotation.concurrent.ThreadSafe

class DaoPersonalInformationStepThreeFragment :
    BaseFragment<DaoPersonalInformationStepThreeViewModel>(R.layout.fragment_dao_personal_information_step_3),
    DaoActivity.ActionEvent, ImeOptionEditText.OnImeOptionListener {

    private val daoActivity by lazyFast { getAppCompatActivity() as DaoActivity }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initDaoActivity()
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initViewModel()
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
        initCheckListener()
    }

    private fun initBinding() {
        viewModel.loadDaoForm(daoActivity.viewModel.defaultDaoForm())
        viewModel.input.permanentAddressInput
            .subscribe {
                permanentAddressInput(it)
            }.addTo(disposables)
        viewModel.input.homeAddressInput
            .subscribe {
                tie_home_address.setTextNullable(it)
            }.addTo(disposables)
        viewModel.input.streetNameInput
            .subscribe {
                tie_street_name.setTextNullable(it)
            }.addTo(disposables)
        viewModel.input.villageBarangayInput
            .subscribe {
                tie_village_brgy.setTextNullable(it)
            }.addTo(disposables)
        viewModel.input.provinceInput
            .subscribe {
                if (it.value != null) {
                    tie_city.setEnableView(it != null)
                    tie_province.setText(it.value)
                }
            }.addTo(disposables)
        viewModel.input.cityInput
            .subscribe {
                tie_city.setText(it.value)
            }.addTo(disposables)
        viewModel.input.zipCodeInput
            .subscribe {
                tie_zip_code.setTextNullable(it)
            }.addTo(disposables)
        viewModel.input.countryInput
            .subscribe {
                tie_country.setText(it.value)
                if (it.id != Constant.getDefaultCountryDao().id) {
                    tie_province.setStyle(EditTextStyleEnum.FREE_TEXT)
                    tie_city.setStyle(EditTextStyleEnum.FREE_TEXT)
                    tie_city.setEnableView(true)
                    tie_province.setOnClickListener(null)
                    tie_city.setOnClickListener(null)
                } else {
                    tie_province.setStyle(EditTextStyleEnum.DROP_DOWN)
                    tie_city.setStyle(EditTextStyleEnum.DROP_DOWN)
                    tie_city.setEnableView(viewModel.input.provinceInput.hasValue())
                    tie_province.setOnClickListener {
                        navigateSingleSelector(SingleSelectorTypeEnum.PROVINCE.name, hasSearch = true)
                    }
                    tie_city.setOnClickListener {
                        navigateSingleSelector(
                            SingleSelectorTypeEnum.CITY.name,
                            hasSearch = true,
                            param = viewModel.input.provinceInput.value?.id
                        )
                    }
                }
            }.addTo(disposables)
        viewModel.input.homeAddressPermanentInput
            .subscribe {
                tie_permanent_home_address.setTextNullable(it)
            }.addTo(disposables)
        viewModel.input.streetNamePermanentInput
            .subscribe {
                tie_permanent_street_name.setTextNullable(it)
            }.addTo(disposables)
        viewModel.input.villageBarangayPermanentInput
            .subscribe {
                tie_permanent_village_brgy.setTextNullable(it)
            }.addTo(disposables)
        viewModel.input.provincePermanentInput
            .subscribe {
                if (it.value != null) {
                    tie_permanent_city.setEnableView(it != null)
                    tie_permanent_province.setText(it.value)
                }
            }.addTo(disposables)
        viewModel.input.cityPermanentInput
            .subscribe {
                tie_permanent_city.setText(it.value)
            }.addTo(disposables)
        viewModel.input.zipCodePermanentInput
            .subscribe {
                tie_permanent_zip_code.setTextNullable(it)
            }.addTo(disposables)
        viewModel.input.countryPermanentInput
            .subscribe {
                tie_permanent_country.setText(it.value)
                if (it.id != Constant.getDefaultCountryDao().id) {
                    tie_permanent_province.setStyle(EditTextStyleEnum.FREE_TEXT)
                    tie_permanent_city.setStyle(EditTextStyleEnum.FREE_TEXT)
                    tie_permanent_city.setEnableView(true)
                    tie_permanent_province.setOnClickListener(null)
                    tie_permanent_city.setOnClickListener(null)
                } else {
                    tie_permanent_province.setStyle(EditTextStyleEnum.DROP_DOWN)
                    tie_permanent_city.setStyle(EditTextStyleEnum.DROP_DOWN)
                    tie_permanent_city.setEnableView(viewModel.input.provincePermanentInput.hasValue())
                    tie_permanent_province.setOnClickListener {
                        navigateSingleSelector(SingleSelectorTypeEnum.PROVINCE_PERMANENT.name, hasSearch = true)
                    }
                    tie_permanent_city.setOnClickListener {
                        navigateSingleSelector(
                            SingleSelectorTypeEnum.CITY_PERMANENT.name,
                            hasSearch = true,
                            param = viewModel.input.provincePermanentInput.value?.id
                        )
                    }
                }
            }.addTo(disposables)
        viewModel.isEditMode
            .subscribe {
                if (it) {
                    daoActivity.setButtonName(formatString(R.string.action_save))
                }
            }.addTo(disposables)
    }

    private fun permanentAddressInput(isChecked: Boolean) {
        cb_permanent_address.isChecked = isChecked
        cl_permanent_address.visibility(!isChecked)
        if (isChecked) {
            tie_permanent_country.setText(Constant.EMPTY)
            tie_permanent_home_address.setText(Constant.EMPTY)
            tie_permanent_street_name.setText(Constant.EMPTY)
            tie_permanent_village_brgy.setText(Constant.EMPTY)
            tie_permanent_province.setText(Constant.EMPTY)
            tie_permanent_city.setText(Constant.EMPTY)
            tie_permanent_zip_code.setText(Constant.EMPTY)
        } else {
            tie_permanent_country.clear()
            tie_permanent_home_address.clear()
            tie_permanent_street_name.clear()
            tie_permanent_village_brgy.clear()
            tie_permanent_province.clear()
            tie_permanent_city.clear()
            tie_permanent_zip_code.clear()
            viewModel.input.countryPermanentInput.onNext(Constant.getDefaultCountryDao())
        }
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(
            this,
            viewModelFactory
        )[DaoPersonalInformationStepThreeViewModel::class.java]
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
        viewModel.output.loadingField.observe(viewLifecycleOwner, EventObserver {
            showFieldProgressBar(it, true)
        })
        viewModel.output.dismissLoadingField.observe(viewLifecycleOwner, EventObserver {
            showFieldProgressBar(it, false)
        })
        viewModel.output.loadingPermanentField.observe(viewLifecycleOwner, EventObserver {
            showFieldProgressBar(it, true)
        })
        viewModel.output.dismissLoadingPermanentField.observe(viewLifecycleOwner, EventObserver {
            showFieldProgressBar(it, false)
        })
        viewModel.navigateNextStep.observe(viewLifecycleOwner, EventObserver {
            daoActivity.setPersonalInformationStepThreeInput(it)
            if (viewModel.isEditMode.value == true) {
                requireActivity().onBackPressed()
            } else {
                findNavController().navigate(R.id.action_personal_information_step_four)
            }
        })
        viewModel.navigateResult.observe(viewLifecycleOwner, EventObserver {
            navigationDaoResult(it)
        })
        if (daoActivity.viewModel.hasPersonalInformationThreeInput.hasValue() &&
            !viewModel.isLoadedScreen.hasValue()
        ) {
            daoActivity.viewModel.input3.let {
                viewModel.setExistingPersonalInformationStepThree(it)
            }
        }
        arguments?.getBoolean(EXTRA_IS_EDIT, false)?.let {
            viewModel.isEditMode.onNext(it)
        }
    }

    private fun showFieldProgressBar(singleSelectorTypeEnum: SingleSelectorTypeEnum, isLoading: Boolean) {
        if (singleSelectorTypeEnum == SingleSelectorTypeEnum.PROVINCE) {
            viewLoadingProvince.visibility(isLoading)
            tie_province.setEnableView(!isLoading)
        } else if (singleSelectorTypeEnum == SingleSelectorTypeEnum.CITY) {
            viewLoadingCity.visibility(isLoading)
            tie_city.setEnableView(!isLoading)
        } else if (singleSelectorTypeEnum == SingleSelectorTypeEnum.PROVINCE_PERMANENT) {
            viewLoadingPermanentProvince.visibility(isLoading)
            tie_permanent_province.setEnableView(!isLoading)
        } else if (singleSelectorTypeEnum == SingleSelectorTypeEnum.CITY_PERMANENT) {
            viewLoadingPermanentCity.visibility(isLoading)
            tie_permanent_city.setEnableView(!isLoading)
        }
    }

    private fun init() {
        initImeOption()
        tie_city.setEnableView(false)
        tie_permanent_city.setEnableView(false)
        validateForm()
    }

    private fun initImeOption() {
        val imeOptionEditText = ImeOptionEditText()
        imeOptionEditText.addEditText(
            tie_province,
            tie_city,
            tie_zip_code,
            tie_village_brgy,
            tie_street_name,
            tie_home_address,
            tie_permanent_province,
            tie_permanent_city,
            tie_permanent_zip_code,
            tie_permanent_village_brgy,
            tie_permanent_street_name,
            tie_permanent_home_address
        )
        imeOptionEditText.setOnImeOptionListener(this)
        imeOptionEditText.startListener()
    }

    private fun initEventBus() {
        eventBus.inputSyncEvent.flowable.subscribe {
            when (it.eventType) {
                SingleSelectorTypeEnum.PROVINCE.name -> {
                    tie_city.clear()
                    val selector = JsonHelper.fromJson<Selector>(it.payload)
                    viewModel.input.provinceInput.onNext(selector)
                }
                SingleSelectorTypeEnum.CITY.name -> {
                    val selector = JsonHelper.fromJson<Selector>(it.payload)
                    viewModel.input.cityInput.onNext(selector)
                }
                SingleSelectorTypeEnum.COUNTRY.name -> {
                    tie_province.clear()
                    tie_city.clear()
                    val selector = JsonHelper.fromJson<Selector>(it.payload)
                    viewModel.input.countryInput.onNext(selector)
                }
                SingleSelectorTypeEnum.PROVINCE_PERMANENT.name -> {
                    tie_permanent_city.clear()
                    val selector = JsonHelper.fromJson<Selector>(it.payload)
                    viewModel.input.provincePermanentInput.onNext(selector)
                }
                SingleSelectorTypeEnum.CITY_PERMANENT.name -> {
                    val selector = JsonHelper.fromJson<Selector>(it.payload)
                    viewModel.input.cityPermanentInput.onNext(selector)
                }
                SingleSelectorTypeEnum.COUNTRY_PERMANENT.name -> {
                    tie_permanent_province.clear()
                    tie_permanent_city.clear()
                    val selector = JsonHelper.fromJson<Selector>(it.payload)
                    viewModel.input.countryPermanentInput.onNext(selector)
                }
            }
        }.addTo(disposables)
    }

    private fun initClickListener() {
        tie_country.setOnClickListener {
            navigateSingleSelector(SingleSelectorTypeEnum.COUNTRY.name, true)
        }

        tie_permanent_country.setOnClickListener {
            navigateSingleSelector(SingleSelectorTypeEnum.COUNTRY_PERMANENT.name, true)
        }
    }

    private fun initCheckListener() {
        cb_permanent_address.setOnCheckedChangeListener { _, isChecked ->
            viewModel.input.permanentAddressInput.onNext(isChecked)
        }
    }

    private fun initDaoActivity() {
        daoActivity.setToolBarDesc(formatString(R.string.title_residential_address))
        daoActivity.showToolBarDetails()
        daoActivity.showButton(true)
        daoActivity.setEnableButton(true)
        daoActivity.showProgress(true)
        daoActivity.setActionEvent(this)
        daoActivity.setProgressValue(3)
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
        tie_country.refresh()
        tie_home_address.refresh()
        tie_street_name.refresh()
        tie_village_brgy.refresh()
        tie_province.refresh()
        tie_city.refresh()
        tie_zip_code.refresh()
        tie_permanent_home_address.refresh()
        tie_permanent_street_name.refresh()
        tie_permanent_village_brgy.refresh()
        tie_permanent_province.refresh()
        tie_permanent_city.refresh()
        tie_permanent_zip_code.refresh()
        tie_permanent_country.refresh()
    }

    private fun validateForm() {
        val homeAddressObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = tie_home_address
        )
        val streetNameObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = tie_street_name
        )
        val villageBrgyObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = tie_village_brgy
        )
        val provinceObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = tie_province
        )
        val cityObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = tie_city
        )
        val zipCodeObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_zip_code_length),
            editText = tie_zip_code
        )
        val countryObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = tie_country
        )
        val homeAddressPermanentObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = tie_permanent_home_address
        )
        val streetNamePermanentObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = tie_permanent_street_name
        )
        val villageBrgyPermanentObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = tie_permanent_village_brgy
        )
        val provincePermanentObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = tie_permanent_province
        )
        val cityPermanentObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = tie_permanent_city
        )
        val zipPermanentCodeObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_zip_code_length),
            editText = tie_permanent_zip_code
        )
        val countryPermanentObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = tie_permanent_country
        )
        initSetError(homeAddressObservable)
        initSetError(streetNameObservable)
        initSetError(villageBrgyObservable)
        initSetError(provinceObservable)
        initSetError(cityObservable)
        initSetError(zipCodeObservable)
        initSetError(countryObservable)
        initSetError(homeAddressPermanentObservable)
        initSetError(streetNamePermanentObservable)
        initSetError(villageBrgyPermanentObservable)
        initSetError(provincePermanentObservable)
        initSetError(cityPermanentObservable)
        initSetError(zipPermanentCodeObservable)
        initSetError(countryPermanentObservable)
        RxCombineValidator(
            homeAddressObservable,
            streetNameObservable,
            villageBrgyObservable,
            provinceObservable,
            cityObservable,
            zipCodeObservable,
            countryObservable,
            homeAddressPermanentObservable,
            streetNamePermanentObservable,
            villageBrgyPermanentObservable,
            provincePermanentObservable,
            cityPermanentObservable,
            zipPermanentCodeObservable,
            countryPermanentObservable
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

    private fun navigateSingleSelector(selectorType: String, hasSearch: Boolean = false, param: String? = null) {
        val action =
            DaoPersonalInformationStepThreeFragmentDirections.actionSelectorActivity(
                selectorType,
                hasSearch,
                param
            )
        findNavController().navigate(action)
    }

    private fun navigationDaoResult(daoHit: DaoHit) {
        val action =
            DaoPersonalInformationStepThreeFragmentDirections.actionDaoResultFragment(
                daoHit.referenceNumber,
                DaoResultFragment.TYPE_REACH_OUT_HIT,
                daoHit.businessName,
                daoHit.preferredBranch,
                daoHit.preferredBranchEmail
            )
        findNavController().navigate(action)
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
            tie_province.getTextNullable(),
            tie_city.getTextNullable(),
            tie_home_address.getTextNullable(),
            tie_street_name.getTextNullable(),
            tie_village_brgy.getTextNullable(),
            tie_zip_code.getTextNullable(),
            tie_permanent_province.getTextNullable(),
            tie_permanent_city.getTextNullable(),
            tie_permanent_home_address.getTextNullable(),
            tie_permanent_street_name.getTextNullable(),
            tie_permanent_village_brgy.getTextNullable(),
            tie_permanent_zip_code.getTextNullable()
        )
    }

    @ThreadSafe
    companion object {
        const val EXTRA_IS_EDIT = "isEdit"
    }
}
