package com.unionbankph.corporate.account_setup.presentation

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.databinding.ActivityAccountSetupBinding
import timber.log.Timber


class AccountSetupActivity :
    BaseActivity<ActivityAccountSetupBinding, AccountSetupViewModel>(){

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


    override val bindingInflater: (LayoutInflater) -> ActivityAccountSetupBinding
        get() = ActivityAccountSetupBinding::inflate
    override val viewModelClassType: Class<AccountSetupViewModel>
        get() =  AccountSetupViewModel::class.java

}