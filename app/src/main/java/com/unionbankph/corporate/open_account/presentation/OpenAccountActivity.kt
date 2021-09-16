package com.unionbankph.corporate.open_account.presentation

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.widget.dialog.ConfirmationBottomSheet
import com.unionbankph.corporate.common.presentation.callback.OnConfirmationPageCallBack
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.databinding.ActivityOpenAccountBinding
import com.unionbankph.corporate.open_account.data.OpenAccountForm
import com.unionbankph.corporate.open_account.presentation.enter_name.OAEnterNameViewModel
import com.unionbankph.corporate.open_account.presentation.enter_contact_info.OAEnterContactInfoViewModel
import timber.log.Timber


class OpenAccountActivity :
    BaseActivity<ActivityOpenAccountBinding, OpenAccountViewModel>() {

    private lateinit var navHostFragment: NavHostFragment

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.toolbar, binding.appBarLayout)
        setDrawableBackButton(R.drawable.ic_msme_back_button_orange, R.color.colorDarkOrange, true)
        setIsScreenScrollable(false)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()

    }

    override fun onViewsBound() {
        super.onViewsBound()
        init()
    }

    private fun init() {
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.openAccountNavHostFragment) as NavHostFragment
        val inflater = navHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.open_account_navigation)
        graph.setStartDestination(R.id.account_selection_fragment)
        navHostFragment.navController.graph = graph

        handleIfFromOTPValidation()
    }

    private fun handleIfFromOTPValidation() {
        if (intent.getBooleanExtra(EXTRA_FROM_OTP, false)) {
            val form = JsonHelper.fromJson<OpenAccountForm>(intent.getStringExtra(EXTRA_FORM))
            viewModel.setExistingFormData(form)
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

    fun setNameInput(input: OAEnterNameViewModel.Input) {
        viewModel.setNameInput(input)
    }

    fun setContactInput(input: OAEnterContactInfoViewModel.Input){
        viewModel.setContactInfo(input)
    }

    fun getDefaultForm() : OpenAccountForm = viewModel.defaultForm()

    fun setIsScreenScrollable(isScrollable: Boolean) {
        val elevation = when (isScrollable) {
            true -> TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 4f, resources.displayMetrics)
            else -> 0f
        }

        supportActionBar?.elevation = elevation
        binding.appBarLayout.elevation = elevation
    }

    companion object {

        const val EXTRA_FROM_OTP = "from_otp"
        const val EXTRA_FORM = "form"

        const val FRAGMENT_ENTER_NAME = "fragment_enter_name"
        const val TAG_GO_BACK_DAO_DIALOG = "user_creation_go_back_dialog"
    }

    override val bindingInflater: (LayoutInflater) -> ActivityOpenAccountBinding
        get() = ActivityOpenAccountBinding::inflate

    override val viewModelClassType: Class<OpenAccountViewModel>
        get() = OpenAccountViewModel::class.java
}