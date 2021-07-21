package com.unionbankph.corporate.payment_link.presentation.billing_details

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.databinding.ActivityBillingDetailsBinding
import com.unionbankph.corporate.payment_link.domain.model.response.GetPaymentLinkByReferenceIdResponse
import timber.log.Timber
import java.lang.NumberFormatException
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class BillingDetailsActivity :
        BaseActivity<ActivityBillingDetailsBinding, BillingDetailsViewModel>()
{

    override fun onViewModelBound() {
        super.onViewModelBound()
    }

    override fun onViewsBound() {
        super.onViewsBound()


        binding.btnViewMore.setOnClickListener{
//            val intent = Intent(this@BillingDetailsActivity, ActivityLogsActivity::class.java)
//            startActivity(intent)
        }

        backButton()
        setupInputs()
        setupOutputs()
        copyLink()
        shareLink()


    }

    private fun backButton() {
        binding.btnBack.setOnClickListener(){
            finish()
        }
    }

    private fun setupInputs() {

        val referenceNmber = intent.getStringExtra(EXTRA_REFERENCE_NUMBER).toString()
        binding.billingDetailsLoading.visibility = View.VISIBLE
        viewModel.initBundleData(
            referenceNmber
        )

    }

    private fun setupOutputs(){
        viewModel.paymentLinkDetailsResponse.observe(this, Observer {
            binding.billingDetailsLoading.visibility = View.GONE
            updatePaymentLinkDetails(it)
        })

        viewModel.uiState.observe(this, Observer {
            it.getContentIfNotHandled().let { event ->
                when (event) {
                    is UiState.Error -> {
                        binding.billingDetailsLoading.visibility = View.GONE
                        handleOnError(event.throwable)
                    }
                }
            }
        })
    }

    private fun updatePaymentLinkDetails(response: GetPaymentLinkByReferenceIdResponse){
        var grossAmountString = "0.00"
        var feeString = "0.00"
        var feeDouble = 0.00
        var grossAmountDouble = 0.00
        try {
            grossAmountString = response.paymentDetails!!.amount
            grossAmountDouble = grossAmountString.toDouble()
        }catch (e: NumberFormatException){
            Timber.e(e.message)
            e.printStackTrace()
        }

        try {
            feeString = response.paymentDetails!!.fee!!
            feeDouble = feeString.toDouble()
        }catch (e: NumberFormatException){
            Timber.e(e.message)
            e.printStackTrace()
        }

        var netDouble = grossAmountDouble-feeDouble

        val amountFormatFinal = DecimalFormat("PHP #,##0.00")

        try {
            grossAmountString = amountFormatFinal.format(response.paymentDetails?.amount?.toDouble()!!)

        }catch (e: NumberFormatException){
            Timber.e(e.message)
            e.printStackTrace()
        }

        val feeFormatFinal = DecimalFormat("- #,##0.00")
        try {
            feeString = feeFormatFinal.format(response.paymentDetails?.fee?.toDouble()!!)
        }catch (e: NumberFormatException){
            Timber.e(e.message)
            e.printStackTrace()
        }

        binding.tvGrossAmount.text = grossAmountString
        binding.tvFee.text = feeString
        binding.tvNetAmount.text = amountFormatFinal.format(netDouble)

        binding.tvPayorName.text = response.payorDetails?.fullName
        binding.tvPayorEmail.text = response.payorDetails?.emailAddress
        binding.tvPayorContactNumber.text = response.payorDetails?.mobileNumber
        var paymentMethod = response.payorDetails?.paymentMethod

        if (paymentMethod == "INSTAPAY"){
            binding.instapayLogo.visibility = View.VISIBLE
            binding.ubLogo.visibility = View.GONE
        }else if (paymentMethod == "UB ONLINE"){
            binding.instapayLogo.visibility = View.GONE
            binding.ubLogo.visibility = View.VISIBLE
        } else {
            Toast.makeText(this, "no payment method present", Toast.LENGTH_SHORT).show()
        }

        binding.tvRefNumber.text = response.paymentDetails?.referenceNo
        binding.tvReferenceNumberTitle.text = response.paymentDetails?.referenceNo
        binding.tvAmount.text = grossAmountString
        binding.tvDescription.text = response.paymentDetails?.paymentFor
        binding.tvRemarks.text = response.paymentDetails?.description
        binding.tvLinkUrl.text = response.paymentDetails?.paymentLink

        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH)
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val formatter = SimpleDateFormat("MMM dd, yyyy , hh:mm aa", Locale.ENGLISH)
        formatter.timeZone = TimeZone.getDefault()

        var settlementDateString = "UNAVAILABLE"
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()

        val localDate = Date()
        var settlementDateLocal : Date? = null

        response.paymentDetails?.settlementDate?.let {
            settlementDateString = it
            try {
                settlementDateString = sdf.format(parser.parse(it))
                settlementDateLocal = sdf.parse(settlementDateString)
            } catch (e: Exception){
                Timber.e(e.toString())
            }
        }

        var settledDateString = "UNAVAILABLE"
        response.paymentDetails?.settledDate?.let {
            settledDateString = it
            try {
                settledDateString = formatter.format(parser.parse(it))
            } catch (e: Exception){
                Timber.e(e.toString())
            }
        }

        if(settlementDateLocal!=null){
            val elapsedTimeSinceSettlementDate = settlementDateLocal!!.time - localDate.time
            if(elapsedTimeSinceSettlementDate>0){
                binding.tvExpiryInformation.text = "Payment will be eligible for payout on " + settlementDateString
            }else{
                binding.tvExpiryInformation.text = "Payment has been eligible for payout since " + settlementDateString
            }
        }else{
            binding.tvExpiryInformation.text = "Payment settlement date not available"
        }


        binding.tvBillingDetailsDateSettled.text = settledDateString

        var expiryDateString = "UNAVAILABLE"
        response.expiry?.let {
            expiryDateString = it
            try {
                expiryDateString = formatter.format(parser.parse(it))
            } catch (e: Exception){
                Timber.e(e.toString()) // this never gets called either
            }
        }
        binding.tvLinkExpiry.text = expiryDateString

    }


    companion object{
        const val EXTRA_REFERENCE_NUMBER = "extra_reference_number"
    }

    private fun copyLink(){
        binding.ivCopyButton.setOnClickListener{
            val copiedUrl = binding.tvLinkUrl.text
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied to clipboard", copiedUrl)

            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()

            clipboard.setPrimaryClip(clip)

//            showToast("Copied to clipboard")
        }
    }

    private fun shareLink() {
        binding.ivShareButton.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.putExtra(Intent.EXTRA_TEXT, binding.tvLinkUrl.text.toString())
            intent.type = "text/plain"
            startActivity(Intent.createChooser(intent, "Share To:"))
        }
    }

    override val layoutId: Int
        get() = R.layout.activity_billing_details

    override val viewModelClassType: Class<BillingDetailsViewModel>
        get() = BillingDetailsViewModel::class.java
}