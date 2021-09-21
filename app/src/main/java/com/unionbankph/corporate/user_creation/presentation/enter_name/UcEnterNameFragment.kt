package com.unionbankph.corporate.user_creation.presentation.enter_name

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.getTextNullable
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.refresh
import com.unionbankph.corporate.app.common.extension.runPostDelayed
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.app.common.widget.validator.validation.RxCombineValidator
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidationResult
import com.unionbankph.corporate.auth.presentation.login.LoginActivity
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.databinding.FragmentUcEnterNameBinding
import com.unionbankph.corporate.user_creation.presentation.UserCreationActivity
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo

class UcEnterNameFragment :
    BaseFragment<FragmentUcEnterNameBinding, UcEnterNameViewModel>() {

    private val openAccountActivity by lazyFast { getAppCompatActivity() as UserCreationActivity }

    private val formDisposable = CompositeDisposable()

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        handleOnBackPressed()
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initViewModel()
    }

    override fun onViewsBound() {
        super.onViewsBound()
        validateForm()
        initViewBinding()
        initOnClicks()
    }

    private fun initOnClicks() {
        binding.buttonNext.setOnClickListener { attemptSubmit() }
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

        viewModel.navigateNextStep.observe(viewLifecycleOwner, EventObserver {
            openAccountActivity.setNameInput(it)
            findNavController().navigate(R.id.action_enter_name_to_tnc_reminder)
        })

        val hasValue = openAccountActivity.viewModel.hasNameInput.hasValue()
        if (hasValue && !viewModel.isLoadedScreen.hasValue()) {
            openAccountActivity.viewModel.nameInput.let {
                viewModel.setExistingNameInput(it)
            }
        } else {
            setSubmitButtonState(false)
        }
    }

    private fun initViewBinding() {
        viewModel.loadDefaultForm(openAccountActivity.getDefaultForm())
        viewModel.input.firstNameInput
            .subscribe {
                binding.editTextFirstName.setText(it)
            }.addTo(disposables)
        viewModel.input.lastNameInput
            .subscribe {
                binding.editTextLastName.setText(it)
            }.addTo(disposables)
    }

    private fun validateForm() {
        formDisposable.clear()
        val firstNameObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            hasSkip = false,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_40),
            editText = binding.editTextFirstName
        )
        val lastNameObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            hasSkip = false,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_40),
            editText = binding.editTextLastName
        )
        initError(firstNameObservable, binding.textViewFirstNameLabel)
        initError(lastNameObservable, binding.textViewLastName)
        RxCombineValidator(
            firstNameObservable,
            lastNameObservable
        )
            .asObservable()
            .distinctUntilChanged()
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .doOnNext {
                viewModel.input.isValidFormInput.onNext(it)
                setSubmitButtonState(it)
            }
            .subscribe()
            .addTo(formDisposable)
    }

    private fun attemptSubmit() {
        clearFormFocus()
        if (viewModel.hasValidForm()) {
            updatePreTextValues()
            viewModel.onClickedNext()
        } else {
            refreshFields()
        }
    }

    private fun updatePreTextValues() {
        viewModel.setPreTextValues(
            binding.editTextFirstName.getTextNullable(),
            binding.editTextLastName.getTextNullable()
        )
    }

    private fun refreshFields() {
        binding.editTextFirstName.refresh()
        binding.editTextLastName.refresh()
    }

    private fun initError(
        textInputEditTextObservable: Observable<RxValidationResult<EditText>>,
        textView: TextView
    ) {
        textInputEditTextObservable
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe {
                viewUtil.setError(it)
                textView.setTextColor(when (it.isProper) {
                    true -> ContextCompat.getColor(requireContext(), R.color.dsColorDarkGray)
                    else -> ContextCompat.getColor(requireContext(), R.color.colorErrorColor)
                })
            }.addTo(formDisposable)
    }

    private fun setSubmitButtonState(it: Boolean?) {
        binding.buttonNext.isEnabled = it!!
    }

    private fun clearFormFocus() {
        binding.constraintLayout1.post {
            viewUtil.dismissKeyboard(getAppCompatActivity())
            binding.constraintLayout1.requestFocus()
            binding.constraintLayout1.isFocusableInTouchMode = true
        }
    }

    private fun handleOnBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            openAccountActivity.popBackStack()
        }
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentUcEnterNameBinding
        get() = FragmentUcEnterNameBinding::inflate

    override val viewModelClassType: Class<UcEnterNameViewModel>
        get() = UcEnterNameViewModel::class.java
}