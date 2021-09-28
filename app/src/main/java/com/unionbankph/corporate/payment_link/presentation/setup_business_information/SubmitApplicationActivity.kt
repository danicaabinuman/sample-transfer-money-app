package com.unionbankph.corporate.payment_link.presentation.setup_business_information

import android.view.LayoutInflater
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.databinding.ActivitySubmitApplicationBinding

class SubmitApplicationActivity:BaseActivity<ActivitySubmitApplicationBinding,SubmitApplicationViewModel>() {
    override val bindingInflater: (LayoutInflater) -> ActivitySubmitApplicationBinding
        get() = ActivitySubmitApplicationBinding::inflate
    override val viewModelClassType: Class<SubmitApplicationViewModel>
        get() = SubmitApplicationViewModel::class.java
}