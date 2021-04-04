package com.unionbankph.corporate.request_payment_link.presentation.setup_payment_link.nominate_settlement_account

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.unionbankph.corporate.R

class NominateSettlementActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nominate_settlement)
    }


    companion object {
        const val EXTRA_ACCOUNTS = "extra_accounts"
    }
}