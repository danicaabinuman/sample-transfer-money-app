package com.unionbankph.corporate.request_payment_link.presentation.onboarding

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.unionbankph.corporate.R
import com.unionbankph.corporate.common.data.source.local.sharedpref.SharedPreferenceUtil
import com.unionbankph.corporate.request_payment_link.presentation.request_payment.RequestPaymentActivity
import kotlinx.android.synthetic.main.activity_request_payment_splash.imageViewBack
import kotlinx.android.synthetic.main.activity_request_payment_splash.textViewSkip
import javax.inject.Inject

class RequestPaymentOnboardingActivity : AppCompatActivity() {

    val prevs : String = "go"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_payment_splash)



    }

    override fun onResume() {
        super.onResume()

        val sharedPreferences : SharedPreferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
        if (sharedPreferences.getBoolean(prevs, false)){
            val editor : SharedPreferences.Editor = sharedPreferences.edit()
            editor.putBoolean(prevs, Boolean.equals(true))
            editor.apply()
        } else {
            skipFeatureTips()
        }
    }
    private fun skipFeatureTips(){
        textViewSkip.setOnClickListener{
            val intent = Intent(this, RequestPaymentActivity::class.java)
            startActivity(intent)
        }
    }
}