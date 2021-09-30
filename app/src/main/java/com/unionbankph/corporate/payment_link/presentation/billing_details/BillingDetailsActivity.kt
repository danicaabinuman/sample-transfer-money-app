package com.unionbankph.corporate.payment_link.presentation.billing_details


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.Observer
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.bills_payment.presentation.frequent_biller_form.ManageFrequentBillerFormActivity
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.helper.ConstantHelper
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.databinding.ActivityBillingDetailsBinding
import com.unionbankph.corporate.payment_link.domain.model.PaymentLogsModel
import com.unionbankph.corporate.payment_link.domain.model.response.GetPaymentLinkByReferenceIdResponse
import com.unionbankph.corporate.payment_link.domain.model.response.GetPaymentLogsResponse
import com.unionbankph.corporate.payment_link.presentation.activity_logs.ActivityLogsActivity
import com.unionbankph.corporate.payment_link.presentation.activity_logs.PaymentHistoryLogsAdapter
import timber.log.Timber
import java.lang.NumberFormatException
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class BillingDetailsActivity :
    BaseActivity<ActivityBillingDetailsBinding, BillingDetailsViewModel>() {

    private lateinit var mAdapter: PaymentHistoryLogsAdapter

    override fun onViewModelBound() {
        super.onViewModelBound()
        Timber.e("onViewModelBound Test")
        setupOutputs()
    }

    override fun onViewsBound() {
        super.onViewsBound()

        initEvents()
        initRecyclerView()
        setupInputs()
        buttonViewMore()
    }

    private fun initEvents() {

        binding.btnBack.setOnClickListener { onBackPressed() }

        binding.cardViewMore.root.setOnClickListener { navigateToActivityLogs() }
    }

    private fun setupInputs() {
        val referenceNumber = intent.getStringExtra(EXTRA_REFERENCE_NUMBER).toString()
        viewModel.initBundleData(referenceNumber)
    }

    private fun initRecyclerView(){

        mAdapter = PaymentHistoryLogsAdapter(context, PaymentHistoryLogsAdapter.BILLING_ADAPTER)
        binding.rvPaymentLogs.layoutManager = getLinearLayoutManager()
        binding.rvPaymentLogs.adapter = mAdapter
        binding.rvPaymentLogs.visibility = View.VISIBLE
        Timber.e("initViews shiiing")
    }

    private fun setupOutputs() {

        viewModel.uiState.observe(this, EventObserver {
            when (it) {
                is UiState.Loading -> {
                    showProgressAlertDialog(this::class.java.simpleName)
                }
                is UiState.Complete -> {
                    dismissProgressAlertDialog()
                }
                is UiState.Error -> {
                    handleOnError(it.throwable)
                }
            }
        })

        viewModel.state.observe(this, {
            updatePaymentLinkDetails(it.paymentDetails!!)
            mAdapter.appendData(it.paymentLogs?.take(3) ?: mutableListOf())
//            initLogs(it.paymentLogs ?: mutableListOf())
        })
    }

    private fun navigateToActivityLogs() {
        val bundle = Bundle().apply {
            putString(
                ActivityLogsActivity.EXTRA_ACTIVITY_LOGS,
                JsonHelper.toJson(viewModel.state.value?.paymentLogs)
            )
            Timber.e("navigateToActivityLogs " + JsonHelper.toJson(viewModel.state.value?.paymentLogs))
        }
        navigator.navigate(
            this,
            ActivityLogsActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun updatePaymentLinkDetails(response: GetPaymentLinkByReferenceIdResponse) {
        var grossAmountString = "0.00"
        var feeString = "0.00"
        var feeDouble = 0.00
        var grossAmountDouble = 0.00
        try {
            grossAmountString = response.paymentDetails!!.amount
            grossAmountDouble = grossAmountString.toDouble()
        } catch (e: NumberFormatException) {
            Timber.e(e.message)
            e.printStackTrace()
        }

        try {
            feeString = response.paymentDetails!!.fee!!
            feeDouble = feeString.toDouble()
        } catch (e: NumberFormatException) {
            Timber.e(e.message)
            e.printStackTrace()
        }

        var netDouble = grossAmountDouble - feeDouble

        val amountFormatFinal = DecimalFormat("PHP #,##0.00")

        try {
            grossAmountString =
                amountFormatFinal.format(response.paymentDetails?.amount?.toDouble()!!)

        } catch (e: NumberFormatException) {
            Timber.e(e.message)
            e.printStackTrace()
        }

        val feeFormatFinal = DecimalFormat("- #,##0.00")
        try {
            feeString = feeFormatFinal.format(response.paymentDetails?.fee?.toDouble()!!)
        } catch (e: NumberFormatException) {
            Timber.e(e.message)
            e.printStackTrace()
        }

        binding.tvGrossAmount.text = grossAmountString
        binding.tvFee.text = feeString
        binding.tvNetAmount.text = amountFormatFinal.format(netDouble)

        binding.tvPayorName.text = response.payorDetails?.fullName
        binding.tvPayorEmail.text = response.payorDetails?.emailAddress
        binding.tvPayorContactNumber.text = response.payorDetails?.mobileNumber
        val paymentMethod = response.payorDetails?.paymentMethod

        binding.imageViewLogo.setImageResource(
            ConstantHelper.Drawable.getPaymentMethodLogo(
            paymentMethod ?: Constant.PaymentMethod.UNKNOWN
            )
        )

        binding.apply {
            tvRefNumber.text = response.paymentDetails?.referenceNo
            tvReferenceNumberTitle.text = response.paymentDetails?.referenceNo
            tvAmount.text = grossAmountString
            tvDescription.text = response.paymentDetails?.paymentFor
            tvRemarks.text = response.paymentDetails?.description
            tvLinkUrl.text = response.paymentDetails?.paymentLink
        }

        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH)
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val formatter = SimpleDateFormat("MMM dd, yyyy , hh:mm aa", Locale.ENGLISH)
        formatter.timeZone = TimeZone.getDefault()

        var settlementDateString = "UNAVAILABLE"
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()

        val localDate = Date()
        var settlementDateLocal: Date? = null

        response.paymentDetails?.settlementDate?.let {
            settlementDateString = it
            try {
                settlementDateString = sdf.format(parser.parse(it))
                settlementDateLocal = sdf.parse(settlementDateString)
            } catch (e: Exception) {
                Timber.e(e.toString())
            }
        }

        var settledDateString = "UNAVAILABLE"
        response.paymentDetails?.settledDate?.let {
            settledDateString = it
            try {
                settledDateString = formatter.format(parser.parse(it))
            } catch (e: Exception) {
                Timber.e(e.toString())
            }
        }

        if (settlementDateLocal != null) {
            val elapsedTimeSinceSettlementDate = settlementDateLocal!!.time - localDate.time
            if (elapsedTimeSinceSettlementDate > 0) {
                binding.tvExpiryInformation.text =
                    "Payment will be eligible for payout on " + settlementDateString
            } else {
                binding.tvExpiryInformation.text =
                    "Payment has been eligible for payout since " + settlementDateString
            }
        } else {
            binding.tvExpiryInformation.text = "Payment settlement date not available"
        }


        binding.tvBillingDetailsDateSettled.text = settledDateString

        var expiryDateString = "UNAVAILABLE"
        response.expiry?.let {
            expiryDateString = it
            try {
                expiryDateString = formatter.format(parser.parse(it))
            } catch (e: Exception) {
                Timber.e(e.toString()) // this never gets called either
            }
        }
        binding.tvLinkExpiry.text = expiryDateString

    }

    private fun buttonViewMore(){
        binding.cardViewMore.root.setOnClickListener { navigateToActivityLogs() }
    }

    companion object {
        const val EXTRA_REFERENCE_NUMBER = "extra_reference_number"
    }

//    private fun copyLink(){
//        ivCopyButton.setOnClickListener{
//            val copiedUrl = tvLinkUrl.text
//            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
//            val clip = ClipData.newPlainText("Copied to clipboard", copiedUrl)
//
//            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
//
//            clipboard.setPrimaryClip(clip)
//
////            showToast("Copied to clipboard")
//        }
//    }
//
//    private fun shareLink() {
//        ivShareButton.setOnClickListener {
//            val intent = Intent()
//            intent.action = Intent.ACTION_SEND
//            intent.putExtra(Intent.EXTRA_TEXT, tvLinkUrl.text.toString())
//            intent.type = "text/plain"
//            startActivity(Intent.createChooser(intent, "Share To:"))
//        }
//    }

    override val viewModelClassType: Class<BillingDetailsViewModel>
        get() = BillingDetailsViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityBillingDetailsBinding
        get() = ActivityBillingDetailsBinding::inflate
}