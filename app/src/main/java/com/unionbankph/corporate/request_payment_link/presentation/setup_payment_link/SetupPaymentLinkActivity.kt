package com.unionbankph.corporate.request_payment_link.presentation.setup_payment_link

import android.content.Intent
import android.text.Html
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.request_payment_link.presentation.request_payment.RequestForPaymentActivity
import com.unionbankph.corporate.request_payment_link.presentation.setup_payment_link.terms_of_service.TermsOfServiceActivity
import kotlinx.android.synthetic.main.activity_setup_payment_links.*

class SetupPaymentLinkActivity : BaseActivity<SetupPaymentLinkViewModel>(R.layout.activity_setup_payment_links) {

    override fun onViewsBound() {
        super.onViewsBound()
        initViews()
    }

    private fun initViews(){
        initTermsAndCondition()

        btnSetupBusinessLink.setOnClickListener{
            val intent = Intent(this, RequestForPaymentActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnCancel.setOnClickListener{
            finish()
        }

    }

    private fun initTermsAndCondition(){
        cb_fnc_tnc.text = Html.fromHtml("I have read the " +
                "<a href=''> Fees and Charges</a>" +
                " and agree to the " +
                "<a href=''>Terms and Condition</a>" +
                " of the Request for Payment")
        cb_fnc_tnc.isClickable = true
        cb_fnc_tnc.setOnClickListener{
            val intent = Intent(this, TermsOfServiceActivity::class.java)
            startActivity(intent)
        }
//        cb_fnc_tnc.movementMethod = LinkMovementMethod.getInstance()
    }
}