package com.unionbankph.corporate.fund_transfer.presentation.beneficiary_selection

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.view.isVisible
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.presentation.own_account.OwnAccountFragment
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.platform.bus.event.InputSyncEvent
import com.unionbankph.corporate.app.common.widget.recyclerview.viewpager.ViewPagerAdapter
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_beneficiary.*
import kotlinx.android.synthetic.main.widget_transparent_appbar.*

class BeneficiaryActivity : BaseActivity<BeneficiaryViewModel>(R.layout.activity_beneficiary) {

    private var adapter: ViewPagerAdapter? = null

    var isInitialLoad = true

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
        if (isSME) {
            removeElevation(viewToolbar)
            shadow_toolbar.isVisible = true
        }
        setToolbarTitle(tvToolbar, getString(R.string.title_select_beneficiary))
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
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

    private fun setupViewPager() {
        adapter = ViewPagerAdapter(
            supportFragmentManager
        )
        adapter?.addFragment(
            BeneficiaryFragment.newInstance(
                intent.getStringExtra(EXTRA_ID).notNullable(),
                intent.getStringExtra(EXTRA_SOURCE_ACCOUNT_ID).notNullable()
            ),
            FRAGMENT_BENEFICIARIES
        )
        if (intent.getStringExtra(EXTRA_PAGE) != null) {
            adapter?.addFragment(
                OwnAccountFragment.newInstance(
                    intent.getStringExtra(EXTRA_SOURCE_ACCOUNT_ID).notNullable(),
                    intent.getStringExtra(EXTRA_DESTINATION_ACCOUNT_ID),
                    intent.getStringExtra(EXTRA_CHANNEL_ID).notNullable(),
                    intent.getStringExtra(EXTRA_PERMISSION_ID).notNullable(),
                    intent.getStringExtra(EXTRA_CURRENCY).notNullable()
                ), FRAGMENT_OWN_ACCOUNT
            )
        } else {
            tabLayoutBeneficiary.visibility = View.GONE
        }

        viewPagerBeneficiary.adapter = adapter
        viewPagerBeneficiary.offscreenPageLimit = 0
        if (intent.getStringExtra(EXTRA_DESTINATION_ACCOUNT_ID) != null &&
            intent.getStringExtra(EXTRA_PAGE) != null
        ) {
            viewPagerBeneficiary.currentItem = 1
        }
        tabLayoutBeneficiary.setupWithViewPager(viewPagerBeneficiary, false)
    }

    fun setCurrentViewPager(currentItem: Int) {
        if (intent.getStringExtra(EXTRA_DESTINATION_ACCOUNT_ID) == null &&
            intent.getStringExtra(EXTRA_PAGE) != null
        ) {
            viewPagerBeneficiary.currentItem = currentItem
        }
    }

    companion object {
        const val FRAGMENT_BENEFICIARIES = "Beneficiaries"
        const val FRAGMENT_OWN_ACCOUNT = "Own Accounts"
        const val EXTRA_ID = "id"
        const val EXTRA_PAGE = "page"
        const val EXTRA_PERMISSION = "permission"
        const val EXTRA_CURRENCY = "currency"

        const val EXTRA_DESTINATION_ACCOUNT_ID = "destination_account_id"
        const val EXTRA_SOURCE_ACCOUNT_ID = "source_account_id"
        const val EXTRA_CHANNEL_ID = "channel_id"
        const val EXTRA_PERMISSION_ID = "permission_id"

        const val PAGE_UBP_FORM = "ubp_form"
    }
}
