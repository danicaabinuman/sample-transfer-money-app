package com.unionbankph.corporate.payment_link.presentation.activity_logs

import android.content.Intent
import android.view.LayoutInflater
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.databinding.ActivityActivityLogsBinding

class ActivityLogsActivity :
        BaseActivity<ActivityActivityLogsBinding, ActivityLogsViewModel>()
{
    override fun onViewsBound() {
        super.onViewsBound()

    }

    override fun onViewModelBound() {
        super.onViewModelBound()

    }

    override val viewModelClassType: Class<ActivityLogsViewModel>
        get() = ActivityLogsViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityActivityLogsBinding
        get() = ActivityActivityLogsBinding::inflate

}