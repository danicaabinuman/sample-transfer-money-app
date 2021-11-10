package com.unionbankph.corporate.user_creation.presentation

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.app.common.widget.dialog.ConfirmationBottomSheet
import com.unionbankph.corporate.auth.presentation.login.LoginActivity
import com.unionbankph.corporate.common.presentation.callback.OnConfirmationPageCallBack
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.databinding.ActivityUserCreationBinding
import com.unionbankph.corporate.user_creation.data.UserCreationForm
import com.unionbankph.corporate.user_creation.presentation.enter_name.UcEnterNameViewModel
import com.unionbankph.corporate.user_creation.presentation.enter_contact_info.UcEnterContactInfoViewModel
import timber.log.Timber


class UserCreationActivity :
    BaseActivity<ActivityUserCreationBinding, UserCreationViewModel>() {

    private lateinit var navHostFragment: NavHostFragment

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.toolbar, binding.appBarLayout)
        setDrawableBackButton(R.drawable.ic_msme_back_button_orange, R.color.colorDarkOrange, true)
        setIsScreenScrollable(false)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()

        viewModel.uiState.observe(this, EventObserver {
            when (it) {
                is UiState.Loading -> {
                    showProgressAlertDialog(this::class.java.simpleName)
                }
                is UiState.Complete -> {
                    dismissProgressAlertDialog()
                }
                is UiState.Error -> {
                    handleOnError(it.throwable)
                }
                is UiState.Exit -> {
                    navigator.navigateClearStacks(
                        this,
                        LoginActivity::class.java,
                        Bundle().apply { putBoolean(LoginActivity.EXTRA_SPLASH_SCREEN, false) },
                        true
                    )
                }
            }
        })
    }

    override fun onViewsBound() {
        super.onViewsBound()
        init()
    }

    private fun init() {
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.openAccountNavHostFragment) as NavHostFragment
        val inflater = navHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.user_creation_navigation)
        graph.setStartDestination(R.id.account_selection_fragment)
        navHostFragment.navController.graph = graph

        handleIfFromOTPValidation()
    }

    private fun handleIfFromOTPValidation() {
        if (intent.getBooleanExtra(EXTRA_FROM_OTP, false)) {
            val form = JsonHelper.fromJson<UserCreationForm>(intent.getStringExtra(EXTRA_FORM))
            viewModel.setExistingFormData(form)
            viewModel.setOTPVerificationOTPToken(
                intent.getStringExtra(EXTRA_VERIFICATION_TOKEN) ?: ""
            )
            navigateUpToNominatePassword()
        }
    }

    private fun navigateUpToNominatePassword() {
        navHostFragment.navController
            .navigate(R.id.action_selection_to_reminder)
        navHostFragment.navController
            .navigate(R.id.action_reminder_to_enter_name)
        navHostFragment.navController
            .navigate(R.id.action_enter_name_to_tnc_reminder)
        navHostFragment.navController
            .navigate(R.id.action_tnc_reminder_to_tnc)
        navHostFragment.navController
            .navigate(R.id.action_tnc_to_enter_contact_info)
        navHostFragment.navController
            .navigate(R.id.action_contact_to_nominate_password)
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

    override fun onSupportNavigateUp(): Boolean {
        return Navigation.findNavController(
            this, R.id.openAccountNavHostFragment).navigateUp()
    }

    fun showGoBackBottomSheet() {
        val cancelBottomSheet = ConfirmationBottomSheet.newInstance(
            R.drawable.ic_warning_white,
            formatString(R.string.title_go_back),
            formatString(R.string.msg_go_back),
            formatString(R.string.action_confirm),
            formatString(R.string.action_no)
        )
        cancelBottomSheet.setOnConfirmationPageCallBack(object : OnConfirmationPageCallBack {
            override fun onClickPositiveButtonDialog(data: String?, tag: String?) {
                cancelBottomSheet.dismiss()
                popBackStack()
            }

            override fun onClickNegativeButtonDialog(data: String?, tag: String?) {
                cancelBottomSheet.dismiss()
            }
        })
        cancelBottomSheet.show(
            supportFragmentManager,
            TAG_GO_BACK_DAO_DIALOG
        )
    }

    fun popBackStack() {
        navHostFragment.navController.popBackStack()
    }

    fun setNameInput(input: UcEnterNameViewModel.Input) {
        viewModel.setNameInput(input)
    }

    fun setContactInput(input: UcEnterContactInfoViewModel.Input){
        viewModel.setContactInfo(input)
    }

    fun getDefaultForm() : UserCreationForm = viewModel.defaultForm()

    fun getOTPSuccessToken() : String = viewModel.getOTPSuccessToken()

    fun setIsScreenScrollable(isScrollable: Boolean) {
        val elevation = when (isScrollable) {
            true -> TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 4f, resources.displayMetrics)
            else -> 0f
        }

        supportActionBar?.elevation = elevation
        binding.appBarLayout.elevation = elevation
    }

    fun navigateToWebApps(type: String) {

        val url = when (type) {
            WEB_APP_ACCOUNT_OPENING ->
                formatString(R.string.url_portal_account_opening, BuildConfig.URL_ENV_PREFIX)
            else ->
                formatString(R.string.url_portal_enrollment, BuildConfig.URL_ENV_PREFIX)
        }

        Timber.e("Web App URL $url")
        navigator.navigateBrowser(this, url)
    }

    companion object {

        const val WEB_APP_ENROLlMENT = "enrollment"
        const val WEB_APP_ACCOUNT_OPENING = "account_opening"

        const val EXTRA_FROM_OTP = "from_otp"
        const val EXTRA_FORM = "form"
        const val EXTRA_VERIFICATION_TOKEN = "verification_token"

        const val FRAGMENT_ENTER_NAME = "fragment_enter_name"
        const val TAG_GO_BACK_DAO_DIALOG = "user_creation_go_back_dialog"
    }

    override val bindingInflater: (LayoutInflater) -> ActivityUserCreationBinding
        get() = ActivityUserCreationBinding::inflate

    override val viewModelClassType: Class<UserCreationViewModel>
        get() = UserCreationViewModel::class.java
}