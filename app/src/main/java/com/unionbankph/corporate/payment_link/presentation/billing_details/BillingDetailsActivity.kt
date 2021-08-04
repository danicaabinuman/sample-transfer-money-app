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
import com.unionbankph.corporate.payment_link.domain.model.response.GetPaymentLinkByReferenceIdResponse
import kotlinx.android.synthetic.main.activity_billing_details.*
import kotlinx.android.synthetic.main.activity_link_details.*
import timber.log.Timber
import java.lang.NumberFormatException
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class BillingDetailsActivity :
        BaseActivity<BillingDetailsViewModel>(R.layout.activity_billing_details)
{

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel = ViewModelProviders.of(
            this,
            viewModelFactory
        )[BillingDetailsViewModel::class.java]
    }

    override fun onViewsBound() {
        super.onViewsBound()


//        btnViewMore.setOnClickListener{
//            val intent = Intent(this@BillingDetailsActivity, ActivityLogsActivity::class.java)
//            startActivity(intent)
//        }

        backButton()
        setupInputs()
        setupOutputs()

    }

    private fun backButton() {
        btnBack.setOnClickListener(){
            finish()
        }
    }

    private fun setupInputs() {

        val referenceNmber = intent.getStringExtra(EXTRA_REFERENCE_NUMBER).toString()
        billingDetailsLoading.visibility = View.VISIBLE
        viewModel.initBundleData(
            referenceNmber
        )

    }

    private fun setupOutputs(){
        viewModel.paymentLinkDetailsResponse.observe(this, Observer {
            billingDetailsLoading.visibility = View.GONE
            updatePaymentLinkDetails(it)
        })

        viewModel.uiState.observe(this, Observer {
            it.getContentIfNotHandled().let { event ->
                when (event) {
                    is UiState.Error -> {
                        billingDetailsLoading.visibility = View.GONE
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

        tvGrossAmount.text = grossAmountString
        tvFee.text = feeString
        tvNetAmount.text = amountFormatFinal.format(netDouble)

        tvPayorName.text = response.payorDetails?.fullName
        tvPayorEmail.text = response.payorDetails?.emailAddress
        tvPayorContactNumber.text = response.payorDetails?.mobileNumber
        var paymentMethod = response.payorDetails?.paymentMethod

        if (paymentMethod == "INSTAPAY"){
            instapayLogo.visibility = View.VISIBLE
            ubLogo.visibility = View.GONE
            bayadCenterLogo.visibility = View.GONE
            ecpayLogo.visibility = View.GONE
            lbcLogo.visibility = View.GONE
            palawanLogo.visibility = View.GONE
            cebuanaLogo.visibility = View.GONE
            grabpayLogo.visibility = View.GONE
            gcashLogo.visibility = View.GONE
        } else if (paymentMethod == "GCASH"){
            instapayLogo.visibility = View.GONE
            ubLogo.visibility = View.GONE
            bayadCenterLogo.visibility = View.GONE
            ecpayLogo.visibility = View.GONE
            lbcLogo.visibility = View.GONE
            palawanLogo.visibility = View.GONE
            cebuanaLogo.visibility = View.GONE
            grabpayLogo.visibility = View.GONE
            gcashLogo.visibility = View.VISIBLE
        } else if (paymentMethod == "GRABPAY"){
            instapayLogo.visibility = View.GONE
            ubLogo.visibility = View.GONE
            bayadCenterLogo.visibility = View.GONE
            ecpayLogo.visibility = View.GONE
            lbcLogo.visibility = View.GONE
            palawanLogo.visibility = View.GONE
            cebuanaLogo.visibility = View.GONE
            grabpayLogo.visibility = View.VISIBLE
            gcashLogo.visibility = View.GONE
        } else if (paymentMethod == "UB ONLINE"){
            instapayLogo.visibility = View.GONE
            ubLogo.visibility = View.VISIBLE
            bayadCenterLogo.visibility = View.GONE
            ecpayLogo.visibility = View.GONE
            lbcLogo.visibility = View.GONE
            palawanLogo.visibility = View.GONE
            cebuanaLogo.visibility = View.GONE
            gcashLogo.visibility = View.GONE
            grabpayLogo.visibility = View.GONE
        }
        else if (paymentMethod == "CEBUANALHUILLIER"){
            instapayLogo.visibility = View.GONE
            ubLogo.visibility = View.GONE
            bayadCenterLogo.visibility = View.GONE
            ecpayLogo.visibility = View.GONE
            lbcLogo.visibility = View.GONE
            palawanLogo.visibility = View.GONE
            cebuanaLogo.visibility = View.VISIBLE
            gcashLogo.visibility = View.GONE
            grabpayLogo.visibility = View.GONE
        }
        else if (paymentMethod == "PALAWAN"){
            instapayLogo.visibility = View.GONE
            ubLogo.visibility = View.GONE
            bayadCenterLogo.visibility = View.GONE
            ecpayLogo.visibility = View.GONE
            lbcLogo.visibility = View.GONE
            palawanLogo.visibility = View.VISIBLE
            cebuanaLogo.visibility = View.GONE
            gcashLogo.visibility = View.GONE
            grabpayLogo.visibility = View.GONE
        }
        else if (paymentMethod == "LBC"){
            instapayLogo.visibility = View.GONE
            ubLogo.visibility = View.GONE
            bayadCenterLogo.visibility = View.GONE
            ecpayLogo.visibility = View.GONE
            lbcLogo.visibility = View.VISIBLE
            palawanLogo.visibility = View.GONE
            cebuanaLogo.visibility = View.GONE
            gcashLogo.visibility = View.GONE
            grabpayLogo.visibility = View.GONE
        }
        else if (paymentMethod == "ECPAY"){
            instapayLogo.visibility = View.GONE
            ubLogo.visibility = View.GONE
            bayadCenterLogo.visibility = View.GONE
            ecpayLogo.visibility = View.VISIBLE
            lbcLogo.visibility = View.GONE
            palawanLogo.visibility = View.GONE
            cebuanaLogo.visibility = View.GONE
            gcashLogo.visibility = View.GONE
            grabpayLogo.visibility = View.GONE
        }
        else if (paymentMethod == "BAYADCENTER"){
            instapayLogo.visibility = View.VISIBLE
            ubLogo.visibility = View.GONE
            bayadCenterLogo.visibility = View.GONE
            ecpayLogo.visibility = View.GONE
            lbcLogo.visibility = View.GONE
            palawanLogo.visibility = View.GONE
            cebuanaLogo.visibility = View.GONE
            gcashLogo.visibility = View.GONE
            grabpayLogo.visibility = View.GONE
        }
        else {
            instapayLogo.visibility = View.GONE
            ubLogo.visibility = View.GONE
            bayadCenterLogo.visibility = View.GONE
            ecpayLogo.visibility = View.GONE
            lbcLogo.visibility = View.GONE
            palawanLogo.visibility = View.GONE
            cebuanaLogo.visibility = View.GONE
            gcashLogo.visibility = View.GONE
            grabpayLogo.visibility = View.GONE
            
            if (paymentMethod.toString() == "ECPY") {
                paymentMethodText.text = "EcPay"
            } else if (paymentMethod.toString() == "BAYD") {
                paymentMethodText.text = "Bayad Center"
            } else if (paymentMethod.toString() == "PLWN") {
                paymentMethodText.text = "Palawan"
            } else if (paymentMethod.toString() == "CEBL") {
                paymentMethodText.text = "Cebuana"
            }
            paymentMethodText.visibility = View.VISIBLE
        }

        tvRefNumber.text = response.paymentDetails?.referenceNo
        tvReferenceNumberTitle.text = response.paymentDetails?.referenceNo
        tvAmount.text = grossAmountString
        tvDescription.text = response.paymentDetails?.paymentFor
        tvRemarks.text = response.paymentDetails?.description
        tvLinkUrl.text = response.paymentDetails?.paymentLink

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
                tvExpiryInformation.text = "Payment will be eligible for payout on " + settlementDateString
            }else{
                tvExpiryInformation.text = "Payment has been eligible for payout since " + settlementDateString
            }
        }else{
            tvExpiryInformation.text = "Payment settlement date not available"
        }


        tvBillingDetailsDateSettled.text = settledDateString

        var expiryDateString = "UNAVAILABLE"
        response.expiry?.let {
            expiryDateString = it
            try {
                expiryDateString = formatter.format(parser.parse(it))
            } catch (e: Exception){
                Timber.e(e.toString()) // this never gets called either
            }
        }
        tvLinkExpiry.text = expiryDateString

    }


    companion object{
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
}