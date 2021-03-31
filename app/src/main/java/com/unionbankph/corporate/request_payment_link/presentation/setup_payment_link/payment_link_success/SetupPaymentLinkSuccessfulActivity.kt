package com.unionbankph.corporate.request_payment_link.presentation.setup_payment_link.payment_link_success

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.unionbankph.corporate.R
import com.unionbankph.corporate.request_payment_link.presentation.request_payment.RequestForPaymentActivity

class SetupPaymentLinkSuccessfulActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_payment_link_success)

//        btnSetupBusinessLink.setOnClickListener{
//            val intent = Intent(this, RequestPaymentActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
//
//        btnCancel.setOnClickListener{
//            finish()
//        }
    }
}

