package com.unionbankph.corporate.payment_link.presentation.billing_details

import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.payment_link.domain.model.response.GetPaymentLinkByReferenceIdResponse
import kotlinx.android.synthetic.main.activity_billing_details.*
import timber.log.Timber
import java.text.SimpleDateFormat

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


        btnViewMore.setOnClickListener{
//            val intent = Intent(this@BillingDetailsActivity, ActivityLogsActivity::class.java)
//            startActivity(intent)
        }

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

        tvGrossAmount.text = response.paymentDetails?.amount
        tvFee.text = response.paymentDetails?.fee.toString()
        tvNetAmount.text = response.paymentDetails?.amount

        tvPayorName.text = response.payorDetails?.fullName
        tvPayorEmail.text = response.payorDetails?.emailAddress
        tvPayorContactNumber.text = response.payorDetails?.mobileNumber

        tvRefNumber.text = response.paymentDetails?.referenceNo
        tvReferenceNumberTitle.text = response.paymentDetails?.referenceNo
        tvAmount.text = response.paymentDetails?.amount
        tvDescription.text = response.paymentDetails?.paymentFor
        tvRemarks.text = response.paymentDetails?.description
        tvLinkUrl.text = response.paymentDetails?.paymentLink
        tvFee.text = response.paymentDetails?.fee
        var status = response.paymentDetails?.status

        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val formatter = SimpleDateFormat("MMM dd, yyyy hh:mm:aa")

        var createdDateString = "UNAVAILABLE"
        response.createdDate?.let {
            createdDateString = it
            try {
                createdDateString = formatter.format(parser.parse(it))
            } catch (e: Exception){
                Timber.e(e.toString()) // this never gets called either
            }
        }
        tvBillingDetailsDateCreated.text = createdDateString

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

        if(status.equals("ARCHIVED",true)){

            tvStatusArchived.visibility = View.VISIBLE
            tvStatusUnpaid.visibility = View.GONE
            tvStatusExpired.visibility = View.GONE
            tvStatusPaid.visibility = View.GONE

        }else if(status.equals("PAID", true)){
            tvStatusArchived.visibility = View.GONE
            tvStatusUnpaid.visibility = View.GONE
            tvStatusExpired.visibility = View.GONE
            tvStatusPaid.visibility = View.VISIBLE
        }else if (status.equals("EXPIRED", true)){
            tvStatusArchived.visibility = View.GONE
            tvStatusUnpaid.visibility = View.GONE
            tvStatusExpired.visibility = View.VISIBLE
            tvStatusPaid.visibility = View.GONE
        }else{
            tvStatusArchived.visibility = View.GONE
            tvStatusUnpaid.visibility = View.VISIBLE
            tvStatusExpired.visibility = View.GONE
            tvStatusPaid.visibility = View.GONE
        }

    }
    companion object{
        const val EXTRA_REFERENCE_NUMBER = "extra_reference_number"
    }

}