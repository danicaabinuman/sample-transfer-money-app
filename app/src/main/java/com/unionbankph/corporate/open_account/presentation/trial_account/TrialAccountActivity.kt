package com.unionbankph.corporate.open_account.presentation.trial_account

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.MenuItem
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.databinding.ActivityTrialAccountSetupBinding

class TrialAccountActivity :
    BaseActivity<ActivityTrialAccountSetupBinding, TrialAccountViewModel>()  {

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.toolbar, binding.appBarLayout)
        setToolbarTitle(binding.tvToolbar, formatString(R.string.title_cotinue_account_setup))
        setDrawableBackButton(R.drawable.ic_msme_back_button_orange, R.color.colorDarkOrange, true)

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

    override val bindingInflater: (LayoutInflater) -> ActivityTrialAccountSetupBinding
        get() = ActivityTrialAccountSetupBinding::inflate

    override val viewModelClassType: Class<TrialAccountViewModel>
        get() = TrialAccountViewModel::class.java
}