package com.unionbankph.corporate.auth.presentation.migration.nominate_email

import android.os.Bundle
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
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.validator.validation.RxCombineValidator
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidator
import com.unionbankph.corporate.auth.data.constant.ECredStatus
import com.unionbankph.corporate.auth.data.form.ECredForm
import com.unionbankph.corporate.auth.data.form.MigrationNominateEmailForm
import com.unionbankph.corporate.auth.data.model.LoginMigrationDto
import com.unionbankph.corporate.auth.presentation.migration.MigrationMainActivity
import com.unionbankph.corporate.auth.presentation.migration.MigrationViewModel
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationDismissLoading
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationError
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationLoading
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationNominateEmail
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationNominateEmailECred
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationSaveECredPayload
import com.unionbankph.corporate.auth.presentation.migration.migration_merge.MigrationMergeActivity
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.databinding.FragmentNominateEmailBinding
import io.reactivex.Observable
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit

class NominateEmailFragment :
    BaseFragment<FragmentNominateEmailBinding, MigrationViewModel>() {

    private val migrationMainActivity by lazyFast { (activity as MigrationMainActivity) }

    private val loginMigrationDto by lazyFast { migrationMainActivity.getLoginMigrationInfo() }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowMigrationLoading -> {
                    showProgressAlertDialog(
                        NominateEmailFragment::class.java.simpleName
                    )
                }
                is ShowMigrationDismissLoading -> {
                    dismissProgressAlertDialog()
                }
                is ShowMigrationNominateEmail -> {
                    if (it.loginMigrationDto.corpId != null && it.loginMigrationDto.userId != null) {
                        navigateMigrationMergeScreen(it.loginMigrationDto)
                    } else {
                        navigateNominatePassword()
                    }
                }
                is ShowMigrationNominateEmailECred -> {
                    val eCredStatus =
                        enumValueOf<ECredStatus>(it.nominateEmailDto.status.notNullable())
                    if (eCredStatus == ECredStatus.NEW_ECRED_MIGRATION) {
                        viewModel.saveECredPayload(
                            ECredForm(emailAddress = binding.editTextEmailAddress.text.toString())
                        )
                    } else if (eCredStatus == ECredStatus.FOR_ECRED_MERGING) {
                        val loginMigrationDto = LoginMigrationDto().apply {
                            emailAddress = binding.editTextEmailAddress.text.toString()
                            userId = migrationMainActivity.intent.getStringExtra(
                                MigrationMainActivity.EXTRA_USER_ID
                            )
                        }
                        navigateMigrationMergeScreen(loginMigrationDto)
                    }
                }
                is ShowMigrationSaveECredPayload -> {
                    navigateNominatePassword()
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
        binding.buttonNext.setOnClickListener {
            if (migrationMainActivity.getType() == MigrationMainActivity.TYPE_ECREDITING) {
                viewModel.nominateECreditingEmailAddress(
                    migrationMainActivity.getAccessToken(),
                    binding.editTextEmailAddress.text.toString().trim()
                )
            } else {
                viewModel.nominateEmailMigration(
                    loginMigrationDto.temporaryCorporateUserId!!,
                    MigrationNominateEmailForm(
                        binding.editTextEmailAddress.text.toString().trim(),
                        loginMigrationDto.migrationToken
                    )
                )
            }
        }
    }

    private fun validateForm() {
        val emailObservable = RxValidator.createFor(binding.editTextEmailAddress)
            .nonEmpty(
                String.format(
                    getString(R.string.error_specific_field),
                    binding.textInputLayoutEmailAddress.hint
                )
            )
            .email(getString(R.string.error_invalid_email_address))
            .onValueChanged()
            .toObservable()
            .debounce {
                Observable.timer(
                    resources.getInteger(R.integer.time_edit_text_debounce).toLong(),
                    TimeUnit.MILLISECONDS
                )
            }

        emailObservable
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe { viewUtil.setError(it) }
            .addTo(disposables)

        RxCombineValidator(emailObservable)
            .asObservable()
            .distinctUntilChanged()
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe {
                binding.buttonNext.enableButton(it)
            }.addTo(disposables)
    }

    private fun navigateNominatePassword() {
        migrationMainActivity.getViewPager().currentItem =
            migrationMainActivity.getViewPager().currentItem.plus(1)
        eventBus.actionSyncEvent.emmit(
            BaseEvent(
                ActionSyncEvent.ACTION_UPDATE_EMAIL_MIGRATION,
                binding.editTextEmailAddress.text.toString()
            )
        )
    }

    private fun navigateMigrationMergeScreen(loginMigrationDto: LoginMigrationDto) {
        val bundle = Bundle().apply {
            putString(
                MigrationMergeActivity.EXTRA_LOGIN_MIGRATION_DTO,
                JsonHelper.toJson(loginMigrationDto)
            )
            putString(
                MigrationMergeActivity.EXTRA_TYPE, migrationMainActivity.getType()
            )
            putString(
                MigrationMergeActivity.EXTRA_ACCESS_TOKEN, migrationMainActivity.getAccessToken()
            )
        }
        navigator.navigate(
            (activity as MigrationMainActivity),
            MigrationMergeActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_UP
        )
    }

    companion object {
        fun newInstance(): NominateEmailFragment {
            val fragment =
                NominateEmailFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }

    override val viewModelClassType: Class<MigrationViewModel>
        get() = MigrationViewModel::class.java

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentNominateEmailBinding
        get() = FragmentNominateEmailBinding::inflate
}
