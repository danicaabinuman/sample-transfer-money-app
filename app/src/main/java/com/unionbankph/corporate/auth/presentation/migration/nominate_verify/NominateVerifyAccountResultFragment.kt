package com.unionbankph.corporate.auth.presentation.migration.nominate_verify

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.extension.toHtmlSpan
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.auth.data.model.ECredSubmitOTPDto
import com.unionbankph.corporate.auth.data.model.MigrationSubmitDto
import com.unionbankph.corporate.auth.presentation.login.LoginActivity
import com.unionbankph.corporate.auth.presentation.migration.MigrationViewModel
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationDismissLoading
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationError
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationLoading
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationResendEmailECred
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_nominate_verify_account_result.*

class NominateVerifyAccountResultFragment :
    BaseFragment<MigrationViewModel>(R.layout.fragment_nominate_verify_account_result) {

    private lateinit var emailAddress: String

    override fun onInitializeListener() {
        super.onInitializeListener()
        initEventBus()
        btn_back_to_login.setOnClickListener {
            navigator.navigateClearUpStack(
                getAppCompatActivity(),
                LoginActivity::class.java,
                Bundle().apply {
                    putBoolean(LoginActivity.EXTRA_SPLASH_SCREEN, false)
                },
                isClear = true,
                isAnimated = true
            )
        }
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel = ViewModelProviders.of(this, viewModelFactory)[MigrationViewModel::class.java]
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowMigrationLoading -> {
                    showProgressAlertDialog(
                        NominateVerifyAccountFragment::class.java.simpleName
                    )
                }
                is ShowMigrationDismissLoading -> {
                    dismissProgressAlertDialog()
                }
                is ShowMigrationResendEmailECred -> {
                    MaterialDialog(getAppCompatActivity()).show {
                        lifecycleOwner(getAppCompatActivity())
                        message(text = it.message.message.notNullable())
                        positiveButton(R.string.action_close)
                    }
                }
                is ShowMigrationError -> {
                    handleOnError(it.throwable)
                }
            }
        })
    }

    private fun initEventBus() {
        eventBus.actionSyncEvent.flowable.subscribe {
            when (it.eventType) {
                ActionSyncEvent.ACTION_UPDATE_EMAIL_MIGRATION -> {
                    emailAddress = it.payload.notNullable()
                }
                ActionSyncEvent.ACTION_UPDATE_VERIFY_RESULT_MIGRATION -> {
                    val migrationSubmitDto = JsonHelper.fromJson<MigrationSubmitDto>(it.payload)
                    tvHeader.text = migrationSubmitDto.message
                    textViewVerifyAccountDesc.text = formatString(
                        R.string.param_desc_nominate_verify_account,
                        emailAddress
                    ).toHtmlSpan()
                }
                ActionSyncEvent.ACTION_UPDATE_VERIFY_RESULT_ECRED_MIGRATION -> {
                    val eCredSubmitOTPDto = JsonHelper.fromJson<ECredSubmitOTPDto>(it.payload)
                    textViewVerifyAccountDesc.text = formatString(
                        R.string.param_desc_nominate_verify_account,
                        eCredSubmitOTPDto.emailAddress
                    ).toHtmlSpan()
                }
            }
        }.addTo(disposables)
    }

    companion object {
        fun newInstance(): NominateVerifyAccountResultFragment {
            val fragment =
                NominateVerifyAccountResultFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }
}
