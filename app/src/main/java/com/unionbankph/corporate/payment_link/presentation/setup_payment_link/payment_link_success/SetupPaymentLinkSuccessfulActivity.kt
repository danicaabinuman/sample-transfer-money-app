package com.unionbankph.corporate.payment_link.presentation.setup_payment_link.payment_link_success

import android.content.Intent
import android.view.LayoutInflater
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.platform.bus.event.TransactSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.dashboard.DashboardViewModel
import com.unionbankph.corporate.databinding.ActivitySetupPaymentLinkSuccessBinding
import com.unionbankph.corporate.payment_link.presentation.onboarding.RequestPaymentSplashActivity
import com.unionbankph.corporate.payment_link.presentation.request_payment.RequestForPaymentActivity

class SetupPaymentLinkSuccessfulActivity :
    BaseActivity<ActivitySetupPaymentLinkSuccessBinding, SetupPaymentLinkSuccessfulViewModel>() {

    private var fromWhatTab : String? = null

    override fun onViewModelBound() {
        super.onViewModelBound()

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

        binding.btnTakeMeThere.setOnClickListener{

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

    override val viewModelClassType: Class<SetupPaymentLinkSuccessfulViewModel>
        get() = SetupPaymentLinkSuccessfulViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivitySetupPaymentLinkSuccessBinding
        get() = ActivitySetupPaymentLinkSuccessBinding::inflate
}

