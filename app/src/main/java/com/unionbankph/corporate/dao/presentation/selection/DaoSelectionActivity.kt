package com.unionbankph.corporate.dao.presentation.selection

import android.os.Bundle
import android.view.MenuItem
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.common.presentation.constant.URLDataEnum
import com.unionbankph.corporate.dao.presentation.DaoActivity
import kotlinx.android.synthetic.main.activity_dao_selection.*
import kotlinx.android.synthetic.main.activity_organization_transfer.viewToolbar
import kotlinx.android.synthetic.main.widget_transparent_appbar.*

/**
 * Created by herald on 1/27/21
 */
class DaoSelectionActivity : BaseActivity<DaoSelectionViewModel>(R.layout.activity_dao_selection) {

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
        setToolbarTitle(tvToolbar, formatString(R.string.title_open_a_business_account))
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        cl_open_business_account.setOnClickListener {
            navigator.navigateBrowser(
                this,
                URLDataEnum.ACCOUNT_OPENING_LINK
            )
        }
        cl_existing_application.setOnClickListener {
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

}