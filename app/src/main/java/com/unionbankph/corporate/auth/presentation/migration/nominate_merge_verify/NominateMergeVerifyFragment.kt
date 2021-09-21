package com.unionbankph.corporate.auth.presentation.migration.nominate_merge_verify

import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.enableButton
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.toHtmlSpan
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.widget.validator.validation.RxCombineValidator
import com.unionbankph.corporate.auth.data.form.ECredMergeAccountForm
import com.unionbankph.corporate.auth.data.form.MigrationMergeAccountForm
import com.unionbankph.corporate.auth.data.model.LoginMigrationDto
import com.unionbankph.corporate.auth.presentation.migration.MigrationViewModel
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationDismissLoading
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationECredMergeAccount
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationError
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationLoading
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationMergeAccount
import com.unionbankph.corporate.auth.presentation.migration.migration_merge.MigrationMergeActivity
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.databinding.FragmentNominateEmailTakenVerifyBinding
import io.reactivex.rxkotlin.addTo

class NominateMergeVerifyFragment :
    BaseFragment<FragmentNominateEmailTakenVerifyBinding, MigrationViewModel>() {

    private val migrationMergeActivity by lazyFast { (activity as MigrationMergeActivity) }

    private val loginMigrationDto by lazyFast { migrationMergeActivity.getLoginMigrationMergeInfo() }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowMigrationLoading -> {
                    showProgressAlertDialog(this::class.java.simpleName)
                }
                is ShowMigrationECredMergeAccount -> {
                    eventBus.actionSyncEvent.emmit(
                        BaseEvent(
                            ActionSyncEvent.ACTION_UPDATE_AUTH_MIGRATION_MERGE,
                            JsonHelper.toJson(it.eCredMergeSubmitDto)
                        )
                    )
                    navigateNextScreen()
                }
                is ShowMigrationMergeAccount -> {
                    navigateNextScreen()
                }
                is ShowMigrationDismissLoading -> {
                    dismissProgressAlertDialog()
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
        initView(loginMigrationDto)
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        binding.buttonContinue.setOnClickListener {
            if (migrationMergeActivity.getType() == MigrationMergeActivity.TYPE_ECREDITING) {
                viewModel.mergeECredAccount(
                    migrationMergeActivity.getAccessToken(),
                    ECredMergeAccountForm(
                        username = loginMigrationDto.emailAddress,
                        password = binding.editTextPassword.text.toString()
                    )
                )
            } else {
                val migrationMergeAccountForm =
                    MigrationMergeAccountForm()
                migrationMergeAccountForm.corpId = loginMigrationDto.corpId
                migrationMergeAccountForm.userId = loginMigrationDto.userId
                migrationMergeAccountForm.emailAddress = loginMigrationDto.emailAddress
                migrationMergeAccountForm.password = binding.editTextPassword.text.toString().trim()
                migrationMergeAccountForm.migrationToken = loginMigrationDto.migrationToken
                migrationMergeAccountForm.temporaryCorporateUserId =
                    loginMigrationDto.temporaryCorporateUserId
                viewModel.migrationMergeAccount(migrationMergeAccountForm)
            }
        }
        binding.buttonBack.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) return@setOnClickListener
            mLastClickTime = SystemClock.elapsedRealtime()
            migrationMergeActivity.getViewPager().currentItem =
                migrationMergeActivity.getViewPager().currentItem.minus(1)
        }
    }

    private fun initView(loginMigrationDto: LoginMigrationDto) {
        binding.textViewEmailTakenVerifyDesc.text = formatString(
            R.string.param_desc_email_address_taken_verify,
            loginMigrationDto.emailAddress
        ).toHtmlSpan()
    }

    private fun validateForm() {
        val passwordObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = 8,
            maxLength = 30,
            editText = binding.editTextPassword
        )

        passwordObservable
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe { viewUtil.setError(it) }
            .addTo(disposables)

        RxCombineValidator(passwordObservable)
            .asObservable()
            .distinctUntilChanged()
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe {
                binding.buttonContinue.enableButton(it)
            }.addTo(disposables)
    }

    private fun navigateNextScreen() {
        migrationMergeActivity.getViewPager().currentItem =
            migrationMergeActivity.getViewPager().currentItem.plus(1)
    }

    companion object {
        fun newInstance(): NominateMergeVerifyFragment {
            val fragment =
                NominateMergeVerifyFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }

    override val viewModelClassType: Class<MigrationViewModel>
        get() = MigrationViewModel::class.java

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentNominateEmailTakenVerifyBinding
        get() = FragmentNominateEmailTakenVerifyBinding::inflate
}
