package com.unionbankph.corporate.account_setup.presentation.business_info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account_setup.presentation.AccountSetupActivity
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.widget.validator.validation.RxCombineValidator
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidationResult
import com.unionbankph.corporate.databinding.FragmentAsBusinessInformationBinding
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo

class AsBusinessInformationFragment
    : BaseFragment<FragmentAsBusinessInformationBinding, AsBusinessInformationViewModel>(){

    private val accountSetupActivity by lazyFast { getAppCompatActivity() as AccountSetupActivity }

    private val formDisposable = CompositeDisposable()

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)

        accountSetupActivity.apply {
            setIsScreenScrollable(false)
            setToolbarButtonType(AccountSetupActivity.BUTTON_SAVE_EXIT)
            showToolbarButton(true)
            setProgressValue(8)
        }
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initViews()
        initViewBindings()
    }

    private fun initViews() {
        binding.includeAsBusinessInformationAddress.apply {
            tvWidgetAddressCity.setEnableView(false)
            tieWidgetAddressCity.setEnableDropdownFields(false)
        }
        validateForm()
    }

    private fun initViewBindings() {
        viewModel.input.apply {
            businessName.subscribe {
                binding.tieAsBusinessInformationBusinessName.setTextNullable(it)
            }.addTo(disposables)
            line1Input.subscribe {
                binding.includeAsBusinessInformationAddress.tieWidgetAddressLine1.setTextNullable(it)
            }.addTo(disposables)
            line2Input.subscribe {
                binding.includeAsBusinessInformationAddress.tieWidgetAddressLine2.setTextNullable(it)
            }.addTo(disposables)
            regionInput.subscribe {
                binding.includeAsBusinessInformationAddress.apply {
                    tieWidgetAddressRegion.setTextNullable(it.value ?: "")
                    tvWidgetAddressCity.setEnableView(it != null)
                    tieWidgetAddressCity.setEnableDropdownFields(it != null)
                }
            }.addTo(disposables)
            cityInput.subscribe {
                binding.includeAsBusinessInformationAddress.tieWidgetAddressCity.setTextNullable(it.value ?: "")
            }.addTo(disposables)
            zipInput.subscribe {
                binding.includeAsBusinessInformationAddress.tieWidgetAddressZipCode.setTextNullable(it)
            }.addTo(disposables)
        }
    }

    private fun validateForm() {
        formDisposable.clear()

        val line1Observable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            hasSkip = false,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_50),
            editText = binding.includeAsBusinessInformationAddress.tieWidgetAddressLine1
        )
        val line2Observable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            hasSkip = false,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_50),
            editText = binding.includeAsBusinessInformationAddress.tieWidgetAddressLine2
        )
        val cityObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            hasSkip = false,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_40),
            editText = binding.includeAsBusinessInformationAddress.tieWidgetAddressCity,
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
            editText = binding.includeAsBusinessInformationAddress.tieWidgetAddressRegion,
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
            editText = binding.includeAsBusinessInformationAddress.tieWidgetAddressZipCode,
            customErrorMessage = String.format(
                getString(R.string.error_specific_field),
                getString(R.string.hint_zip_code)
            )
        )

        binding.includeAsBusinessInformationAddress.apply {
            initError(line1Observable, tvWidgetAddressLine1)
            initError(line2Observable, tvWidgetAddressLine2)
            initError(regionObservable, tvWidgetAddressRegion)
            initError(cityObservable, tvWidgetAddressCity)
            initError(zipObservable, tvWidgetAddressZipCode)
        }

        RxCombineValidator(
            line1Observable,
            line2Observable,
            cityObservable,
            regionObservable,
            zipObservable
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

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentAsBusinessInformationBinding
        get() = FragmentAsBusinessInformationBinding::inflate

    override val viewModelClassType: Class<AsBusinessInformationViewModel>
        get() = AsBusinessInformationViewModel::class.java
}