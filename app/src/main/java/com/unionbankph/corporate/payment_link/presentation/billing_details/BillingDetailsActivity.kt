package com.unionbankph.corporate.payment_link.presentation.billing_details

import android.content.Intent
import androidx.lifecycle.Observer
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.common.presentation.callback.AccountAdapterCallback
import com.unionbankph.corporate.payment_link.presentation.activity_logs.ActivityLogsActivity
import kotlinx.android.synthetic.main.activity_billing_details.*
import kotlinx.android.synthetic.main.activity_nominate_settlement.*

class BillingDetailsActivity :
        BaseActivity<BillingDetailsViewModel>(R.layout.activity_billing_details)
{
    override fun onViewsBound() {
        super.onViewsBound()


        btnViewMore.setOnClickListener{
            val intent = Intent(this@BillingDetailsActivity, ActivityLogsActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onViewModelBound() {
        super.onViewModelBound()

    }

}