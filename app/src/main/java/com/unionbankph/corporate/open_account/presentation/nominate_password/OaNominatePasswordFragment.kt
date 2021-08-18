package com.unionbankph.corporate.open_account.presentation.nominate_password

import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.convertToDP
import com.unionbankph.corporate.app.common.widget.dialog.NewConfirmationBottomSheet
import com.unionbankph.corporate.app.common.widget.validator.validation.RxCombineValidator
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidationResult
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidator
import com.unionbankph.corporate.app.util.ViewUtil
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_oa_nominate_password.*
import kotlinx.android.synthetic.main.fragment_oa_nominate_password.buttonNext
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class OaNominatePasswordFragment :
    BaseFragment<OaNominatePasswordViewModel>(R.layout.fragment_oa_nominate_password) {

    private val formDisposable = CompositeDisposable()

    override fun onViewsBound() {
        super.onViewsBound()
        validateForm()
        initListeners()
    }

    private fun initListeners() {
        buttonNext.setOnClickListener {
            displaySuccessBottomSheet()
        }
    }

    private fun validateForm() {
        formDisposable.clear()

        val emptyObservable = RxValidator.createFor(textInputEditTextPassword)
            .nonEmpty(getString(R.string.error_password_validation_message))
            .onValueChanged()
            .toObservable()
            .debounce {
                Observable.timer(
                    resources.getInteger(R.integer.time_edit_text_debounce_password).toLong(),
                    TimeUnit.MILLISECONDS
                )
            }

        val lengthObservable = RxValidator.createFor(textInputEditTextPassword)
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

        val upperCaseObservable = RxValidator.createFor(textInputEditTextPassword)
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

        val numberObservable = RxValidator.createFor(textInputEditTextPassword)
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

        val symbolObservable = RxValidator.createFor(textInputEditTextPassword)
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

        initError(lengthObservable, imageViewBullet1)
        initError(upperCaseObservable, imageViewBullet2)
        initError(numberObservable, imageViewBullet3)
        initError(symbolObservable, imageViewBullet4)
        initError(emptyObservable, imageViewBullet5)

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
                buttonNext.isEnabled = isProper

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
                    val widthHeight = SUCCESS_INDICATOR_DIMEN.convertToDP(context!!)
                    imageView.apply {
                        setImageResource(R.drawable.ic_check_circle_green)
                        layoutParams.height = widthHeight
                        layoutParams.width = widthHeight
                    }
                } else {
                    val widthHeight = ERROR_INDICATOR_DIMEN.convertToDP(context!!)
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
            inputLayoutPassword.isErrorEnabled = false
            inputLayoutPassword.error = null
        } else {
            inputLayoutPassword.isErrorEnabled = false
            inputLayoutPassword.error = getString(R.string.error_password_validation_message)
        }

        textViewPasswordRequirementLabel.visibility = View.GONE
        textViewPasswordLabel.setTextColor(when (isProper) {
            true -> ContextCompat.getColor(context!!, R.color.dsColorDarkGray)
            else -> ContextCompat.getColor(context!!, R.color.colorErrorColor)
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
        bottomSheet.show(childFragmentManager, NewConfirmationBottomSheet.TAG)
    }

    companion object {
        const val ERROR_INDICATOR_DIMEN = 6
        const val SUCCESS_INDICATOR_DIMEN = 12
    }
}