package com.unionbankph.corporate.request_payment_link.presentation.onboarding

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.request_payment_link.presentation.request_payment.RequestForPaymentActivity
import kotlinx.android.synthetic.main.activity_request_payment_splash.textViewSkip

class RequestPaymentOnboardingActivity : AppCompatActivity() {

    val prevs : String = "go"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_payment_splash)

        sharedPreferences()
        skipFeatureTips()

    }

    override fun onResume() {
        super.onResume()

    }

    private fun sharedPreferences(){
        val sharedPreferences : SharedPreferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
        if (sharedPreferences.getBoolean(prevs, false)){
            val editor : SharedPreferences.Editor = sharedPreferences.edit()
            editor.putBoolean(prevs, true)
            editor.apply()
        } else {
            moveToNextActivity()
        }
    }

    private fun skipFeatureTips(){
        textViewSkip.setOnClickListener{
            moveToNextActivity()
        }
    }

    private fun moveToNextActivity(){
        val intent = Intent(this, RequestForPaymentActivity::class.java)
        startActivity(intent)
    }

    override fun onBackPressed() {
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
    }
}