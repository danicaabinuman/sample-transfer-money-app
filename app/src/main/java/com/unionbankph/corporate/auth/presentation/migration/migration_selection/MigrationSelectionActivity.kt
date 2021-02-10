package com.unionbankph.corporate.auth.presentation.migration.migration_selection

import android.os.Bundle
import android.view.View
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.App
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.makeLinks
import com.unionbankph.corporate.app.common.extension.setEnableView
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.auth.presentation.migration.MigrationViewModel
import com.unionbankph.corporate.auth.presentation.migration.migration_form.MigrationFormActivity
import kotlinx.android.synthetic.main.activity_migration_selection.*

/**
 * Created by herald25santos on 2020-02-03
 */
class MigrationSelectionActivity : BaseActivity<MigrationViewModel>(R.layout.activity_migration_selection) {

    override fun onViewsBound() {
        super.onViewsBound()
        constraintLayoutECrediting.setEnableView(!App.isSupportedInProduction)
        textViewMigration.makeLinks(
            Pair(getString(R.string.action_here), View.OnClickListener {
                onBackPressed()
            })
        )
        textViewTitle.text = formatString(
            if (isSME) {
                R.string.title_migration_selection_sme
            } else {
                R.string.title_migration_selection
            }
        )
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        constraintLayoutEBanking.setOnClickListener {
            navigateMigrationFormScreen(MigrationFormActivity.SCREEN_EBANKING)
        }
        constraintLayoutECrediting.setOnClickListener {
            navigateMigrationFormScreen(MigrationFormActivity.SCREEN_ECREDITING)
        }
    }

    private fun navigateMigrationFormScreen(screen: String) {
        navigator.navigate(
            this,
            MigrationFormActivity::class.java,
            Bundle().apply { putString(MigrationFormActivity.EXTRA_SCREEN, screen) },
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }
}
