package com.unionbankph.corporate.dao.presentation.personal_info_3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
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
import com.unionbankph.corporate.databinding.FragmentDaoPersonalInformationStep3Binding
import com.unionbankph.corporate.settings.presentation.form.Selector
import com.unionbankph.corporate.settings.presentation.single_selector.SingleSelectorTypeEnum
import io.reactivex.rxkotlin.addTo
import javax.annotation.concurrent.ThreadSafe

class DaoPersonalInformationStepThreeFragment :
    BaseFragment<FragmentDaoPersonalInformationStep3Binding, DaoPersonalInformationStepThreeViewModel>(),
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
                binding.tieHomeAddress.setTextNullable(it)
            }.addTo(disposables)
        viewModel.input.streetNameInput
            .subscribe {
                binding.tieStreetName.setTextNullable(it)
            }.addTo(disposables)
        viewModel.input.villageBarangayInput
            .subscribe {
                binding.tieVillageBrgy.setTextNullable(it)
            }.addTo(disposables)
        viewModel.input.provinceInput
            .subscribe {
                if (it.value != null) {
                    binding.tieCity.setEnableView(it != null)
                    binding.tieProvince.setText(it.value)
                }
            }.addTo(disposables)
        viewModel.input.cityInput
            .subscribe {
                binding.tieCity.setText(it.value)
            }.addTo(disposables)
        viewModel.input.zipCodeInput
            .subscribe {
                binding.tieZipCode.setTextNullable(it)
            }.addTo(disposables)
        viewModel.input.countryInput
            .subscribe {
                binding.tieCountry.setText(it.value)
                if (it.id != Constant.getDefaultCountryDao().id) {
                    binding.tieProvince.setStyle(EditTextStyleEnum.FREE_TEXT)
                    binding.tieCity.setStyle(EditTextStyleEnum.FREE_TEXT)
                    binding.tieCity.setEnableView(true)
                    binding.tieProvince.setOnClickListener(null)
                    binding.tieCity.setOnClickListener(null)
                } else {
                    binding.tieProvince.setStyle(EditTextStyleEnum.DROP_DOWN)
                    binding.tieCity.setStyle(EditTextStyleEnum.DROP_DOWN)
                    binding.tieCity.setEnableView(viewModel.input.provinceInput.hasValue())
                    binding.tieProvince.setOnClickListener {
                        navigateSingleSelector(SingleSelectorTypeEnum.PROVINCE.name, hasSearch = true)
                    }
                    binding.tieCity.setOnClickListener {
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
                binding.tiePermanentHomeAddress.setTextNullable(it)
            }.addTo(disposables)
        viewModel.input.streetNamePermanentInput
            .subscribe {
                binding.tiePermanentStreetName.setTextNullable(it)
            }.addTo(disposables)
        viewModel.input.villageBarangayPermanentInput
            .subscribe {
                binding.tiePermanentVillageBrgy.setTextNullable(it)
            }.addTo(disposables)
        viewModel.input.provincePermanentInput
            .subscribe {
                if (it.value != null) {
                    binding.tiePermanentCity.setEnableView(it != null)
                    binding.tiePermanentProvince.setText(it.value)
                }
            }.addTo(disposables)
        viewModel.input.cityPermanentInput
            .subscribe {
                binding.tiePermanentCity.setText(it.value)
            }.addTo(disposables)
        viewModel.input.zipCodePermanentInput
            .subscribe {
                binding.tiePermanentZipCode.setTextNullable(it)
            }.addTo(disposables)
        viewModel.input.countryPermanentInput
            .subscribe {
                binding.tiePermanentCountry.setText(it.value)
                if (it.id != Constant.getDefaultCountryDao().id) {
                    binding.tiePermanentProvince.setStyle(EditTextStyleEnum.FREE_TEXT)
                    binding.tiePermanentCity.setStyle(EditTextStyleEnum.FREE_TEXT)
                    binding.tiePermanentCity.setEnableView(true)
                    binding.tiePermanentProvince.setOnClickListener(null)
                    binding.tiePermanentCity.setOnClickListener(null)
                } else {
                    binding.tiePermanentProvince.setStyle(EditTextStyleEnum.DROP_DOWN)
                    binding.tiePermanentCity.setStyle(EditTextStyleEnum.DROP_DOWN)
                    binding.tiePermanentCity.setEnableView(viewModel.input.provincePermanentInput.hasValue())
                    binding.tiePermanentProvince.setOnClickListener {
                        navigateSingleSelector(SingleSelectorTypeEnum.PROVINCE_PERMANENT.name, hasSearch = true)
                    }
                    binding.tiePermanentCity.setOnClickListener {
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
        binding.cbPermanentAddress.isChecked = isChecked
        binding.clPermanentAddress.visibility(!isChecked)
        if (isChecked) {
            binding.tiePermanentCountry.setText(Constant.EMPTY)
            binding.tiePermanentHomeAddress.setText(Constant.EMPTY)
            binding.tiePermanentStreetName.setText(Constant.EMPTY)
            binding.tiePermanentVillageBrgy.setText(Constant.EMPTY)
            binding.tiePermanentProvince.setText(Constant.EMPTY)
            binding.tiePermanentCity.setText(Constant.EMPTY)
            binding.tiePermanentZipCode.setText(Constant.EMPTY)
        } else {
            binding.tiePermanentCountry.clear()
            binding.tiePermanentHomeAddress.clear()
            binding.tiePermanentStreetName.clear()
            binding.tiePermanentVillageBrgy.clear()
            binding.tiePermanentProvince.clear()
            binding.tiePermanentCity.clear()
            binding.tiePermanentZipCode.clear()
            viewModel.input.countryPermanentInput.onNext(Constant.getDefaultCountryDao())
        }
    }

    private fun initViewModel() {
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
            binding.viewLoadingProvince.root.visibility(isLoading)
            binding.tieProvince.setEnableView(!isLoading)
        } else if (singleSelectorTypeEnum == SingleSelectorTypeEnum.CITY) {
            binding.viewLoadingCity.root.visibility(isLoading)
            binding.tieCity.setEnableView(!isLoading)
        } else if (singleSelectorTypeEnum == SingleSelectorTypeEnum.PROVINCE_PERMANENT) {
            binding.viewLoadingPermanentProvince.root.visibility(isLoading)
            binding.tiePermanentProvince.setEnableView(!isLoading)
        } else if (singleSelectorTypeEnum == SingleSelectorTypeEnum.CITY_PERMANENT) {
            binding.viewLoadingPermanentCity.root.visibility(isLoading)
            binding.tiePermanentCity.setEnableView(!isLoading)
        }
    }

    private fun init() {
        initImeOption()
        binding.tieCity.setEnableView(false)
        binding.tiePermanentCity.setEnableView(false)
        validateForm()
    }

    private fun initImeOption() {
        val imeOptionEditText = ImeOptionEditText()
        imeOptionEditText.addEditText(
            binding.tieProvince,
            binding.tieCity,
            binding.tieZipCode,
            binding.tieVillageBrgy,
            binding.tieStreetName,
            binding.tieHomeAddress,
            binding.tiePermanentProvince,
            binding.tiePermanentCity,
            binding.tiePermanentZipCode,
            binding.tiePermanentVillageBrgy,
            binding.tiePermanentStreetName,
            binding.tiePermanentHomeAddress
        )
        imeOptionEditText.setOnImeOptionListener(this)
        imeOptionEditText.startListener()
    }

    private fun initEventBus() {
        eventBus.inputSyncEvent.flowable.subscribe {
            when (it.eventType) {
                SingleSelectorTypeEnum.PROVINCE.name -> {
                    binding.tieCity.clear()
                    val selector = JsonHelper.fromJson<Selector>(it.payload)
                    viewModel.input.provinceInput.onNext(selector)
                }
                SingleSelectorTypeEnum.CITY.name -> {
                    val selector = JsonHelper.fromJson<Selector>(it.payload)
                    viewModel.input.cityInput.onNext(selector)
                }
                SingleSelectorTypeEnum.COUNTRY.name -> {
                    binding.tieProvince.clear()
                    binding.tieCity.clear()
                    val selector = JsonHelper.fromJson<Selector>(it.payload)
                    viewModel.input.countryInput.onNext(selector)
                }
                SingleSelectorTypeEnum.PROVINCE_PERMANENT.name -> {
                    binding.tiePermanentCity.clear()
                    val selector = JsonHelper.fromJson<Selector>(it.payload)
                    viewModel.input.provincePermanentInput.onNext(selector)
                }
                SingleSelectorTypeEnum.CITY_PERMANENT.name -> {
                    val selector = JsonHelper.fromJson<Selector>(it.payload)
                    viewModel.input.cityPermanentInput.onNext(selector)
                }
                SingleSelectorTypeEnum.COUNTRY_PERMANENT.name -> {
                    binding.tiePermanentProvince.clear()
                    binding.tiePermanentCity.clear()
                    val selector = JsonHelper.fromJson<Selector>(it.payload)
                    viewModel.input.countryPermanentInput.onNext(selector)
                }
            }
        }.addTo(disposables)
    }

    private fun initClickListener() {
        binding.tieCountry.setOnClickListener {
            navigateSingleSelector(SingleSelectorTypeEnum.COUNTRY.name, true)
        }

        binding.tiePermanentCountry.setOnClickListener {
            navigateSingleSelector(SingleSelectorTypeEnum.COUNTRY_PERMANENT.name, true)
        }
    }

    private fun initCheckListener() {
        binding.cbPermanentAddress.setOnCheckedChangeListener { _, isChecked ->
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
        binding.tieCountry.refresh()
        binding.tieHomeAddress.refresh()
        binding.tieStreetName.refresh()
        binding.tieVillageBrgy.refresh()
        binding.tieProvince.refresh()
        binding.tieCity.refresh()
        binding.tieZipCode.refresh()
        binding.tiePermanentHomeAddress.refresh()
        binding.tiePermanentStreetName.refresh()
        binding.tiePermanentVillageBrgy.refresh()
        binding.tiePermanentProvince.refresh()
        binding.tiePermanentCity.refresh()
        binding.tiePermanentZipCode.refresh()
        binding.tiePermanentCountry.refresh()
    }

    private fun validateForm() {
        val homeAddressObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tieHomeAddress
        )
        val streetNameObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tieStreetName
        )
        val villageBrgyObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tieVillageBrgy
        )
        val provinceObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tieProvince
        )
        val cityObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tieCity
        )
        val zipCodeObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_zip_code_length),
            editText = binding.tieZipCode
        )
        val countryObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tieCountry
        )
        val homeAddressPermanentObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tiePermanentHomeAddress
        )
        val streetNamePermanentObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tiePermanentStreetName
        )
        val villageBrgyPermanentObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tiePermanentVillageBrgy
        )
        val provincePermanentObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tiePermanentProvince
        )
        val cityPermanentObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tiePermanentCity
        )
        val zipPermanentCodeObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_zip_code_length),
            editText = binding.tiePermanentZipCode
        )
        val countryPermanentObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tiePermanentCountry
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
        binding.constraintLayout.post {
            viewUtil.dismissKeyboard(getAppCompatActivity())
            binding.constraintLayout.requestFocus()
            binding.constraintLayout.isFocusableInTouchMode = true
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
            binding.tieProvince.getTextNullable(),
            binding.tieCity.getTextNullable(),
            binding.tieHomeAddress.getTextNullable(),
            binding.tieStreetName.getTextNullable(),
            binding.tieVillageBrgy.getTextNullable(),
            binding.tieZipCode.getTextNullable(),
            binding.tiePermanentProvince.getTextNullable(),
            binding.tiePermanentCity.getTextNullable(),
            binding.tiePermanentHomeAddress.getTextNullable(),
            binding.tiePermanentStreetName.getTextNullable(),
            binding.tiePermanentVillageBrgy.getTextNullable(),
            binding.tiePermanentZipCode.getTextNullable()
        )
    }

    @ThreadSafe
    companion object {
        const val EXTRA_IS_EDIT = "isEdit"
    }

    override val viewModelClassType: Class<DaoPersonalInformationStepThreeViewModel>
        get() = DaoPersonalInformationStepThreeViewModel::class.java

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentDaoPersonalInformationStep3Binding
        get() = FragmentDaoPersonalInformationStep3Binding::inflate
}
