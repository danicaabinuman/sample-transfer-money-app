package com.unionbankph.corporate.payment_link.presentation.setup_payment_link.payment_link_success

import android.content.Intent
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.platform.bus.event.TransactSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.dashboard.DashboardViewModel
import com.unionbankph.corporate.payment_link.presentation.onboarding.RequestPaymentSplashActivity
import com.unionbankph.corporate.payment_link.presentation.request_payment.RequestForPaymentActivity
import kotlinx.android.synthetic.main.activity_setup_payment_link_success.*

class SetupPaymentLinkSuccessfulActivity : BaseActivity<SetupPaymentLinkSuccessfulViewModel>(R.layout.activity_setup_payment_link_success) {

    private var fromWhatTab : String? = null

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel = ViewModelProviders.of(
            this,
            viewModelFactory
        )[SetupPaymentLinkSuccessfulViewModel::class.java]
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initViews()
    }

    private fun initViews(){
        fromWhatTab = intent.getStringExtra(RequestPaymentSplashActivity.EXTRA_FROM_WHAT_TAB)
        if(fromWhatTab == null){
            fromWhatTab = DashboardViewModel.FROM_REQUEST_PAYMENT_BUTTON
        }

        btnTakeMeThere.setOnClickListener{

            when(fromWhatTab){
                DashboardViewModel.FROM_REQUEST_PAYMENT_BUTTON -> {
                    val intent = Intent(this, RequestForPaymentActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                DashboardViewModel.FROM_TRANSACT_TAB -> {
                    eventBus.transactSyncEvent.emmit(
                        BaseEvent(TransactSyncEvent.ACTION_GO_TO_PAYMENT_LINK_LIST)
                    )
                    finish()
                }
            }
        }
    }
}

