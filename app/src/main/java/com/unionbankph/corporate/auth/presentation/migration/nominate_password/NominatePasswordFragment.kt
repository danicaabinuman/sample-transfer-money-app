package com.unionbankph.corporate.auth.presentation.migration.nominate_password

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.enableButton
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.widget.validator.validation.RxCombineValidator
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidator
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.auth.data.form.ECredForm
import com.unionbankph.corporate.auth.data.form.MigrationNominatePasswordForm
import com.unionbankph.corporate.auth.presentation.migration.MigrationMainActivity
import com.unionbankph.corporate.auth.presentation.migration.MigrationViewModel
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationDismissLoading
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationError
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationLoading
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationNominatePassword
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationSaveECredPayload
import com.unionbankph.corporate.databinding.FragmentNominatePasswordBinding
import io.reactivex.Observable
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class NominatePasswordFragment : BaseFragment<FragmentNominatePasswordBinding, MigrationViewModel>() {

    private val migrationMainActivity by lazyFast { (activity as MigrationMainActivity) }

    private val loginMigrationDto by lazyFast { migrationMainActivity.getLoginMigrationInfo() }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowMigrationLoading -> {
                    showProgressAlertDialog(
                        NominatePasswordFragment::class.java.simpleName
                    )
                }
                is ShowMigrationDismissLoading -> {
                    dismissProgressAlertDialog()
                }
                is ShowMigrationNominatePassword -> {
                    navigateNominateMobileNumber()
                }
                is ShowMigrationSaveECredPayload -> {
                    navigateNominateMobileNumber()
                }
                is ShowMigrationError -> {
                    handleOnError(it.throwable)
                }
            }
        })
    }

    override fun onViewsBound() {
        super.onViewsBound()
        validateForm()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initEventBus()
        binding.buttonNext.setOnClickListener {
            if (migrationMainActivity.getType() == MigrationMainActivity.TYPE_ECREDITING) {
                viewModel.saveECredPayload(
                    ECredForm(
                        password = binding.editTextPassword.text.toString()
                            .trim()
                    )
                )
            } else {
                viewModel.nominatePasswordMigration(
                    loginMigrationDto.temporaryCorporateUserId.notNullable(),
                    MigrationNominatePasswordForm(
                        binding.editTextPassword.text.toString().trim(),
                        loginMigrationDto.migrationToken
                    )
                )
            }
        }
    }

    private fun initEventBus() {
        eventBus.actionSyncEvent.flowable.subscribe {
            if (it.eventType == ActionSyncEvent.ACTION_UPDATE_EMAIL_MIGRATION) {
                binding.tvPasswordDesc.text = Html.fromHtml(
                    String.format(
                        getString(R.string.param_msg_nominate_password),
                        it.payload!!
                    )
                )
            }
        }.addTo(disposables)
    }

    private fun validateForm() {

        val passwordObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = 8,
            maxLength = 30,
            editText = binding.editTextPassword
        )

        val cPasswordObservable = RxValidator.createFor(binding.editTextConfirmPassword)
            .nonEmpty(getString(R.string.error_this_field))
            .sameAs(binding.editTextPassword, getString(R.string.error_compare_password))
            .onValueChanged()
            .toObservable()
            .debounce {
                Observable.timer(
                    resources.getInteger(R.integer.time_edit_text_debounce_password).toLong(),
                    TimeUnit.MILLISECONDS
                )
            }

        val passwordLimitObservable = RxValidator.createFor(binding.editTextPassword)
            .minLength(8)
            .maxLength(30)
            .onValueChanged()
            .toObservable()
            .debounce {
                Observable.timer(
                    resources.getInteger(R.integer.time_edit_text_debounce_password).toLong(),
                    TimeUnit.MILLISECONDS
                )
            }

        val passwordAlphaObservable = RxValidator.createFor(binding.editTextPassword)
            .patternMatches(
                getString(R.string.error_no_aplha),
                Pattern.compile(ViewUtil.REGEX_FORMAT_HAS_ALPHA)
            )
            .onValueChanged()
            .toObservable()
            .debounce {
                Observable.timer(
                    resources.getInteger(R.integer.time_edit_text_debounce_password).toLong(),
                    TimeUnit.MILLISECONDS
                )
            }

        val passwordNumericObservable = RxValidator.createFor(binding.editTextPassword)
            .patternMatches(
                getString(R.string.error_no_number),
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

        val passwordSymbolObservable = RxValidator.createFor(binding.editTextPassword)
            .patternMatches(
                getString(R.string.error_no_symbol),
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

        passwordObservable
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe { viewUtil.setError(it) }
            .addTo(disposables)

        cPasswordObservable
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe { viewUtil.setError(it) }
            .addTo(disposables)

        passwordLimitObservable
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe { viewUtil.setPasswordConfirmError(it, binding.viewPasswordConfirmation.imageViewPassword1) }
            .addTo(disposables)

        passwordAlphaObservable
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe { viewUtil.setPasswordConfirmError(it, binding.viewPasswordConfirmation.imageViewPassword2) }
            .addTo(disposables)

        passwordNumericObservable
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe { viewUtil.setPasswordConfirmError(it, binding.viewPasswordConfirmation.imageViewPassword3) }
            .addTo(disposables)

        passwordSymbolObservable
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe { viewUtil.setPasswordConfirmError(it, binding.viewPasswordConfirmation.imageViewPassword4) }
            .addTo(disposables)

        RxCombineValidator(
            passwordObservable,
            cPasswordObservable,
            passwordLimitObservable,
            passwordAlphaObservable,
            passwordNumericObservable,
            passwordSymbolObservable
        )
            .asObservable()
            .distinctUntilChanged()
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe {
                binding.buttonNext.enableButton(it)
            }.addTo(disposables)
    }

    private fun navigateNominateMobileNumber() {
        viewUtil.dismissKeyboard(migrationMainActivity)
        migrationMainActivity.getViewPager().currentItem =
            migrationMainActivity.getViewPager().currentItem.plus(1)
    }

    companion object {
        fun newInstance(): NominatePasswordFragment {
            val fragment =
                NominatePasswordFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }

    override val viewModelClassType: Class<MigrationViewModel>
        get() = MigrationViewModel::class.java

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentNominatePasswordBinding
        get() = FragmentNominatePasswordBinding::inflate
}
