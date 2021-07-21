package com.unionbankph.corporate.dao.presentation.selection

import android.os.Bundle
import android.view.MenuItem
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.common.presentation.constant.URLDataEnum
import com.unionbankph.corporate.dao.presentation.DaoActivity
import com.unionbankph.corporate.databinding.ActivityDaoSelectionBinding

/**
 * Created by herald on 1/27/21
 */
class DaoSelectionActivity :
    BaseActivity<ActivityDaoSelectionBinding, DaoSelectionViewModel>() {

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
        setToolbarTitle(binding.viewToolbar.tvToolbar, formatString(R.string.title_open_a_business_account))
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        binding.clOpenBusinessAccount.setOnClickListener {
            navigator.navigateBrowser(
                this,
                URLDataEnum.ACCOUNT_OPENING_LINK
            )
        }
        binding.clExistingApplication.setOnClickListener {
            navigateDaoScreen()
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

    private fun navigateDaoScreen() {
        navigator.navigate(
            this,
            DaoActivity::class.java,
            null,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    override val layoutId: Int
        get() = R.layout.activity_dao_selection

    override val viewModelClassType: Class<DaoSelectionViewModel>
        get() = DaoSelectionViewModel::class.java

}