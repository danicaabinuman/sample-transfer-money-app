package com.unionbankph.corporate.auth.presentation.migration.migration_selection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.room.migration.Migration
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.App
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.makeLinks
import com.unionbankph.corporate.app.common.extension.setEnableView
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.auth.presentation.migration.MigrationViewModel
import com.unionbankph.corporate.auth.presentation.migration.migration_form.MigrationFormActivity
import com.unionbankph.corporate.databinding.ActivityMigrationSelectionBinding

/**
 * Created by herald25santos on 2020-02-03
 */
class MigrationSelectionActivity :
    BaseActivity<ActivityMigrationSelectionBinding, MigrationViewModel>() {

    override fun onViewsBound() {
        super.onViewsBound()
        binding.constraintLayoutECrediting.setEnableView(!App.isSupportedInProduction)
        binding.textViewMigration.makeLinks(
            Pair(getString(R.string.action_here), View.OnClickListener {
                onBackPressed()
            })
        )
        binding.textViewTitle.text = formatString(
            if (isSME) {
                R.string.title_migration_selection_sme
            } else {
                R.string.title_migration_selection
            }
        )
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        binding.constraintLayoutEBanking.setOnClickListener {
            navigateMigrationFormScreen(MigrationFormActivity.SCREEN_EBANKING)
        }
        binding.constraintLayoutECrediting.setOnClickListener {
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

    override val viewModelClassType: Class<MigrationViewModel>
        get() = MigrationViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityMigrationSelectionBinding
        get() = ActivityMigrationSelectionBinding::inflate
}
