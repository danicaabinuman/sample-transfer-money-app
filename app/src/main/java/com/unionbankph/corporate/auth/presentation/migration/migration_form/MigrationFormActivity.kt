package com.unionbankph.corporate.auth.presentation.migration.migration_form

import android.os.Bundle
import android.os.Handler
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
import com.unionbankph.corporate.settings.presentation.learn_more.LearnMoreActivity
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_migration_form.*
import java.util.concurrent.TimeUnit

class MigrationFormActivity : BaseActivity<MigrationViewModel>(R.layout.activity_migration_form) {

    private lateinit var imeOptionEditText: ImeOptionEditText

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initTransparency()
        setMargins(
            textViewLearnMore,
            0,
            0,
            0,
            getNavHeight() + resources.getDimension(R.dimen.content_spacing).toInt()
        )
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel = ViewModelProviders.of(this, viewModelFactory)[MigrationViewModel::class.java]
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
            textViewDesc.text = formatString(
                if (isSME) {
                    R.string.desc_migration_form_e_crediting_sme
                } else {
                    R.string.desc_migration_form_e_crediting
                }
            )
            textInputLayoutCorpId.visibility(false)
            Handler().post {
                editTextCorpId.setText(Constant.EMPTY)
            }
        } else {
            textViewDesc.text = formatString(
                if (isSME) {
                    R.string.desc_migration_form_e_banking_sme
                } else {
                    R.string.desc_migration_form_e_banking
                }
            )
            textInputLayoutCorpId.visibility(true)
            editTextCorpId.text?.clear()
        }
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initEventBus()
        imeOptionEditText =
            ImeOptionEditText()
        imeOptionEditText.addEditText(editTextCorpId, editTextUserId, editTextPassword)
        imeOptionEditText.startListener()
        textViewMigration.makeLinks(
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
        textViewMigration.setOnClickListener {
            onBackPressed()
        }
        buttonNext.setOnClickListener {
            if (intent.getStringExtra(EXTRA_SCREEN) == SCREEN_ECREDITING) {
                viewModel.loginECreditingMigration(
                    LoginECreditingMigrationForm(
                        username = editTextUserId.text.toString().trim(),
                        password = editTextPassword.text.toString().trim()
                    )
                )
            } else {
                viewModel.loginEBankingMigration(
                    LoginEBankingMigrationForm(
                        corpId = editTextCorpId.text.toString().trim(),
                        userId = editTextUserId.text.toString().trim(),
                        password = editTextPassword.text.toString().trim()
                    )
                )
            }
        }
        textViewLearnMore.setOnClickListener {
            initClickTextViewLearnMore()
        }
    }

    private fun initEventBus() {
        eventBus.actionSyncEvent.flowable.subscribe {
            if (it.eventType == ActionSyncEvent.ACTION_REFRESH_MIGRATION_FORM_SCREEN) {
                editTextCorpId.text?.clear()
                editTextUserId.text?.clear()
                editTextPassword.text?.clear()
                constraintLayout.requestFocus()
                constraintLayout.isFocusableInTouchMode = true
            }
        }.addTo(disposables)
    }

    private fun validateForm() {
        val corpIdObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field),
            editText = editTextCorpId
        )
        val userIdObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field),
            editText = editTextUserId
        )
        val passwordObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field),
            editText = editTextPassword
        )
        RxCombineValidator(corpIdObservable, userIdObservable, passwordObservable)
            .asObservable()
            .distinctUntilChanged()
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe {
                buttonNext.enableButton(it)
            }.addTo(disposables)
    }

    private fun initClickTextViewLearnMore() {
        RxView.clicks(textViewLearnMore)
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
                editTextUserId.text.toString()
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
                editTextUserId.text.toString()
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
}
