package com.unionbankph.corporate.account_setup.presentation.address

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account_setup.presentation.AccountSetupActivity
import com.unionbankph.corporate.account_setup.presentation.AccountSetupViewModel
import com.unionbankph.corporate.account_setup.presentation.personal_info.AsPersonalInformationFragmentDirections
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.widget.validator.validation.RxCombineValidator
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidationResult
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.dao.presentation.personal_info_3.DaoPersonalInformationStepThreeFragmentDirections
import com.unionbankph.corporate.databinding.FragmentAsAddressBinding
import com.unionbankph.corporate.settings.presentation.form.Selector
import com.unionbankph.corporate.settings.presentation.single_selector.SingleSelectorTypeEnum
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject

class AsAddressFragment : BaseFragment<FragmentAsAddressBinding, AsAddressViewModel>(){

    private val accountSetupActivity by lazyFast { getAppCompatActivity() as AccountSetupActivity }

    private val formDisposable = CompositeDisposable()

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)

        accountSetupActivity.apply {
            setIsScreenScrollable(false)
            setToolbarButtonType(AccountSetupActivity.BUTTON_SAVE_EXIT)
            showToolbarButton(true)
            setProgressValue(2)
        }
    }

    override fun onViewsBound() {
        super.onViewsBound()
        init()
        initViewBindings()
    }

    private fun init() {
        binding.apply {
            includeAsAddressPresentAddress.apply {
                tieWidgetAddressCity.setEnableViewSME(false)
                tvWidgetAddressCity.setEnableView(false)
            }
            includeAsAddressPermanentAddress.apply {
                tieWidgetAddressCity.setEnableViewSME(false)
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
                    binding.includeAsAddressPresentAddress.tieWidgetAddressRegion.clear()
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
                    viewModel.input.regionPermanentInput.onNext(selector)
                }
                SingleSelectorTypeEnum.CITY_PERMANENT.name -> {
                    val selector = JsonHelper.fromJson<Selector>(it.payload)
                    viewModel.input.cityPermanentInput.onNext(selector)
                }
            }
        }.addTo(disposables)
    }

    private fun initOnClicks() {
        binding.btnAsAddressNext.setOnClickListener {
            attemptSubmit()
        }
        binding.cbAsAddressSameAsPresentAddress.setOnCheckedChangeListener { _, isChecked ->
            handleSameAsPresentAddress(isChecked)
        }
        binding.includeAsAddressPresentAddress.tieWidgetAddressRegion.setOnClickListener {
            navigateSingleSelector(SingleSelectorTypeEnum.PROVINCE.name)
        }

        binding.includeAsAddressPermanentAddress.tieWidgetAddressRegion.setOnClickListener {
            navigateSingleSelector(SingleSelectorTypeEnum.PROVINCE_PERMANENT.name)
        }
        binding.includeAsAddressPresentAddress.tieWidgetAddressCity.setOnClickListener {
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
            binding.includeAsAddressPresentAddress.tieWidgetAddressLine1.setTextNullable(it)
        }.addTo(disposables)
        viewModel.input.line2Input.subscribe {
            binding.includeAsAddressPresentAddress.tieWidgetAddressLine2.setTextNullable(it)
        }.addTo(disposables)
        viewModel.input.cityInput.subscribe {
            binding.includeAsAddressPresentAddress.tieWidgetAddressCity.setTextNullable(it.value ?: "")
        }.addTo(disposables)
        viewModel.input.regionInput.subscribe {
            binding.includeAsAddressPresentAddress.apply {
                tieWidgetAddressCity.setEnableViewSME(it != null)
                tvWidgetAddressCity.setEnableView(it != null)
                tieWidgetAddressRegion.setTextNullable(it.value ?: "")
            }
        }.addTo(disposables)
        viewModel.input.zipInput.subscribe {
            binding.includeAsAddressPresentAddress.tieWidgetAddressZipCode.setTextNullable(it)
        }.addTo(disposables)
        viewModel.input.line1PermanentInput.subscribe {
            binding.includeAsAddressPermanentAddress.tieWidgetAddressLine1.setTextNullable(it)
        }.addTo(disposables)
        viewModel.input.line2PermanentInput.subscribe {
            binding.includeAsAddressPermanentAddress.tieWidgetAddressLine2.setTextNullable(it)
        }.addTo(disposables)
        viewModel.input.cityPermanentInput.subscribe {
            binding.includeAsAddressPermanentAddress.tieWidgetAddressCity.setTextNullable(it.value ?: "")
        }.addTo(disposables)
        viewModel.input.regionPermanentInput.subscribe {
            binding.includeAsAddressPermanentAddress.apply {
                tieWidgetAddressCity.setEnableViewSME(it != null)
                tvWidgetAddressCity.setEnableView(it != null)
                tieWidgetAddressRegion.setTextNullable(it.value ?: "")
            }
        }.addTo(disposables)
        viewModel.input.zipPermanentInput.subscribe {
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
            editText = binding.includeAsAddressPresentAddress.tieWidgetAddressLine1
        )
        val line2Observable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            hasSkip = false,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_50),
            editText = binding.includeAsAddressPresentAddress.tieWidgetAddressLine2
        )
        val cityObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            hasSkip = false,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_40),
            editText = binding.includeAsAddressPresentAddress.tieWidgetAddressCity,
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
            editText = binding.includeAsAddressPresentAddress.tieWidgetAddressRegion,
            customErrorMessage = String.format(
                getString(R.string.error_specific_field),
                getString(R.string.title_region)
            )
        )
        val zipObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            hasSkip = false,
            minLength = resources.getInteger(R.integer.max_zip_code_length),
            maxLength = resources.getInteger(R.integer.max_zip_code_length),
            editText = binding.includeAsAddressPresentAddress.tieWidgetAddressZipCode,
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
            minLength = resources.getInteger(R.integer.max_zip_code_length),
            maxLength = resources.getInteger(R.integer.max_zip_code_length),
            editText = binding.includeAsAddressPermanentAddress.tieWidgetAddressZipCode,
            customErrorMessage = String.format(
                getString(R.string.error_specific_field),
                getString(R.string.hint_zip_code)
            )
        )
        initError(line1Observable, binding.includeAsAddressPresentAddress.tvWidgetAddressLine1)
        initError(line2Observable, binding.includeAsAddressPresentAddress.tvWidgetAddressLine2)
        initError(cityObservable, binding.includeAsAddressPresentAddress.tvWidgetAddressCity)
        initError(regionObservable, binding.includeAsAddressPresentAddress.tvWidgetAddressRegion)
        initError(zipObservable, binding.includeAsAddressPresentAddress.tvWidgetAddressZipCode)
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
        if (viewModel.hasValidForm()) {

        } else {
            refreshFields()
        }
    }

    private fun refreshFields() {
        binding.apply {
            includeAsAddressPresentAddress.apply {
                tieWidgetAddressLine1.refresh()
                tieWidgetAddressLine2.refresh()
                tieWidgetAddressCity.refresh()
                tieWidgetAddressRegion.refresh()
                tieWidgetAddressZipCode.refresh()
            }
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
                labelTextView.setTextColor(when (validation.isProper) {
                    true -> ContextCompat.getColor(requireContext(), R.color.dsColorDarkGray)
                    else -> ContextCompat.getColor(requireContext(), R.color.colorErrorColor)
                })
            }.addTo(formDisposable)
    }

    private fun handleSameAsPresentAddress(isSame: Boolean) {
        binding.apply {
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