package com.unionbankph.corporate.app.common.widget.validator.validation

import android.widget.EditText
import android.widget.TextView
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.unionbankph.corporate.app.common.widget.validator.*
import io.reactivex.Observable
import java.util.*
import java.util.regex.Pattern

class RxValidator private constructor(private val et: EditText) {

    private val validators = ArrayList<Validator<EditText>>()
    private val externalValidators = ArrayList<Validator<EditText>>()
    private var changeEmitter: Observable<String>? = null

    fun onFocusChanged(): RxValidator {
        this.changeEmitter = RxView.focusChanges(et).skip(1).filter { hasFocus -> !hasFocus }
            .map { aBoolean -> et.text.toString() }
        return this
    }

    fun onValueChanged(): RxValidator {
        this.changeEmitter = RxTextView.textChanges(et).skip(1).map { it.toString() }
        return this
    }

    fun onSubscribe(): RxValidator {
        this.changeEmitter = Observable.create { subscriber ->
            subscriber.onNext(et.text.toString())
            subscriber.onComplete()
        }
        return this
    }

    fun email(): RxValidator {
        this.validators.add(EmailValidator())
        return this
    }

    fun email(invalidEmailMessage: String): RxValidator {
        this.validators.add(EmailValidator(invalidEmailMessage))
        return this
    }

    fun email(invalidEmailMessage: String, pattern: Pattern): RxValidator {
        this.validators.add(EmailValidator(invalidEmailMessage, pattern))
        return this
    }

    fun patternMatches(invalidValueMessage: String, pattern: Pattern): RxValidator {
        this.validators.add(PatternMatchesValidator(invalidValueMessage, pattern))
        return this
    }

    fun patternMatches(invalidValueMessage: String, pattern: String): RxValidator {
        this.validators.add(PatternMatchesValidator(invalidValueMessage, pattern))
        return this
    }

    fun patternFind(invalidValueMessage: String, pattern: Pattern): RxValidator {
        this.validators.add(PatternFindValidator(invalidValueMessage, pattern))
        return this
    }

    fun patternFind(invalidValueMessage: String, pattern: String): RxValidator {
        this.validators.add(PatternFindValidator(invalidValueMessage, pattern))
        return this
    }

    fun nonEmpty(): RxValidator {
        this.validators.add(NonEmptyValidator())
        return this
    }

    fun nonEmpty(cannotBeEmptyMessage: String): RxValidator {
        this.validators.add(NonEmptyValidator(cannotBeEmptyMessage))
        return this
    }

    fun digitOnly(): RxValidator {
        this.validators.add(DigitValidator())
        return this
    }

    fun digitOnly(digitOnlyErrorMessage: String): RxValidator {
        this.validators.add(DigitValidator(digitOnlyErrorMessage))
        return this
    }

    fun minLength(length: Int): RxValidator {
        this.validators.add(MinLengthValidator(length))
        return this
    }

    fun minLength(length: Int, badLengthMessage: String): RxValidator {
        this.validators.add(MinLengthValidator(length, badLengthMessage))
        return this
    }

    fun maxLength(length: Int): RxValidator {
        this.validators.add(MaxLengthValidator(length))
        return this
    }

    fun maxLength(length: Int, badLengthMessage: String): RxValidator {
        this.validators.add(MaxLengthValidator(length, badLengthMessage))
        return this
    }

    fun length(length: Int): RxValidator {
        this.validators.add(LengthValidator(length))
        return this
    }

    fun length(length: Int, badLengthMessage: String): RxValidator {
        this.validators.add(LengthValidator(length, badLengthMessage))
        return this
    }

    fun sameAs(anotherTextView: TextView, message: String): RxValidator {
        this.validators.add(SameAsValidator(anotherTextView, message))
        return this
    }

    fun notSameAs(anotherText: String, message: String): RxValidator {
        this.validators.add(NotSameAsValidator(anotherText, message))
        return this
    }

    fun with(externalValidator: Validator<EditText>): RxValidator {
        this.externalValidators.add(externalValidator)
        return this
    }

    fun toObservable(): Observable<RxValidationResult<EditText>> {
        if (changeEmitter == null) {
            throw ChangeEmitterNotSetException(
                "Change emitter have to be set. Did you forget to set onFocusChanged, onValueChanged or onSubscribe?"
            )
        }

        val validationResultObservable = changeEmitter?.concatMap { s ->
                Observable.fromIterable(validators)
                    .concatMap { validator -> validator.validate(s, et) }
            }!!.buffer(validators.size)
            .map { ValidationResultHelper.getFirstBadResultOrSuccess(it) }

        return if (externalValidators.isEmpty()) {
            validationResultObservable
        } else validationResultObservable.flatMap { result ->
            // if normal validators doesn't found error, launch external one
            if (result.isProper) {
                Observable.fromIterable(externalValidators)
                    .concatMap { validator ->
                        validator.validate(result.validatedText, result.item)
                    }
                    .buffer(externalValidators.size)
                    .map {
                        ValidationResultHelper.getFirstBadResultOrSuccess(it)
                    }
            } else {
                Observable.just(result)
            }
        }
    }

    companion object {

        fun createFor(et: EditText): RxValidator {
            return RxValidator(et)
        }
    }

    data class PatternMatch(
        var invalidValueMessage: String,
        var pattern: Pattern
    )
}
