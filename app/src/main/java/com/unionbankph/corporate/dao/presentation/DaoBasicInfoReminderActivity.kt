package com.unionbankph.corporate.dao.presentation

import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.formatString
import kotlinx.android.synthetic.main.activity_dao_basic_info_reminder.*

class DaoBasicInfoReminderActivity :
    BaseActivity<DaoViewModel>(R.layout.activity_dao_basic_info_reminder) {

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel = ViewModelProviders.of(this, viewModelFactory)[DaoViewModel::class.java]

        viewModel.getSignatoryDetailsFromCache()

        viewModel.navigatePages.observe(this, Observer {
            textViewName.text = formatString(R.string.hi, viewModel.signatoriesDetail.value?.firstNameInput)
        })
    }

    override fun onInitializeListener() {
        super.onInitializeListener()

        imageViewBack.setOnClickListener { onBackPressed() }
        buttonProceed.setOnClickListener { onBackPressed() }
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
}