package com.unionbankph.corporate.account_setup.presentation.address

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account_setup.data.Address
import com.unionbankph.corporate.account_setup.data.PersonalInformation
import com.unionbankph.corporate.account_setup.presentation.AccountSetupActivity
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.app.common.widget.validator.validation.RxCombineValidator
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidationResult
import com.unionbankph.corporate.auth.presentation.login.LoginActivity
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.databinding.FragmentAsAddressBinding
import com.unionbankph.corporate.settings.presentation.form.Selector
import com.unionbankph.corporate.settings.presentation.single_selector.SingleSelectorTypeEnum
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import timber.log.Timber

class AsAddressFragment : BaseFragment<FragmentAsAddressBinding, AsAddressViewModel>(){

    private val accountSetupActivity by lazyFast { getAppCompatActivity() as AccountSetupActivity }

    private val formDisposable = CompositeDisposable()

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)

        accountSetupActivity.apply {
            setIsScreenScrollable(false)
            setToolbarButtonType(AccountSetupActivity.BUTTON_SAVE_EXIT)
            showToolbarButton(true)
            setProgressValue(7)
        }
    }

    override fun onViewModelBound() {
        super.onViewModelBound()

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
                is UiState.Exit -> {
                    navigator.navigateClearStacks(
                        getAppCompatActivity(),
                        LoginActivity::class.java,
                        Bundle().apply { putBoolean(LoginActivity.EXTRA_SPLASH_SCREEN, false) },
                        true
                    )
                }
            }
        })

        viewModel.state.observe(viewLifecycleOwner, EventObserver {
            Timber.e("address " + JsonHelper.toJson(it))
            accountSetupActivity.viewModel.setAddressInput(it)
            findNavController().navigate(R.id.action_business_information)
        })

        val hasExistingData =
            accountSetupActivity.viewModel.state.value?.hasPersonalInfoInput ?: false

        if (hasExistingData) {
            viewModel.populateFieldsWithExisting(
                accountSetupActivity.viewModel.state.value?.address ?: Address()
            )
        }
    }

    override fun onViewsBound() {
        super.onViewsBound()
        init()
        initViewBindings()
    }

    private fun init() {
        binding.apply {
            cbAsAddressSameAsPresentAddress.setMSMETheme()

            tieAsAddressCity.setEnableDropdownFields(false)
            tvAsAddressCity.setEnableView(false)

            includeAsAddressPermanentAddress.apply {
                tieWidgetAddressCity.setEnableDropdownFields(false)
                tvWidgetAddressCity.setEnableView(false)
            }
        }
        validateForm()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initEventBus()
        initOnClicks()
    }

    private fun initEventBus() {
        eventBus.inputSyncEvent.flowable.subscribe {
            when (it.eventType) {
                SingleSelectorTypeEnum.PROVINCE.name -> {
                    binding.tieAsAddressCity.clear()
                    val selector = JsonHelper.fromJson<Selector>(it.payload)
                    viewModel.input.regionInput.onNext(selector)
                }
                SingleSelectorTypeEnum.CITY.name -> {
                    val selector = JsonHelper.fromJson<Selector>(it.payload)
                    viewModel.input.cityInput.onNext(selector)
                }
                SingleSelectorTypeEnum.PROVINCE_PERMANENT.name -> {
                    binding.includeAsAddressPermanentAddress.tieWidgetAddressRegion.clear()
                    val selector = JsonHelper.fromJson<Selector>(it.payload)
                    viewModel.input.permanentRegionInput.onNext(selector)
                }
                SingleSelectorTypeEnum.CITY_PERMANENT.name -> {
                    val selector = JsonHelper.fromJson<Selector>(it.payload)
                    viewModel.input.permanentCityInput.onNext(selector)
                }
            }
        }.addTo(disposables)
    }

    private fun initOnClicks() {
        binding.btnAsAddressNext.setOnClickListener {
            attemptSubmit()
        }
        binding.cbAsAddressSameAsPresentAddress.setOnCheckedChangeListener { _, isChecked ->
            viewModel.input.isSameAsPresentAddress.onNext(isChecked)
        }
        binding.tieAsAddressRegion.setOnClickListener {
            navigateSingleSelector(SingleSelectorTypeEnum.PROVINCE.name)
        }
        binding.includeAsAddressPermanentAddress.tieWidgetAddressRegion.setOnClickListener {
            navigateSingleSelector(SingleSelectorTypeEnum.PROVINCE_PERMANENT.name)
        }
        binding.tieAsAddressCity.setOnClickListener {
            navigateSingleSelector(
                SingleSelectorTypeEnum.CITY.name,
                hasSearch = true,
                param = viewModel.input.regionInput.value?.id
            )
        }
        binding.includeAsAddressPermanentAddress.tieWidgetAddressCity.setOnClickListener {
            navigateSingleSelector(
                SingleSelectorTypeEnum.CITY.name,
                hasSearch = true,
                param = viewModel.input.regionInput.value?.id
            )
        }
    }

    private fun initViewBindings() {
        viewModel.input.isSameAsPresentAddress.subscribe {
            handleSameAsPresentAddress(it)
        }.addTo(disposables)
        viewModel.input.line1Input.subscribe {
            binding.tieAsAddressLine1.setTextNullable(it)
        }.addTo(disposables)
        viewModel.input.line2Input.subscribe {
            binding.tieAsAddressLine2.setTextNullable(it)
        }.addTo(disposables)
        viewModel.input.cityInput.subscribe {
            binding.tieAsAddressCity.setTextNullable(it.value ?: "")
        }.addTo(disposables)
        viewModel.input.regionInput.subscribe {
            binding.apply {
                tieAsAddressCity.setEnableDropdownFields(it != null)
                tvAsAddressCity.setEnableView(it != null)
                tieAsAddressRegion.setTextNullable(it.value ?: "")
            }
        }.addTo(disposables)
        viewModel.input.zipInput.subscribe {
            binding.tieAsAddressZipCode.setTextNullable(it)
        }.addTo(disposables)
        viewModel.input.permanentLine1Input.subscribe {
            binding.includeAsAddressPermanentAddress.tieWidgetAddressLine1.setTextNullable(it)
        }.addTo(disposables)
        viewModel.input.permanentLine2Input.subscribe {
            binding.includeAsAddressPermanentAddress.tieWidgetAddressLine2.setTextNullable(it)
        }.addTo(disposables)
        viewModel.input.permanentCityInput.subscribe {
            binding.includeAsAddressPermanentAddress.tieWidgetAddressCity.setTextNullable(it.value ?: "")
        }.addTo(disposables)
        viewModel.input.permanentRegionInput.subscribe {
            binding.includeAsAddressPermanentAddress.apply {
                tieWidgetAddressCity.setEnableDropdownFields(it != null)
                tvWidgetAddressCity.setEnableView(it != null)
                tieWidgetAddressRegion.setTextNullable(it.value ?: "")
            }
        }.addTo(disposables)
        viewModel.input.permanentZipInput.subscribe {
            binding.includeAsAddressPermanentAddress.tieWidgetAddressZipCode.setTextNullable(it)
        }.addTo(disposables)
    }

    private fun validateForm() {
        formDisposable.clear()
        val line1Observable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            hasSkip = false,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_50),
            editText = binding.tieAsAddressLine1
        )
        val line2Observable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            hasSkip = false,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_50),
            editText = binding.tieAsAddressLine2
        )
        val cityObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            hasSkip = false,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_40),
            editText = binding.tieAsAddressCity,
            customErrorMessage = String.format(
                getString(R.string.error_specific_field),
                getString(R.string.title_city)
            )
        )
        val regionObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            hasSkip = false,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_40),
            editText = binding.tieAsAddressRegion,
            customErrorMessage = String.format(
                getString(R.string.error_specific_field),
                getString(R.string.title_region)
            )
        )
        val zipObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            hasSkip = false,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_zip_code_length),
            editText = binding.tieAsAddressZipCode,
            customErrorMessage = String.format(
                getString(R.string.error_specific_field),
                getString(R.string.hint_zip_code)
            )
        )
        val line1PermanentObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            hasSkip = false,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_50),
            editText = binding.includeAsAddressPermanentAddress.tieWidgetAddressLine1
        )
        val line2PermanentObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            hasSkip = false,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_50),
            editText = binding.includeAsAddressPermanentAddress.tieWidgetAddressLine2
        )
        val cityPermanentObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            hasSkip = false,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_40),
            editText = binding.includeAsAddressPermanentAddress.tieWidgetAddressCity,
            customErrorMessage = String.format(
                getString(R.string.error_specific_field),
                getString(R.string.title_city)
            )
        )
        val regionPermanentObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            hasSkip = false,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_40),
            editText = binding.includeAsAddressPermanentAddress.tieWidgetAddressRegion,
            customErrorMessage = String.format(
                getString(R.string.error_specific_field),
                getString(R.string.title_region)
            )
        )
        val zipPermanentObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            hasSkip = false,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_zip_code_length),
            editText = binding.includeAsAddressPermanentAddress.tieWidgetAddressZipCode,
            customErrorMessage = String.format(
                getString(R.string.error_specific_field),
                getString(R.string.hint_zip_code)
            )
        )
        initError(line1Observable, binding.tieAsAddressLine1)
        initError(line2Observable, binding.tieAsAddressLine2)
        initError(cityObservable, binding.tieAsAddressCity)
        initError(regionObservable, binding.tieAsAddressRegion)
        initError(zipObservable, binding.tieAsAddressZipCode)
        initError(line1PermanentObservable, binding.includeAsAddressPermanentAddress.tvWidgetAddressLine1)
        initError(line2PermanentObservable, binding.includeAsAddressPermanentAddress.tvWidgetAddressLine2)
        initError(cityPermanentObservable, binding.includeAsAddressPermanentAddress.tvWidgetAddressCity)
        initError(regionPermanentObservable, binding.includeAsAddressPermanentAddress.tvWidgetAddressRegion)
        initError(zipPermanentObservable, binding.includeAsAddressPermanentAddress.tvWidgetAddressZipCode)

        RxCombineValidator(
            line1Observable,
            line2Observable,
            cityObservable,
            regionObservable,
            zipObservable,
            line1PermanentObservable,
            line2PermanentObservable,
            cityPermanentObservable,
            regionPermanentObservable,
            zipPermanentObservable
        )
            .asObservable()
            .distinctUntilChanged()
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .doOnNext {
                viewModel.input.isValidFormInput.onNext(it)
            }
            .subscribe()
            .addTo(formDisposable)
    }

    private fun attemptSubmit() {
        ignoreSomeFields()

        if (viewModel.hasValidForm()) {
            syncInputData()
            viewModel.onClickedNext()
        } else {
            refreshFields()
        }
    }

    /* Todo: Remove this later */
    private fun ignoreSomeFields() {
        binding.apply {
            tieAsAddressRegion.setText(Constant.EMPTY)
            tieAsAddressCity.setText(Constant.EMPTY)

            includeAsAddressPermanentAddress.apply {
                tieWidgetAddressRegion.setText(Constant.EMPTY)
                tieWidgetAddressCity.setText(Constant.EMPTY)
            }
        }
    }

    private fun syncInputData() {
        binding.apply {
            viewModel.syncInputData(
                line1Input = binding.tieAsAddressLine1.getTextNullable(),
                line2Input = binding.tieAsAddressLine2.getTextNullable(),
                regionInput = viewModel.input.regionInput.value,
                cityInput = viewModel.input.cityInput.value,
                zipCodeInput = binding.tieAsAddressZipCode.getTextNullable(),
                permanentLine1Input = includeAsAddressPermanentAddress.tieWidgetAddressLine1.getTextNullable(),
                permanentLine2Input = includeAsAddressPermanentAddress.tieWidgetAddressLine2.getTextNullable(),
                permanentRegionInput = viewModel.input.permanentRegionInput.value,
                permanentCityInput = viewModel.input.permanentCityInput.value,
                permanentZipCodeInput = includeAsAddressPermanentAddress.tieWidgetAddressZipCode.getTextNullable(),
                isSameAsPermanentAddress = viewModel.input.isSameAsPresentAddress.value
            )
        }
    }

    private fun refreshFields() {
        binding.apply {
            tieAsAddressLine1.refresh()
            tieAsAddressLine2.refresh()
            tieAsAddressCity.refresh()
            tieAsAddressRegion.refresh()
            tieAsAddressZipCode.refresh()
            includeAsAddressPermanentAddress.apply {
                tieWidgetAddressLine1.refresh()
                tieWidgetAddressLine2.refresh()
                tieWidgetAddressCity.refresh()
                tieWidgetAddressRegion.refresh()
                tieWidgetAddressZipCode.refresh()
            }
        }
    }

    private fun initError(
        textInputEditTextObservable: Observable<RxValidationResult<EditText>>,
        labelTextView: TextView
    ) {
        textInputEditTextObservable
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe { validation ->
                viewUtil.setError(validation)
                labelTextView.setFieldLabelError(validation.isProper)
            }.addTo(formDisposable)
    }

    private fun handleSameAsPresentAddress(isSame: Boolean) {
        binding.apply {
            cbAsAddressSameAsPresentAddress.isChecked = isSame
            includeAsAddressPermanentAddress.apply {
                root.visibility(!isSame)
                if (isSame) {
                    tieWidgetAddressLine1.setText(Constant.EMPTY)
                    tieWidgetAddressLine2.setText(Constant.EMPTY)
                    tieWidgetAddressCity.setText(Constant.EMPTY)
                    tieWidgetAddressRegion.setText(Constant.EMPTY)
                    tieWidgetAddressZipCode.setText(Constant.EMPTY)
                } else {
                    tieWidgetAddressLine1.clear()
                    tieWidgetAddressLine2.clear()
                    tieWidgetAddressCity.clear()
                    tieWidgetAddressRegion.clear()
                    tieWidgetAddressZipCode.clear()
                }
            }
        }
    }

    private fun navigateSingleSelector(selectorType: String, hasSearch: Boolean = false, param: String? = null) {
        val action = AsAddressFragmentDirections.actionSelectorActivity(
                selectorType,
                hasSearch,
                param
            )
        findNavController().navigate(action)
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentAsAddressBinding
        get() = FragmentAsAddressBinding::inflate

    override val viewModelClassType: Class<AsAddressViewModel>
        get() = AsAddressViewModel::class.java
}