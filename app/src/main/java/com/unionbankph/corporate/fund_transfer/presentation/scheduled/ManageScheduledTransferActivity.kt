package com.unionbankph.corporate.fund_transfer.presentation.scheduled

import android.os.Bundle
import android.view.MenuItem
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.platform.bus.event.InputSyncEvent
import com.unionbankph.corporate.app.common.widget.recyclerview.viewpager.ViewPagerAdapter
import com.unionbankph.corporate.common.presentation.viewmodel.ShowGeneralGetOrganizationName
import com.unionbankph.corporate.fund_transfer.presentation.scheduled.scheduled_transfer_done.ManageScheduledTransferDoneFragment
import com.unionbankph.corporate.fund_transfer.presentation.scheduled.scheduled_transfer_ongoing.ManageScheduledTransferOngoingFragment
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_manage_scheduled_transfer.*
import kotlinx.android.synthetic.main.activity_manage_scheduled_transfer.shadow_toolbar
import kotlinx.android.synthetic.main.activity_manage_scheduled_transfer.viewToolbar
import kotlinx.android.synthetic.main.activity_privacy_policy.*
import kotlinx.android.synthetic.main.widget_transparent_org_appbar.*

class ManageScheduledTransferActivity :
    BaseActivity<ManageScheduledTransferViewModel>(R.layout.activity_manage_scheduled_transfer) {

    private var adapter: ViewPagerAdapter? = null

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
        if (isSME) {
            removeElevation(viewToolbar)
            shadow_toolbar.isVisible = true
        }
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initGeneralViewModel()
        initViewModel()
    }

    override fun onViewsBound() {
        super.onViewsBound()
        setupViewPager()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        eventBus.inputSyncEvent.flowable.subscribe {
            if (it.eventType == InputSyncEvent.ACTION_INPUT_BENEFICIARY ||
                it.eventType == InputSyncEvent.ACTION_INPUT_OWN_ACCOUNT
            ) {
                onBackPressed()
            }
        }.addTo(disposables)
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

    override fun onBackPressed() {
        onBackPressed(false)
        overridePendingTransition(R.anim.anim_no_change, R.anim.anim_slide_down)
    }

    private fun initGeneralViewModel() {
        generalViewModel.state.observe(this, Observer {
            when (it) {
                is ShowGeneralGetOrganizationName -> {
                    setToolbarTitle(
                        textViewTitle,
                        textViewCorporationName,
                        formatString(R.string.title_manage_scheduled_transfers),
                        it.orgName
                    )
                }
            }
        })
        generalViewModel.getOrgName()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(
            this,
            viewModelFactory
        )[ManageScheduledTransferViewModel::class.java]
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowManageScheduledTransferError -> {
                    handleOnError(it.throwable)
                }
            }
        })
    }

    private fun setupViewPager() {
        adapter = ViewPagerAdapter(
            supportFragmentManager
        )
        adapter?.addFragment(ManageScheduledTransferOngoingFragment.newInstance(), FRAGMENT_ONGOING)
        adapter?.addFragment(ManageScheduledTransferDoneFragment.newInstance(), FRAGMENT_DONE)
        viewPagerManageScheduledTransfers.adapter = adapter
        viewPagerManageScheduledTransfers.offscreenPageLimit = 0
        tabLayoutManageScheduledTransfers.setupWithViewPager(
            viewPagerManageScheduledTransfers,
            false
        )
    }

    fun getViewPager(): ViewPager = viewPagerManageScheduledTransfers

    companion object {
        const val FRAGMENT_ONGOING = "On-Going"
        const val FRAGMENT_DONE = "Done"
    }
}
