package com.unionbankph.corporate.auth.presentation.migration.nominate_merge

import android.os.Bundle
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.toHtmlSpan
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.auth.presentation.login.LoginActivity
import com.unionbankph.corporate.auth.presentation.migration.MigrationViewModel
import com.unionbankph.corporate.auth.presentation.migration.migration_merge.MigrationMergeActivity
import com.unionbankph.corporate.auth.presentation.migration.migration_selection.MigrationSelectionActivity
import com.unionbankph.corporate.databinding.FragmentNominateEmailTakenSuccessBinding

class NominateMergeSuccessFragment :
    BaseFragment<FragmentNominateEmailTakenSuccessBinding, MigrationViewModel>() {

    private val migrationMergeActivity by lazyFast { (activity as MigrationMergeActivity) }

    override fun onViewsBound() {
        super.onViewsBound()
        val loginMigrationDto = migrationMergeActivity.getLoginMigrationMergeInfo()
        if (migrationMergeActivity.getType() == MigrationMergeActivity.TYPE_ECREDITING) {
            binding.textViewEmailTakenSuccessDesc.text = formatString(
                R.string.param_desc_ecred_email_address_taken_success,
                loginMigrationDto.userId,
                loginMigrationDto.emailAddress
            ).toHtmlSpan()
        } else {
            binding.textViewEmailTakenSuccessDesc.text = formatString(
                R.string.param_desc_email_address_taken_success,
                loginMigrationDto.corpId,
                loginMigrationDto.userId,
                loginMigrationDto.emailAddress
            ).toHtmlSpan()
        }
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        binding.buttonLogin.setOnClickListener {
            navigator.navigate(
                (activity as MigrationMergeActivity),
                LoginActivity::class.java,
                Bundle().apply {
                    putBoolean(LoginActivity.EXTRA_SPLASH_SCREEN, false)
                },
                isClear = true,
                isAnimated = true,
                transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_RIGHT
            )
        }
        binding.buttonMergeAnotherAccount.setOnClickListener {
            navigator.navigateClearUpStack(
                (activity as MigrationMergeActivity),
                MigrationSelectionActivity::class.java,
                null,
                isClear = true,
                isAnimated = true
            )
            eventBus.actionSyncEvent.emmit(
                BaseEvent(ActionSyncEvent.ACTION_REFRESH_MIGRATION_FORM_SCREEN)
            )
        }
    }

    companion object {
        fun newInstance(): NominateMergeSuccessFragment {
            val fragment =
                NominateMergeSuccessFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }

    override val layoutId: Int
        get() = R.layout.fragment_nominate_email_taken_success

    override val viewModelClassType: Class<MigrationViewModel>
        get() = MigrationViewModel::class.java
}
