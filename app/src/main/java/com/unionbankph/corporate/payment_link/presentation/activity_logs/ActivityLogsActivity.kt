package com.unionbankph.corporate.payment_link.presentation.activity_logs

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.databinding.ActivityActivityLogsBinding
import com.unionbankph.corporate.payment_link.domain.model.PaymentLogsModel
import timber.log.Timber

class ActivityLogsActivity :
        BaseActivity<ActivityActivityLogsBinding, ActivityLogsViewModel>()
{

    private lateinit var paymentLogs: MutableList<PaymentLogsModel>

    override fun beforeLayout(savedInstanceState: Bundle?) {
        super.beforeLayout(savedInstanceState)

        paymentLogs = JsonHelper.fromListJson(intent.getStringExtra(EXTRA_ACTIVITY_LOGS))
        Timber.e("paymentLogs " + JsonHelper.toJson(paymentLogs))
        Timber.e("paymentLogs obj" + paymentLogs.toString())
        Timber.e("paymentLogs extra" + intent.getStringExtra(EXTRA_ACTIVITY_LOGS))
    }

    override fun onViewsBound() {
        super.onViewsBound()

    }

    override fun onViewModelBound() {
        super.onViewModelBound()

    }

    companion object {

        const val EXTRA_ACTIVITY_LOGS = "activity_logs"
    }

    override val viewModelClassType: Class<ActivityLogsViewModel>
        get() = ActivityLogsViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityActivityLogsBinding
        get() = ActivityActivityLogsBinding::inflate

}