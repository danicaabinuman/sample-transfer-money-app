package com.unionbankph.corporate.open_account.presentation.nominate_password

import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.convertToDP
import com.unionbankph.corporate.app.common.widget.validator.validation.RxCombineValidator
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidationResult
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidator
import com.unionbankph.corporate.app.util.ViewUtil
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_oa_nominate_password.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class OaNominatePasswordFragment :
    BaseFragment<OaNominatePasswordViewModel>(R.layout.fragment_oa_nominate_password) {

    private val formDisposable = CompositeDisposable()

    override fun onViewsBound() {
        super.onViewsBound()
        validateForm()
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

        val lengthObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_nominate_password_field),
            maxLength = resources.getInteger(R.integer.max_length_nominate_password_field),
            editText = textInputEditTextPassword,
            customErrorMessage = getString(R.string.error_password_validation_message)
        )

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
            .skip(1)
            .distinctUntilChanged()
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe { isProper ->
                textViewPasswordRequirementLabel.visibility = View.GONE
                textViewPasswordLabel.setTextColor(when (isProper) {
                    true -> ContextCompat.getColor(context!!, R.color.dsColorDarkGray)
                    else -> ContextCompat.getColor(context!!, R.color.colorErrorColor)
                })
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
                viewUtil.setError(it)

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

    companion object {
        const val ERROR_INDICATOR_DIMEN = 6
        const val SUCCESS_INDICATOR_DIMEN = 12
    }
}