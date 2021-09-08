package com.unionbankph.corporate.payment_link.presentation.billing_details


import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.Observer
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
    BaseActivity<ActivityBillingDetailsBinding, BillingDetailsViewModel>() {

    override fun onViewModelBound() {
        super.onViewModelBound()
    }

    override fun onViewsBound() {
        super.onViewsBound()

//        binding.btnViewMore.setOnClickListener{
//            val intent = Intent(this@BillingDetailsActivity, ActivityLogsActivity::class.java)
//            startActivity(intent)
//        }

        backButton()
        setupInputs()
        setupOutputs()

    }

    private fun backButton() {
        binding.btnBack.setOnClickListener() {
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

    private fun setupOutputs() {
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
        var paymentMethod = response.payorDetails?.paymentMethod
        binding.paymentMethodText.text = paymentMethod.toString()

        if (paymentMethod == "INSTAPAY"){
            binding.apply {
                instapayLogo.visibility = View.VISIBLE
                ubLogo.visibility = View.GONE
                bayadCenterLogo.visibility = View.GONE
                ecpayLogo.visibility = View.GONE
                lbcLogo.visibility = View.GONE
                palawanLogo.visibility = View.GONE
                cebuanaLogo.visibility = View.GONE
                grabpayLogo.visibility = View.GONE
                gcashLogo.visibility = View.GONE
            }
        } else if (paymentMethod == "GCASH"){
            binding.apply {
                instapayLogo.visibility = View.GONE
                ubLogo.visibility = View.GONE
                bayadCenterLogo.visibility = View.GONE
                ecpayLogo.visibility = View.GONE
                lbcLogo.visibility = View.GONE
                palawanLogo.visibility = View.GONE
                cebuanaLogo.visibility = View.GONE
                grabpayLogo.visibility = View.GONE
                gcashLogo.visibility = View.VISIBLE
            }

        } else if (paymentMethod == "GRABPAY"){
            binding.apply {
                instapayLogo.visibility = View.GONE
                ubLogo.visibility = View.GONE
                bayadCenterLogo.visibility = View.GONE
                ecpayLogo.visibility = View.GONE
                lbcLogo.visibility = View.GONE
                palawanLogo.visibility = View.GONE
                cebuanaLogo.visibility = View.GONE
                grabpayLogo.visibility = View.VISIBLE
                gcashLogo.visibility = View.GONE
            }

        } else if (paymentMethod == "UB ONLINE"){
            binding.apply {
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
        }
        else if (paymentMethod == "CEBUANALHUILLIER"){
            binding.apply {
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
        }
        else if (paymentMethod == "PALAWAN"){
            binding.apply {
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
        }
        else if (paymentMethod == "LBC"){
            binding.apply {
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
        }
        else if (paymentMethod == "ECPAY"){
            binding.apply {
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
        }
        else if (paymentMethod == "BAYADCENTER"){
            binding.apply {
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
        }
        else {
            binding.apply {
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
        }

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