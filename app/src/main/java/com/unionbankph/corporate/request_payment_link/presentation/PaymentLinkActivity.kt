package com.unionbankph.corporate.request_payment_link.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.unionbankph.corporate.R
import kotlinx.android.synthetic.main.widget_transparent_dashboard_appbar.*

class PaymentLinkActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_link)

        textViewTitle.text = "Payment Links"
    }


}