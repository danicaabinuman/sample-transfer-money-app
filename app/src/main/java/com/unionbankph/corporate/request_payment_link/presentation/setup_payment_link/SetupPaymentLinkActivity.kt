package com.unionbankph.corporate.request_payment_link.presentation.setup_payment_link

import android.content.Intent
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.request_payment_link.presentation.request_payment.RequestPaymentActivity
import kotlinx.android.synthetic.main.activity_setup_payment_links.*

class SetupPaymentLinkActivity : BaseActivity<SetupPaymentLinkViewModel>(R.layout.activity_setup_payment_links) {

    override fun onViewsBound() {
        super.onViewsBound()
        initViews()
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
    }

    private fun initViews(){

        btnSetupBusinessLink.setOnClickListener{
            val intent = Intent(this, RequestPaymentActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnCancel.setOnClickListener{
            finish()
        }

    }
}