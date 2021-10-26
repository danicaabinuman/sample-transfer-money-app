package com.unionbankph.corporate.dao.presentation

import android.view.LayoutInflater
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.databinding.ActivityDaoBasicInfoReminderBinding

class DaoBasicInfoReminderActivity :
    BaseActivity<ActivityDaoBasicInfoReminderBinding, DaoViewModel>() {

    override fun onViewModelBound() {
        super.onViewModelBound()

        viewModel.navigatePages.observe(this, Observer {
            binding.textViewName.text = formatString(R.string.hi, viewModel.signatoriesDetail.value?.firstNameInput)
        })

        viewModel.getSignatoryDetailsFromCache()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()

        binding.buttonProceed.setOnClickListener { onBackPressed() }
    }

    override fun onBackPressed() {
        onBackPressed(false)
        overridePendingTransition(R.anim.anim_forward_left_to_right, R.anim.anim_forward_right_to_left)
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

    override val bindingInflater: (LayoutInflater) -> ActivityDaoBasicInfoReminderBinding
        get() = ActivityDaoBasicInfoReminderBinding::inflate

    override val viewModelClassType: Class<DaoViewModel>
        get() = DaoViewModel::class.java
}