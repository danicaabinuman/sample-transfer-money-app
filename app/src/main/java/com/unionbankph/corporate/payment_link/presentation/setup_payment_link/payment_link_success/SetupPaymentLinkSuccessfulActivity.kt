package com.unionbankph.corporate.payment_link.presentation.setup_payment_link.payment_link_success

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.unionbankph.corporate.R
import com.unionbankph.corporate.payment_link.presentation.request_payment.RequestForPaymentActivity
import kotlinx.android.synthetic.main.activity_setup_payment_link_success.*

class SetupPaymentLinkSuccessfulActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_payment_link_success)

        btnTakeMeThere.setOnClickListener{

            //TODO GO TO DASHBOARD THEN SELECT TRANSACT THEN PAYMENT LINK LIST
            val intent = Intent(this, RequestForPaymentActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}

