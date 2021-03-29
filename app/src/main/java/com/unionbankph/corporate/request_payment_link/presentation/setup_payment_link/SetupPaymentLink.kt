package com.unionbankph.corporate.request_payment_link.presentation.setup_payment_link

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.base.BaseViewModel

class SetupPaymentLink : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_payment_links)
<<<<<<< Updated upstream
=======

        btnSetupBusinessLink.setOnClickListener{
            val intent = Intent(this, RequestPaymentActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnCancel.setOnClickListener{
            finish()
        }
>>>>>>> Stashed changes
    }
}