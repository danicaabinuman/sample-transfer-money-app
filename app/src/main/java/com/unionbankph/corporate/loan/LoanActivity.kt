package com.unionbankph.corporate.loan

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.clearTheme
import com.unionbankph.corporate.app.common.extension.setColor
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.databinding.ActivityLoanBinding

class LoanActivity : BaseActivity<ActivityLoanBinding, LoanMainViewModel>() {

    private lateinit var navHostFragment: NavHostFragment

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.toolbar, binding.appBarLayout)
        setDrawableBackButton(R.drawable.ic_msme_back_button_orange, R.color.colorDarkOrange, true)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initNavHost()
    }

    private fun initNavHost() {
        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_loan_host_fragment) as NavHostFragment
        val inflater = navHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.nav_loan)
        navHostFragment.navController.graph = graph
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.nav_loan_host_fragment)
        return when(navController.currentDestination?.id) {
            R.id.loansFragment -> {
                finish()
                true
            }
            else -> navController.navigateUp()
        }
    }

    override val viewModelClassType: Class<LoanMainViewModel>
        get() = LoanMainViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityLoanBinding
        get() = ActivityLoanBinding::inflate

}