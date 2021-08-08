package com.unionbankph.corporate.payment_link.presentation.create_merchant

import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import kotlinx.android.synthetic.main.activity_merchant_applicaton_received.*

class MerchantApplicationReceivedActivity :
    BaseActivity<MerchantApplicationReceivedViewModel>(R.layout.activity_merchant_applicaton_received) {

    override fun onViewsBound() {
        super.onViewsBound()

        btn_back_to_dashboard.setOnClickListener(){
            finish()
        }
    }
}