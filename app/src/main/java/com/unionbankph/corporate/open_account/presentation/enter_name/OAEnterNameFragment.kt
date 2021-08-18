package com.unionbankph.corporate.open_account.presentation.enter_name

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
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
import com.unionbankph.corporate.open_account.presentation.OpenAccountActivity
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_oa_enter_name.*

class OAEnterNameFragment :
    BaseFragment<OAEnterNameViewModel>(R.layout.fragment_oa_enter_name) {

    private var enableBackButton = true

    private val openAccountActivity by lazyFast { getAppCompatActivity() as OpenAccountActivity }

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
        buttonNext.setOnClickListener { attemptSubmit() }
    }

    private fun initViewModel() {
        viewModel =
            ViewModelProviders.of(this, viewModelFactory)[OAEnterNameViewModel::class.java]

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
        }
    }

    private fun initViewBinding() {
        viewModel.loadDefaultForm(openAccountActivity.viewModel.defaultForm())
        viewModel.input.firstNameInput
            .subscribe {
                editTextFirstName.setText(it)
            }.addTo(disposables)
        viewModel.input.lastNameInput
            .subscribe {
                editTextLastName.setText(it)
            }.addTo(disposables)
    }

    private fun validateForm() {
        formDisposable.clear()
        val firstNameObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = editTextFirstName
        )
        val lastNameObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = editTextLastName
        )
        initError(firstNameObservable, textViewFirstNameLabel)
        initError(lastNameObservable, textViewLastName)
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
            editTextFirstName.getTextNullable(),
            editTextLastName.getTextNullable()
        )
    }

    private fun refreshFields() {
        editTextFirstName.refresh()
        editTextLastName.refresh()
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
                    true -> ContextCompat.getColor(context!!, R.color.dsColorDarkGray)
                    else -> ContextCompat.getColor(context!!, R.color.colorErrorColor)
                })
            }.addTo(formDisposable)
    }

    private fun setSubmitButtonState(it: Boolean?) {
        buttonNext.isEnabled = it!!
    }

    private fun clearFormFocus() {
        constraintLayout1.post {
            viewUtil.dismissKeyboard(getAppCompatActivity())
            constraintLayout1.requestFocus()
            constraintLayout1.isFocusableInTouchMode = true
        }
    }

    private fun handleOnBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (!enableBackButton) return@addCallback

            enableBackButton = false

            // Temporary: Disable the back press confirmation
            openAccountActivity.popBackStack()
            return@addCallback

            updatePreTextValues()
            if (viewModel.hasFormChanged()) {
                openAccountActivity.showGoBackBottomSheet()
            } else {
                openAccountActivity.popBackStack()
            }

            runPostDelayed({ enableBackButton = true }, 1000)
        }
    }
}