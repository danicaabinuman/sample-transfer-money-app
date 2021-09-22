package com.unionbankph.corporate.auth.presentation.migration.nominate_migration_success

import android.os.Bundle
import android.view.LayoutInflater
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
import com.unionbankph.corporate.databinding.ActivityNominateMigrationSuccessBinding
import com.unionbankph.corporate.settings.data.form.VerifyEmailAddressForm
import com.unionbankph.corporate.settings.presentation.SettingsViewModel
import com.unionbankph.corporate.settings.presentation.ShowSettingsDismissLoading
import com.unionbankph.corporate.settings.presentation.ShowSettingsError
import com.unionbankph.corporate.settings.presentation.ShowSettingsLoading

class NominateMigrationSuccessActivity :
    BaseActivity<ActivityNominateMigrationSuccessBinding, MigrationViewModel>() {

    private lateinit var settingsViewModel: SettingsViewModel

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
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
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowMigrationLoading -> {
                    showInitialLoading()
                }
                is ShowMigrationDismissLoading -> {
                    dismissInitialLoading()
                }
                is ShowMigrationConfirmEmail -> {
                    binding.textViewEmailTakenTitle.text = it.confirmationEmailDto.message
                    binding.textViewEmailTakenDesc.text = it.confirmationEmailDto.description
                }
                is ShowMigrationECredConfirmEmail -> {
                    binding.textViewEmailTakenTitle.text = formatString(R.string.title_migration_successful)
                    binding.textViewEmailTakenDesc.text = formatString(R.string.msg_migration_successful)
                }
                is ShowMigrationError -> {
                    showErrorResult(it.throwable)
                }
            }
        })
    }

    private fun showErrorResult(throwable: Throwable) {
        binding.scrollViewContent.visibility = View.GONE
        try {
            val intent = intent
            val action = intent.action
            val data = intent.data
            val apiError = JsonHelper.fromJson<ApiError>(throwable.message)
            if (action == null || data == null) return
            if (data.toString().contains(HOOK_ECRED)) {
                if (apiError.errorCode == ECRED_MIGRATION_COMPLETE) {
                    binding.imageViewLogo.setImageResource(R.drawable.logo_migration_already_migrated)
                    binding.textViewEmailTakenTitle.text =
                        formatString(R.string.title_account_already_migrated)
                    binding. textViewEmailTakenDesc.text =
                        formatString(R.string.msg_migration_already_migrated)
                } else {
                    binding.imageViewLogo.setImageResource(R.drawable.logo_migration_expired)
                    binding.textViewEmailTakenTitle.text =
                        formatString(R.string.title_migration_link_expired)
                    binding.textViewEmailTakenDesc.text = formatString(R.string.msg_migration_link_expired)
                }
            } else {
                val errorMessage = apiError.errors[0].message.notNullable()
                val errorDescription = apiError.errors[0].description.notNullable()
                val errorCode = apiError.errors[0].code.notNullable()
                binding.textViewEmailTakenTitle.text = errorMessage
                binding.textViewEmailTakenDesc.text = errorDescription
                when (errorCode) {
                    "MGRYSN-00" -> {
                        binding.imageViewLogo.setImageResource(R.drawable.logo_migration_expired)
                    }
                    "MGRYSN-01" -> {
                        binding.imageViewLogo.setImageResource(R.drawable.logo_migration_already_migrated)
                    }
                    "MGRYSN-02" -> {
                        binding.imageViewLogo.setImageResource(R.drawable.logo_migration_expired)
                    }
                    "MGRYSN-03" -> {
                        binding.imageViewLogo.setImageResource(R.drawable.logo_migration_expired)
                    }
                }
            }
        } catch (e: Exception) {
            handleOnError(throwable)
        }
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        binding.buttonLogin.setOnClickListener {
            userLogout()
        }
        binding.buttonMigrateAnotherAccount.setOnClickListener {
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
        binding.viewLoadingState.root.visibility = View.GONE
        binding.scrollViewContent.visibility = View.VISIBLE
    }

    private fun showInitialLoading() {
        binding.viewLoadingState.root.visibility = View.VISIBLE
        binding.scrollViewContent.visibility = View.GONE
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

    override val viewModelClassType: Class<MigrationViewModel>
        get() = MigrationViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityNominateMigrationSuccessBinding
        get() = ActivityNominateMigrationSuccessBinding::inflate
}
