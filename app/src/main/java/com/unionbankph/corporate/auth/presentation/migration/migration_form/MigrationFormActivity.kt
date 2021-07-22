package com.unionbankph.corporate.auth.presentation.migration.migration_form

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxbinding2.view.RxView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.enableButton
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.makeLinks
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.edittext.ImeOptionEditText
import com.unionbankph.corporate.app.common.widget.validator.validation.RxCombineValidator
import com.unionbankph.corporate.auth.data.form.LoginEBankingMigrationForm
import com.unionbankph.corporate.auth.data.form.LoginECreditingMigrationForm
import com.unionbankph.corporate.auth.data.model.ECredLoginDto
import com.unionbankph.corporate.auth.presentation.login.LoginActivity
import com.unionbankph.corporate.auth.presentation.migration.*
import com.unionbankph.corporate.auth.presentation.otp.OTPActivity
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.databinding.ActivityMigrationFormBinding
import com.unionbankph.corporate.settings.presentation.learn_more.LearnMoreActivity
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit

class MigrationFormActivity :
    BaseActivity<ActivityMigrationFormBinding, MigrationViewModel>() {

    private lateinit var imeOptionEditText: ImeOptionEditText

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initTransparency()
        setMargins(
            binding.textViewLearnMore,
            0,
            0,
            0,
            getNavHeight() + resources.getDimension(R.dimen.content_spacing).toInt()
        )
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowMigrationLoading -> {
                    showProgressAlertDialog(MigrationFormActivity::class.java.simpleName)
                }
                is ShowMigrationDismissLoading -> {
                    dismissProgressAlertDialog()
                }
                is ShowMigrationLogin -> {
                    navigateMigrationWelcomeScreen(it)
                }
                is ShowMigrationECredLogin -> {
                    navigateMigrationOTPScreen(it.eCredLoginDto)
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
        if (intent.getStringExtra(EXTRA_SCREEN) == SCREEN_ECREDITING) {
            binding.textViewDesc.text = formatString(
                if (isSME) {
                    R.string.desc_migration_form_e_crediting_sme
                } else {
                    R.string.desc_migration_form_e_crediting
                }
            )
            binding.textInputLayoutCorpId.visibility(false)
            Handler().post {
                binding.editTextCorpId.setText(Constant.EMPTY)
            }
        } else {
            binding.textViewDesc.text = formatString(
                if (isSME) {
                    R.string.desc_migration_form_e_banking_sme
                } else {
                    R.string.desc_migration_form_e_banking
                }
            )
            binding.textInputLayoutCorpId.visibility(true)
            binding.editTextCorpId.text?.clear()
        }
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initEventBus()
        imeOptionEditText =
            ImeOptionEditText()
        imeOptionEditText.addEditText(binding.editTextCorpId, binding.editTextUserId, binding.editTextPassword)
        imeOptionEditText.startListener()
        binding.textViewMigration.makeLinks(
            Pair(getString(R.string.action_here), View.OnClickListener {
                navigator.navigateClearUpStack(
                    this,
                    LoginActivity::class.java,
                    null,
                    isClear = true,
                    isAnimated = true
                )
            })
        )
        binding.textViewMigration.setOnClickListener {
            onBackPressed()
        }
        binding.buttonNext.setOnClickListener {
            if (intent.getStringExtra(EXTRA_SCREEN) == SCREEN_ECREDITING) {
                viewModel.loginECreditingMigration(
                    LoginECreditingMigrationForm(
                        username = binding.editTextUserId.text.toString().trim(),
                        password = binding.editTextPassword.text.toString().trim()
                    )
                )
            } else {
                viewModel.loginEBankingMigration(
                    LoginEBankingMigrationForm(
                        corpId = binding.editTextCorpId.text.toString().trim(),
                        userId = binding.editTextUserId.text.toString().trim(),
                        password = binding.editTextPassword.text.toString().trim()
                    )
                )
            }
        }
        binding.textViewLearnMore.setOnClickListener {
            initClickTextViewLearnMore()
        }
    }

    private fun initEventBus() {
        eventBus.actionSyncEvent.flowable.subscribe {
            if (it.eventType == ActionSyncEvent.ACTION_REFRESH_MIGRATION_FORM_SCREEN) {
                binding.editTextCorpId.text?.clear()
                binding.editTextUserId.text?.clear()
                binding.editTextPassword.text?.clear()
                binding.constraintLayout.requestFocus()
                binding.constraintLayout.isFocusableInTouchMode = true
            }
        }.addTo(disposables)
    }

    private fun validateForm() {
        val corpIdObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field),
            editText = binding.editTextCorpId
        )
        val userIdObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field),
            editText = binding.editTextUserId
        )
        val passwordObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field),
            editText = binding.editTextPassword
        )
        RxCombineValidator(corpIdObservable, userIdObservable, passwordObservable)
            .asObservable()
            .distinctUntilChanged()
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe {
                binding.buttonNext.enableButton(it)
            }.addTo(disposables)
    }

    private fun initClickTextViewLearnMore() {
        RxView.clicks(binding.textViewLearnMore)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                navigator.navigate(
                    this,
                    LearnMoreActivity::class.java,
                    null,
                    isClear = false,
                    isAnimated = true,
                    transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_UP
                )
            }.addTo(disposables)
    }

    private fun navigateMigrationWelcomeScreen(it: ShowMigrationLogin) {
        val bundle = Bundle().apply {
            putString(
                MigrationMainActivity.EXTRA_DATA,
                JsonHelper.toJson(it.loginMigrationDto)
            )
            putString(
                MigrationMainActivity.EXTRA_TYPE,
                intent.getStringExtra(EXTRA_SCREEN)
            )
            putString(
                MigrationMainActivity.EXTRA_USER_ID,
                binding.editTextUserId.text.toString()
            )
        }
        navigator.navigate(
            this,
            MigrationMainActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun navigateMigrationOTPScreen(eCredLoginDto: ECredLoginDto) {
        val bundle = Bundle().apply {
            putString(
                OTPActivity.EXTRA_REQUEST,
                JsonHelper.toJson(eCredLoginDto)
            )
            putString(
                OTPActivity.EXTRA_REQUEST_PAGE,
                OTPActivity.PAGE_MIGRATION
            )
            putString(
                OTPActivity.EXTRA_USER_ID,
                binding.editTextUserId.text.toString()
            )
        }
        navigator.navigate(
            this,
            OTPActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    companion object {
        const val EXTRA_SCREEN = "screen"
        const val SCREEN_ECREDITING = "ecrediting"
        const val SCREEN_EBANKING = "ebanking"
    }

    override val viewModelClassType: Class<MigrationViewModel>
        get() = MigrationViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityMigrationFormBinding
        get() = ActivityMigrationFormBinding::inflate
}
