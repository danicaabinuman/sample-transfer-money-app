package com.unionbankph.corporate.payment_link.presentation.activity_logs

import android.content.Intent
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

    override val layoutId: Int
        get() = R.layout.activity_activity_logs

    override val viewModelClassType: Class<ActivityLogsViewModel>
        get() = ActivityLogsViewModel::class.java

}