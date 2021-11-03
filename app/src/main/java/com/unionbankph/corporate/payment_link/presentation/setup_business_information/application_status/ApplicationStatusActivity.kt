package com.unionbankph.corporate.payment_link.presentation.setup_business_information.application_status

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.databinding.ActivityApplicationStatusBinding

class ApplicationStatusActivity : BaseActivity<ActivityApplicationStatusBinding, ApplicationStatusViewModel>() {

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.root)
        setDrawableBackButton(
            R.drawable.ic_msme_back_button_orange,
            R.color.colorSMEMediumOrange,
            true
        )
        setToolbarTitle(binding.viewToolbar.textViewTitle, "Application Status")
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

    override val bindingInflater: (LayoutInflater) -> ActivityApplicationStatusBinding
        get() = ActivityApplicationStatusBinding::inflate
    override val viewModelClassType: Class<ApplicationStatusViewModel>
        get() = ApplicationStatusViewModel::class.java
}