package com.unionbankph.corporate.open_account.presentation.nominate_password

import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.widget.validator.validation.RxCombineValidator
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidationResult
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidator
import com.unionbankph.corporate.app.util.ViewUtil
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_nominate_password.*
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

        val emptyObservable = RxValidator.createFor(editTextConfirmPassword)
            .nonEmpty(getString(R.string.error_this_field))
            .sameAs(editTextPassword, getString(R.string.error_compare_password))
            .onValueChanged()
            .toObservable()
            .debounce {
                Observable.timer(
                    resources.getInteger(R.integer.time_edit_text_debounce_password).toLong(),
                    TimeUnit.MILLISECONDS
                )
            }

        val lengthObservable = RxValidator.createFor(textInputEditTextPassword)
            .minLength(8)
            .maxLength(20)
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
            .skip(1)
            .distinctUntilChanged()
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe { isProper ->
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
                imageView.setImageResource(
                    when (it.isProper) {
                        true -> R.drawable.circle_green
                        else -> R.drawable.circle_red_badge
                    }
                )
            }.addTo(formDisposable)
    }
}