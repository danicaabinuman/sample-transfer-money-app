package com.unionbankph.corporate.request_payment_link.presentation.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.unionbankph.corporate.R
import com.unionbankph.corporate.request_payment_link.presentation.request_payment.RequestPaymentActivity
import kotlinx.android.synthetic.main.activity_request_payment_splash.*

class RequestPaymentOnboardingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_payment_splash)

        skipFeatureTips()
    }

    private fun skipFeatureTips(){
        textViewSkip.setOnClickListener{
            val intent = Intent(this, RequestPaymentActivity::class.java)
            startActivity(intent)
        }
    }
}