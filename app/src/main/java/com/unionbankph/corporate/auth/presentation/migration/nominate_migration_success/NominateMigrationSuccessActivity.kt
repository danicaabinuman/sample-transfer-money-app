package com.unionbankph.corporate.auth.presentation.migration.nominate_migration_success

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.auth.data.form.ECredEmailConfirmationForm
import com.unionbankph.corporate.auth.data.form.EmailConfirmationForm
import com.unionbankph.corporate.auth.presentation.login.LoginActivity
import com.unionbankph.corporate.auth.presentation.migration.MigrationViewModel
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationConfirmEmail
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationDismissLoading
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationECredConfirmEmail
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationError
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationLoading
import com.unionbankph.corporate.auth.presentation.migration.migration_selection.MigrationSelectionActivity
import com.unionbankph.corporate.common.data.model.ApiError
import com.unionbankph.corporate.common.data.source.local.cache.CacheManager
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.settings.data.form.VerifyEmailAddressForm
import com.unionbankph.corporate.settings.presentation.SettingsViewModel
import com.unionbankph.corporate.settings.presentation.ShowSettingsDismissLoading
import com.unionbankph.corporate.settings.presentation.ShowSettingsError
import com.unionbankph.corporate.settings.presentation.ShowSettingsLoading
import kotlinx.android.synthetic.main.activity_nominate_migration_success.*
import kotlinx.android.synthetic.main.widget_transparent_appbar.*

class NominateMigrationSuccessActivity :
    BaseActivity<MigrationViewModel>(R.layout.activity_nominate_migration_success) {

    private lateinit var settingsViewModel: SettingsViewModel

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initMigrationViewModel()
        initSettingsViewModel()
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initConfirmationEmail()
    }

    private fun initSettingsViewModel() {
        settingsViewModel =
            ViewModelProviders.of(this, viewModelFactory)[SettingsViewModel::class.java]
        settingsViewModel.state.observe(this, Observer {
            when (it) {
                is ShowSettingsLoading -> {
                    showInitialLoading()
                }
                is ShowSettingsDismissLoading -> {
                    dismissInitialLoading()
                }
                is ShowSettingsError -> {
                    showErrorResult(it.throwable)
                }
            }
        })
    }

    private fun initMigrationViewModel() {
        viewModel = ViewModelProviders.of(this, viewModelFactory)[MigrationViewModel::class.java]
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowMigrationLoading -> {
                    showInitialLoading()
                }
                is ShowMigrationDismissLoading -> {
                    dismissInitialLoading()
                }
                is ShowMigrationConfirmEmail -> {
                    textViewEmailTakenTitle.text = it.confirmationEmailDto.message
                    textViewEmailTakenDesc.text = it.confirmationEmailDto.description
                }
                is ShowMigrationECredConfirmEmail -> {
                    textViewEmailTakenTitle.text = formatString(R.string.title_migration_successful)
                    textViewEmailTakenDesc.text = formatString(R.string.msg_migration_successful)
                }
                is ShowMigrationError -> {
                    showErrorResult(it.throwable)
                }
            }
        })
    }

    private fun showErrorResult(throwable: Throwable) {
        scrollViewContent.visibility = View.GONE
        try {
            val intent = intent
            val action = intent.action
            val data = intent.data
            val apiError = JsonHelper.fromJson<ApiError>(throwable.message)
            if (action == null || data == null) return
            if (data.toString().contains(HOOK_ECRED)) {
                if (apiError.errorCode == ECRED_MIGRATION_COMPLETE) {
                    imageViewLogo.setImageResource(R.drawable.logo_migration_already_migrated)
                    textViewEmailTakenTitle.text =
                        formatString(R.string.title_account_already_migrated)
                    textViewEmailTakenDesc.text =
                        formatString(R.string.msg_migration_already_migrated)
                } else {
                    imageViewLogo.setImageResource(R.drawable.logo_migration_expired)
                    textViewEmailTakenTitle.text =
                        formatString(R.string.title_migration_link_expired)
                    textViewEmailTakenDesc.text = formatString(R.string.msg_migration_link_expired)
                }
            } else {
                val errorMessage = apiError.errors[0].message.notNullable()
                val errorDescription = apiError.errors[0].description.notNullable()
                val errorCode = apiError.errors[0].code.notNullable()
                textViewEmailTakenTitle.text = errorMessage
                textViewEmailTakenDesc.text = errorDescription
                when (errorCode) {
                    "MGRYSN-00" -> {
                        imageViewLogo.setImageResource(R.drawable.logo_migration_expired)
                    }
                    "MGRYSN-01" -> {
                        imageViewLogo.setImageResource(R.drawable.logo_migration_already_migrated)
                    }
                    "MGRYSN-02" -> {
                        imageViewLogo.setImageResource(R.drawable.logo_migration_expired)
                    }
                    "MGRYSN-03" -> {
                        imageViewLogo.setImageResource(R.drawable.logo_migration_expired)
                    }
                }
            }
        } catch (e: Exception) {
            handleOnError(throwable)
        }
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        buttonLogin.setOnClickListener {
            userLogout()
        }
        buttonMigrateAnotherAccount.setOnClickListener {
            navigator.navigateClearStacks(
                this,
                MigrationSelectionActivity::class.java,
                null,
                isAnimated = true
            )
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        userLogout()
    }

    private fun userLogout() {
        if (settingsUtil.isLoggedIn()) {
            super.onBackPressed()
        } else {
            navigator.navigate(
                this,
                LoginActivity::class.java,
                Bundle().apply {
                    putBoolean(LoginActivity.EXTRA_SPLASH_SCREEN, false)
                },
                isClear = true,
                isAnimated = true,
                transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_RIGHT
            )
        }
    }

    private fun dismissInitialLoading() {
        viewLoadingState.visibility = View.GONE
        scrollViewContent.visibility = View.VISIBLE
    }

    private fun showInitialLoading() {
        viewLoadingState.visibility = View.VISIBLE
        scrollViewContent.visibility = View.GONE
    }

    private fun initConfirmationEmail() {
        val intent = intent
        val action = intent.action
        val data = intent.data
        if (action == null || data == null) return
        when {
            data.toString().contains(HOOK_EBANKING) -> {
                val confirmationToken = data.getQueryParameter(PARAM_CONFIRMATION_TOKEN)
                viewModel.migrationConfirmEmail(
                    EmailConfirmationForm(
                        confirmationToken
                    )
                )
            }
            data.toString().contains(HOOK_VERIFY_EMAIL) -> {
                val confirmationId = data.getQueryParameter(PARAM_CONFIRMATION_ID)
                settingsViewModel.verifyEmailAddress(VerifyEmailAddressForm(confirmationId))
            }
            data.toString().contains(HOOK_ECRED) -> {
                val confirmationToken = data.getQueryParameter(PARAM_CONFIRMATION_TOKEN)
                viewModel.migrationECred(
                    ECredEmailConfirmationForm(
                        confirmationToken
                    )
                )
            }
        }
    }

    companion object {
        const val HOOK_ECRED = "migration/ecrediting/migrate-account"
        const val HOOK_EBANKING = "migration/activate-account"
        const val HOOK_VERIFY_EMAIL = "confirmationId"

        const val ECRED_MIGRATION_COMPLETE = "migration.user.migrationComplete"
        const val ECRED_MIGRATION_EXPIRED = "migration.confirmationToken.expired"
        const val ECRED_MIGRATION_INVALID = "migration.confirmationToken.invalid"

        const val PARAM_CONFIRMATION_TOKEN = "confirmation_token"
        const val PARAM_CONFIRMATION_ID = "confirmationId"
    }
}
