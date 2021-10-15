package com.unionbankph.corporate.account_setup.presentation

import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.enableButton
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.dao.presentation.DaoActivity
import com.unionbankph.corporate.databinding.ActivityAccountSetupBinding
import timber.log.Timber


class AccountSetupActivity :
    BaseActivity<ActivityAccountSetupBinding, AccountSetupViewModel>(){

    private lateinit var toolbarButton: TextView
    private var currentScreen: Int = BUSINESS_TYPE_SCREEN

    private lateinit var navHostFragment: NavHostFragment

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.toolbar, binding.appBarLayout)
        setDrawableBackButton(R.drawable.ic_msme_back_button_orange, R.color.colorDarkOrange, true)
        setIsScreenScrollable(false)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()

        viewModel.state.observe(this, Observer {
            Timber.e("asState + ${JsonHelper.toJson(it)}")
        })

        viewModel.toolbarState.observe(this, {
            Timber.e("toolbarState + ${JsonHelper.toJson(it)}")
            invalidateOptionsMenu()
        })
    }

    override fun onViewsBound() {
        super.onViewsBound()
        init()
    }

    private fun init() {
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.accountSetupNavHostFragment) as NavHostFragment
        val inflater = navHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.account_setup_navigation)
        graph.setStartDestination(R.id.business_type_fragment)
        navHostFragment.navController.graph = graph
    }

    fun setDebitCardType(debitCardType: Int) {
        viewModel.setDebitCardType(debitCardType)
    }

    fun setBusinessType(businessType: Int) {
        viewModel.setBusinessType(businessType)
    }

    fun getExistingBusinessType(): Int {
        return viewModel.state.value?.businessType ?: -1
    }

    fun setBusinessAccountType(accountType: Int) {
        viewModel.setBusinessAccountType(accountType)
    }

    fun getExistingBusinessAccountType(): Int {
        return viewModel.state.value?.businessAccountType ?: -1
    }

    fun showProgress(isShown: Boolean) {
        binding.progressAction.visibility(isShown)
    }

    fun setCurrentScreen(currentScreen: Int) {
        this.currentScreen = currentScreen
    }

    fun getExistingDebitCardType() : Int? {
        return viewModel.state.value?.debitCardType
    }

    fun setProgressValue(step: Int) {
        val progressValue = step * 100 / getNumberOfPages()
        //progress_action.progress = progressValue
        ObjectAnimator.ofInt(binding.progressAction, "progress", progressValue)
            .setDuration(150)
            .start()
    }

    private fun getNumberOfPages() : Int {
        return when (getExistingBusinessType()) {
            Constant.BusinessType.SOLE_PROP -> PAGE_SIZE_SOLE_PROP
            else -> PAGE_SIZE_SOLE_PROP
        }
    }

    fun showToolbarButton(isShow: Boolean) {
        viewModel.showToolbarButton(isShow)
    }

    fun setToolbarButtonType(type: Int) {
        viewModel.setToolbarButtonType(type)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.menu_save_and_exit_button -> {
                Timber.e("Save and Exit")
                true
            }
            R.id.menu_retake_button -> {
                Timber.e("Retake")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_toolbar, menu)
        val toolbarButtonType = viewModel.toolbarState.value?.buttonType

        val menuActionButton: MenuItem?

        when (toolbarButtonType) {
            BUTTON_SAVE_EXIT -> {
                menuActionButton = menu.findItem(R.id.menu_save_and_exit_button)
                toolbarButton = menuActionButton.actionView.findViewById(R.id.toolbarButton)
                toolbarButton.text = getString(R.string.save_and_exit)
            }
            else -> {
                menuActionButton = menu.findItem(R.id.menu_retake_button)
                toolbarButton = menuActionButton.actionView.findViewById(R.id.toolbarButton)
                toolbarButton.text = getString(R.string.action_retake)
            }
        }

        menuActionButton.isVisible = viewModel.toolbarState.value?.isButtonShow ?: false
        toolbarButton.setOnClickListener { onOptionsItemSelected(menuActionButton) }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onSupportNavigateUp(): Boolean {
        return Navigation.findNavController(
            this, R.id.accountSetupNavHostFragment).navigateUp()
    }

    fun setIsScreenScrollable(isScrollable: Boolean) {
        binding.appBarLayout.apply {
            if (isScrollable) addElevation(this)
            else removeElevation(this)
        }
    }

    fun popBackStack() {
        navHostFragment.navController.popBackStack()
    }

    companion object {
        const val BUTTON_SAVE_EXIT = 1
        const val BUTTON_CLOSE = 2

        const val BACK_ARROW = 100
        const val BACK_X = 101

        const val BUSINESS_TYPE_SCREEN = 1
        const val BUSINESS_ACCOUNT_TYPE_SCREEN = 2

        const val PAGE_SIZE_SOLE_PROP = 3
    }


    override val bindingInflater: (LayoutInflater) -> ActivityAccountSetupBinding
        get() = ActivityAccountSetupBinding::inflate
    override val viewModelClassType: Class<AccountSetupViewModel>
        get() =  AccountSetupViewModel::class.java

}