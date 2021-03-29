package com.unionbankph.corporate.request_payment_link.presentation.setup_payment_link

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.request_payment_link.presentation.request_payment.RequestPaymentActivity
import kotlinx.android.synthetic.main.activity_setup_payment_links.*

class SetupPaymentLink : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_payment_links)

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