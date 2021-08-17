package com.unionbankph.corporate.payment_link.presentation.create_merchant

import android.view.LayoutInflater
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.databinding.ActivityMerchantApplicatonRejectedBinding

class MerchantApplicationRejectedActivity:
    BaseActivity<ActivityMerchantApplicatonRejectedBinding, MerchantApplicationReceivedViewModel>() {

    override val bindingInflater: (LayoutInflater) -> ActivityMerchantApplicatonRejectedBinding
        get() = ActivityMerchantApplicatonRejectedBinding::inflate

    override val viewModelClassType: Class<MerchantApplicationReceivedViewModel>
        get() = MerchantApplicationReceivedViewModel::class.java
}