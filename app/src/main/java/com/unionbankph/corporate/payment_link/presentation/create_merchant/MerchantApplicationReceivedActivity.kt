package com.unionbankph.corporate.payment_link.presentation.create_merchant

import android.view.LayoutInflater
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.databinding.ActivityMerchantApplicatonReceivedBinding


class MerchantApplicationReceivedActivity :
    BaseActivity<ActivityMerchantApplicatonReceivedBinding, MerchantApplicationReceivedViewModel>() {

    override fun onViewsBound() {
        super.onViewsBound()

        binding.btnBackToDashboard.setOnClickListener(){
            finish()
        }
    }

    override val bindingInflater: (LayoutInflater) -> ActivityMerchantApplicatonReceivedBinding
        get() = ActivityMerchantApplicatonReceivedBinding::inflate

    override val viewModelClassType: Class<MerchantApplicationReceivedViewModel>
        get() = MerchantApplicationReceivedViewModel::class.java
}