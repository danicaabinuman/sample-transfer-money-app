package com.unionbankph.corporate.user_creation.presentation.nominate_password

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.convertToDP
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.app.common.widget.dialog.NewConfirmationBottomSheet
import com.unionbankph.corporate.app.common.widget.validator.validation.RxCombineValidator
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidationResult
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidator
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.auth.presentation.login.LoginActivity
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.databinding.FragmentUcNominatePasswordBinding
import com.unionbankph.corporate.user_creation.presentation.UserCreationActivity
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class UcNominatePasswordFragment :
    BaseFragment<FragmentUcNominatePasswordBinding, UcNominatePasswordViewModel>() {

    private val formDisposable = CompositeDisposable()

    private val userCreationActivity by lazyFast { getAppCompatActivity() as UserCreationActivity }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)

        handleOnBackPressed()
    }

    private fun handleOnBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            userCreationActivity.popBackStack()
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

        viewModel.navigateResult.observe(viewLifecycleOwner, EventObserver {
            displaySuccessBottomSheet()
        })
    }

    override fun onViewsBound() {
        super.onViewsBound()
        validateForm()
        initListeners()
    }

    private fun initListeners() {
        binding.buttonNext.setOnClickListener {
            viewModel.onClickedNext(
                binding.textInputEditTextPassword.text.toString(),
                userCreationActivity.getOTPSuccessToken()
            )
        }
    }

    private fun validateForm() {
        formDisposable.clear()

        val emptyObservable = RxValidator.createFor(binding.textInputEditTextPassword)
            .nonEmpty(getString(R.string.error_password_validation_message))
            .onValueChanged()
            .toObservable()
            .debounce {
                Observable.timer(
                    resources.getInteger(R.integer.time_edit_text_debounce_password).toLong(),
                    TimeUnit.MILLISECONDS
                )
            }

        val lengthObservable = RxValidator.createFor(binding.textInputEditTextPassword)
            .minLength(resources.getInteger(R.integer.min_length_nominate_password_field))
            .maxLength(resources.getInteger(R.integer.max_length_nominate_password_field))
            .onValueChanged()
            .toObservable()
            .debounce {
                Observable.timer(
                    resources.getInteger(R.integer.time_edit_text_debounce_password).toLong(),
                    TimeUnit.MILLISECONDS
                )
            }

        val upperCaseObservable = RxValidator.createFor(binding.textInputEditTextPassword)
            .patternMatches(
                getString(R.string.error_password_validation_message),
                Pattern.compile(ViewUtil.REGEX_FORMAT_HAS_ALPHA_UPPERCASE))
            .onValueChanged()
            .toObservable()
            .debounce {
                Observable.timer(
                    resources.getInteger(R.integer.time_edit_text_debounce_password).toLong(),
                    TimeUnit.MILLISECONDS
                )
            }

        val numberObservable = RxValidator.createFor(binding.textInputEditTextPassword)
            .patternMatches(
                getString(R.string.error_password_validation_message),
                Pattern.compile(ViewUtil.REGEX_FORMAT_HAS_NUMBER)
            )
            .onValueChanged()
            .toObservable()
            .debounce {
                Observable.timer(
                    resources.getInteger(R.integer.time_edit_text_debounce_password).toLong(),
                    TimeUnit.MILLISECONDS
                )
            }

        val symbolObservable = RxValidator.createFor(binding.textInputEditTextPassword)
            .patternFind(
                getString(R.string.error_password_validation_message),
                Pattern.compile(ViewUtil.REGEX_FORMAT_HAS_SYMBOL)
            )
            .onValueChanged()
            .toObservable()
            .debounce {
                Observable.timer(
                    resources.getInteger(R.integer.time_edit_text_debounce_password).toLong(),
                    TimeUnit.MILLISECONDS
                )
            }

        initError(lengthObservable, binding.imageViewBullet1)
        initError(upperCaseObservable, binding.imageViewBullet2)
        initError(numberObservable, binding.imageViewBullet3)
        initError(symbolObservable, binding.imageViewBullet4)
        initError(emptyObservable, binding.imageViewBullet5)

        RxCombineValidator(
            lengthObservable,
            upperCaseObservable,
            numberObservable,
            symbolObservable,
            emptyObservable
        )
            .asObservable()
            .distinctUntilChanged()
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe { isProper ->
                binding.buttonNext.isEnabled = isProper

                setErrorLabels(isProper)
            }
            .addTo(disposables)
    }

    private fun initError(
        textInputEditTextObservable: Observable<RxValidationResult<EditText>>,
        imageView: ImageView
    ) {
        textInputEditTextObservable
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe {

                imageView.requestLayout()
                if (it.isProper) {
                    val widthHeight = SUCCESS_INDICATOR_DIMEN.convertToDP(requireContext())
                    imageView.apply {
                        setImageResource(R.drawable.ic_check_circle_green)
                        layoutParams.height = widthHeight
                        layoutParams.width = widthHeight
                    }
                } else {
                    val widthHeight = ERROR_INDICATOR_DIMEN.convertToDP(requireContext())
                    imageView.apply {
                        setImageResource(R.drawable.circle_red_badge)
                        layoutParams.height = widthHeight
                        layoutParams.width = widthHeight
                    }
                }
            }.addTo(formDisposable)
    }

    private fun setErrorLabels(isProper: Boolean) {
        if (isProper) {
            binding.inputLayoutPassword.isErrorEnabled = false
            binding.inputLayoutPassword.error = null
        } else {
            binding.inputLayoutPassword.isErrorEnabled = false
            binding.inputLayoutPassword.error = getString(R.string.error_password_validation_message)
        }

        binding.textViewPasswordRequirementLabel.visibility = View.GONE
        binding.textViewPasswordLabel.setTextColor(when (isProper) {
            true -> ContextCompat.getColor(requireContext(), R.color.dsColorDarkGray)
            else -> ContextCompat.getColor(requireContext(), R.color.colorErrorColor)
        })
    }

    private fun displaySuccessBottomSheet() {
        val bottomSheet = NewConfirmationBottomSheet.newInstance(
            iconResource = R.drawable.bg_user_creation_success,
            title = getString(R.string.user_creation_success_title),
            description = getString(R.string.user_creation_success_desc),
            positiveButtonText = getString(R.string.action_personalise_experience),
            isCancelable = false,
            gravity = NewConfirmationBottomSheet.GRAVITY_CENTER
        )
        bottomSheet.setCallback( onPositiveClick = {
            findNavController().navigate(R.id.action_nominate_to_permission_settings)
        })
        bottomSheet.show(childFragmentManager, NewConfirmationBottomSheet.TAG)
    }

    companion object {
        const val ERROR_INDICATOR_DIMEN = 6
        const val SUCCESS_INDICATOR_DIMEN = 12
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentUcNominatePasswordBinding
        get() = FragmentUcNominatePasswordBinding::inflate

    override val viewModelClassType: Class<UcNominatePasswordViewModel>
        get() = UcNominatePasswordViewModel::class.java
}