package com.unionbankph.corporate.general.presentation.result

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.auth.presentation.login.LoginActivity
import com.unionbankph.corporate.common.data.model.ApiError
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.settings.data.form.VerifyEmailAddressForm
import com.unionbankph.corporate.settings.presentation.SettingsViewModel
import com.unionbankph.corporate.settings.presentation.ShowSettingsChangeEmailSuccess
import com.unionbankph.corporate.settings.presentation.ShowSettingsDismissLoading
import com.unionbankph.corporate.settings.presentation.ShowSettingsError
import com.unionbankph.corporate.settings.presentation.ShowSettingsLoading
import kotlinx.android.synthetic.main.activity_result_landing_page.*
import kotlinx.android.synthetic.main.widget_transparent_appbar.*

class ResultLandingWithoutAuthActivity :
    BaseActivity<SettingsViewModel>(R.layout.activity_result_landing_page) {

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initSettingsViewModel()
    }

    override fun onViewsBound() {
        super.onViewsBound()
        buttonClose.text = getString(R.string.title_login)
        initVerifyEmail()
    }

    private fun initSettingsViewModel() {
        viewModel =
            ViewModelProviders.of(this, viewModelFactory)[SettingsViewModel::class.java]
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowSettingsLoading -> {
                    showInitialLoading()
                }
                is ShowSettingsDismissLoading -> {
                    dismissInitialLoading()
                }
                is ShowSettingsChangeEmailSuccess -> {
                    textViewTitle.text = it.message.message
                    textViewDescription.text = it.message.description
                    scrollView.visibility = View.VISIBLE
                }
                is ShowSettingsError -> {
                    showErrorResult(it.throwable)
                }
            }
        })
    }

    private fun showErrorResult(throwable: Throwable) {

        try {
            val apiError = JsonHelper.fromJson<ApiError>(throwable.message)
            val errorMessage = apiError.errors[0].message ?: ""
            val errorDescription = apiError.errors[0].description ?: ""
            textViewTitle.text = formatString(R.string.title_verification_link_expired)
            textViewDescription.text = formatString(R.string.msg_verification_link_expired)
            scrollView.visibility = View.VISIBLE
        } catch (e: Exception) {
            scrollView.visibility = View.GONE
            handleOnError(throwable)
        }
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        buttonClose.setOnClickListener {
            navigator.navigate(
                this,
                LoginActivity::class.java,
                null,
                isClear = true,
                isAnimated = true,
                transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_RIGHT
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

    private fun dismissInitialLoading() {
        viewLoadingState.visibility = View.GONE
    }

    private fun showInitialLoading() {
        viewLoadingState.visibility = View.VISIBLE
        scrollView.visibility = View.GONE
    }

    private fun initVerifyEmail() {
        val intent = intent
        val action = intent.action
        val data = intent.data
        if (action == null || data == null) return
        if (data.toString().contains("confirm-change-email?confirmationId")) {
            val confirmationId = data.getQueryParameter("confirmationId")
            viewModel.verifyEmailAddress(VerifyEmailAddressForm(confirmationId))
        }
    }
}
