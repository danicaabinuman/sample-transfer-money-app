package com.unionbankph.corporate.dao.presentation.welcome

import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.refresh
import com.unionbankph.corporate.app.common.platform.bus.event.InputSyncEvent
import com.unionbankph.corporate.app.common.widget.validator.validation.RxCombineValidator
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidator
import com.unionbankph.corporate.auth.data.model.CountryCode
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.dao.presentation.DaoActivity
import com.unionbankph.corporate.databinding.FragmentDaoWelcomeBinding
import com.unionbankph.corporate.settings.presentation.form.Selector
import com.unionbankph.corporate.settings.presentation.single_selector.SingleSelectorTypeEnum
import io.reactivex.Observable
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit

class DaoWelcomeFragment :
    BaseFragment<FragmentDaoWelcomeBinding, DaoWelcomeViewModel>(),
    DaoActivity.ActionEvent {

    private val daoActivity by lazyFast { getAppCompatActivity() as DaoActivity }

    override fun onViewModelBound() {
        super.onViewModelBound()
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initBinding()
        init()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initEventBus()
        initListener()
    }

    private fun initBinding() {
        viewModel.salutationInput
            .subscribe {
                binding.tieSalutation.setText(it.value)
            }.addTo(disposables)
        viewModel.countryCodeInput
            .subscribe {
                showCountryDetails(it)
            }.addTo(disposables)
    }

    private fun initListener() {
        binding.tieSalutation.setOnClickListener {
            navigateSingleSelector(SingleSelectorTypeEnum.SALUTATION.name)
        }
        binding.viewCountryCode.tieCountryCode.setOnClickListener {
            findNavController().navigate(R.id.action_country_activity)
        }
    }

    private fun init() {
        daoActivity.showToolBarDetails()
        daoActivity.showButton(true)
        daoActivity.showProgress(true)
        daoActivity.setProgressValue(2)
        daoActivity.setActionEvent(this)
        daoActivity.setToolBarDesc(formatString(R.string.title_lets_get_started))
        validateForm()
    }

    private fun initEventBus() {
        eventBus.inputSyncEvent.flowable.subscribe {
            when (it.eventType) {
                SingleSelectorTypeEnum.SALUTATION.name -> {
                    val selector = JsonHelper.fromJson<Selector>(it.payload)
                    viewModel.salutationInput.onNext(selector)
                }
                InputSyncEvent.ACTION_INPUT_COUNTRY -> {
                    val countryCode = JsonHelper.fromJson<CountryCode>(it.payload)
                    viewModel.countryCodeInput.onNext(countryCode)
                }
            }
        }.addTo(disposables)
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

    private fun showCountryDetails(countryCode: CountryCode?) {
        binding.viewCountryCode.tieCountryCode.setText(countryCode?.callingCode)
        binding.viewCountryCode.imageViewFlag.setImageResource(
            viewUtil.getDrawableById("ic_flag_${countryCode?.code?.toLowerCase()}")
        )
    }

    private fun validateForm() {
        val tieFirstNameObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tieFirstName
        )
        val tieMiddleNameObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tieMiddleName
        )
        val tieLastNameObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tieLastName
        )
        val tieMobileNumberObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tieMobileNumber
        )
        val tieEmailAddressObservable = RxValidator.createFor(binding.tieEmailAddress)
            .nonEmpty(
                String.format(
                    getString(R.string.error_specific_field),
                    binding.tilEmailAddress.hint
                )
            )
            .email(getString(R.string.error_invalid_email_address))
            .onFocusChanged()
            .onValueChanged()
            .toObservable()
            .debounce {
                Observable.timer(
                    resources.getInteger(R.integer.time_edit_text_debounce).toLong(),
                    TimeUnit.MILLISECONDS
                )
            }
        initSetError(tieFirstNameObservable)
        initSetError(tieMiddleNameObservable)
        initSetError(tieLastNameObservable)
        initSetError(tieMobileNumberObservable)
        initSetError(tieEmailAddressObservable)
        RxCombineValidator(
            tieFirstNameObservable,
            tieMiddleNameObservable,
            tieLastNameObservable,
            tieMobileNumberObservable,
            tieEmailAddressObservable
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

    private fun navigateSingleSelector(selectorType: String) {
        val action = DaoWelcomeFragmentDirections.actionSelectorActivity(selectorType)
        findNavController().navigate(action)
    }

    override fun onClickNext() {
        if (viewModel.hasValidForm()) {
            clearFormFocus()
            findNavController().navigate(R.id.action_type_of_business)
        } else {
            showMissingFieldDialog()
            refreshFields()
        }
    }

    private fun refreshFields() {
        binding.tieFirstName.refresh()
        binding.tieMiddleName.refresh()
        binding.tieLastName.refresh()
        binding.tieMobileNumber.refresh()
        binding.tieEmailAddress.refresh()
    }

    override val layoutId: Int
        get() = R.layout.fragment_dao_welcome

    override val viewModelClassType: Class<DaoWelcomeViewModel>
        get() = DaoWelcomeViewModel::class.java
}
