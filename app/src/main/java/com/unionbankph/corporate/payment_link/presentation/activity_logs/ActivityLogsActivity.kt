package com.unionbankph.corporate.payment_link.presentation.activity_logs

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.databinding.ActivityActivityLogsBinding
import com.unionbankph.corporate.payment_link.domain.model.PaymentLogsModel
import com.unionbankph.corporate.payment_link.domain.model.response.GetPaymentLogsResponse
import com.unionbankph.corporate.payment_link.presentation.billing_details.BillingDetailsActivity
import timber.log.Timber

class ActivityLogsActivity :
        BaseActivity<ActivityActivityLogsBinding, ActivityLogsViewModel>()
{

    lateinit var mAdapter: PaymentHistoryLogsAdapter

    private lateinit var paymentLogs: MutableList<PaymentLogsModel>

    override fun onViewsBound() {
        super.onViewsBound()
        initRecyclerView()
    }

    private fun initRecyclerView(){
        mAdapter = PaymentHistoryLogsAdapter(context, PaymentHistoryLogsAdapter.LOGS_ADAPTER)
        binding.rvPaymentLogs.layoutManager = getLinearLayoutManager()
        binding.rvPaymentLogs.adapter = mAdapter

        paymentLogs = JsonHelper.fromListJson(intent.getStringExtra(EXTRA_ACTIVITY_LOGS))
        mAdapter.appendData(paymentLogs)

        val label = getString(R.string.action_retake_id)
        Timber.e("initRecyclerView checking")
    }
    companion object {

        const val EXTRA_ACTIVITY_LOGS = "activity_logs"

    }

    override val viewModelClassType: Class<ActivityLogsViewModel>
        get() = ActivityLogsViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityActivityLogsBinding
        get() = ActivityActivityLogsBinding::inflate

}