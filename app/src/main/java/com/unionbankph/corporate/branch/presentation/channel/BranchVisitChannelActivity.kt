package com.unionbankph.corporate.branch.presentation.channel

import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.branch.presentation.form.BranchVisitFormActivity
import com.unionbankph.corporate.common.presentation.viewmodel.ShowGeneralGetOrganizationName
import com.unionbankph.corporate.databinding.ActivityBranchVisitChannelBinding

class BranchVisitChannelActivity :
    BaseActivity<ActivityBranchVisitChannelBinding, BranchVisitChannelViewModel>() {

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        generalViewModel.state.observe(this, Observer {
            when (it) {
                is ShowGeneralGetOrganizationName -> {
                    setToolbarTitle(
                        binding.viewToolbar.textViewTitle,
                        binding.viewToolbar.textViewCorporationName,
                        formatString(R.string.title_new_branch_transaction),
                        it.orgName
                    )
                }
            }
        })
        generalViewModel.getOrgName()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        binding.constraintLayoutCashDeposit.setOnClickListener {
            navigator.navigate(
                this,
                BranchVisitFormActivity::class.java,
                Bundle().apply {
                    putString(BranchVisitFormActivity.EXTRA_CHANNEL, "Deposit")
                },
                isClear = false,
                isAnimated = true,
                transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
            )
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

    override val layoutId: Int
        get() = R.layout.activity_branch_visit_channel

    override val viewModelClassType: Class<BranchVisitChannelViewModel>
        get() = BranchVisitChannelViewModel::class.java
}
