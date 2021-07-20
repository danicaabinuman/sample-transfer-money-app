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
import com.unionbankph.corporate.databinding.ActivityManageScheduledTransferBinding
import com.unionbankph.corporate.fund_transfer.presentation.scheduled.scheduled_transfer_done.ManageScheduledTransferDoneFragment
import com.unionbankph.corporate.fund_transfer.presentation.scheduled.scheduled_transfer_ongoing.ManageScheduledTransferOngoingFragment
import io.reactivex.rxkotlin.addTo

class ManageScheduledTransferActivity :
    BaseActivity<ActivityManageScheduledTransferBinding, ManageScheduledTransferViewModel>() {

    private var adapter: ViewPagerAdapter? = null

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
        if (isSME) {
            removeElevation(binding.viewToolbar.root)
            binding.shadowToolbar.isVisible = true
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
                        binding.viewToolbar.textViewTitle,
                        binding.viewToolbar.textViewCorporationName,
                        formatString(R.string.title_manage_scheduled_transfers),
                        it.orgName
                    )
                }
            }
        })
        generalViewModel.getOrgName()
    }

    private fun initViewModel() {
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
        binding.viewPagerManageScheduledTransfers.adapter = adapter
        binding.viewPagerManageScheduledTransfers.offscreenPageLimit = 0
        binding.tabLayoutManageScheduledTransfers.setupWithViewPager(
            binding.viewPagerManageScheduledTransfers,
            false
        )
    }

    fun getViewPager(): ViewPager = binding.viewPagerManageScheduledTransfers

    companion object {
        const val FRAGMENT_ONGOING = "On-Going"
        const val FRAGMENT_DONE = "Done"
    }

    override val layoutId: Int
        get() = R.layout.activity_manage_scheduled_transfer

    override val viewModelClassType: Class<ManageScheduledTransferViewModel>
        get() = ManageScheduledTransferViewModel::class.java
}
