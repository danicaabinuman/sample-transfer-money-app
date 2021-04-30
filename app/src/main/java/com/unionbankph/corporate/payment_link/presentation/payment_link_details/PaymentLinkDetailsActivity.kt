package com.unionbankph.corporate.payment_link.presentation.payment_link_details

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.payment_link.domain.model.response.GeneratePaymentLinkResponse
import com.unionbankph.corporate.payment_link.presentation.payment_link.PaymentLinkActivity
import com.unionbankph.corporate.payment_link.presentation.request_payment.RequestForPaymentActivity
import kotlinx.android.synthetic.main.activity_link_details.*
import timber.log.Timber
import java.text.DecimalFormat
import java.text.SimpleDateFormat

class LinkDetailsActivity : BaseActivity<LinkDetailsViewModel>(R.layout.activity_link_details) {


    var mCurrentLinkDetails: GeneratePaymentLinkResponse? = null

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel = ViewModelProviders.of(
            this,
            viewModelFactory
        )[LinkDetailsViewModel::class.java]
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initViews()
        setupInputs()
        setupOutputs()
        flLoading.visibility = View.VISIBLE
    }

    private fun initViews(){

        ivBackButton.setOnClickListener{
            val intent = Intent(this, RequestForPaymentActivity::class.java)
            startActivity(intent)
            finish()
        }

        imgBtnShare.setOnClickListener{
            shareLink()
        }

        ibURLcopy.setOnClickListener{
            copyLink()
        }

        btnGenerateAnotherLink.setOnClickListener{
            finish()
        }

        btnArchive.setOnClickListener{
            flLoading.visibility = View.VISIBLE
            mCurrentLinkDetails?.let {
                viewModel.getPaymentLinkDetailsThenArchive(it.referenceId!!)
            }

        }

    }

    private fun setupInputs() {

        val amount = intent.getStringExtra(EXTRA_AMOUNT).toString()
        val paymentFor = intent.getStringExtra(EXTRA_PAYMENT_FOR).toString()
        val notes = intent.getStringExtra(EXTRA_NOTES).toString()
        val selectedExpiry = intent.getStringExtra(EXTRA_SELECTED_EXPIRY).toString()
        val mobileNumber = intent.getStringExtra(EXTRA_MOBILE_NUMBER).toString()
        viewModel.initBundleData(
            amount,
            paymentFor,
            notes,
            selectedExpiry,
            mobileNumber
        )
    }

    private fun setupOutputs() {
        viewModel.linkDetailsResponse.observe(this, Observer {
            flLoading.visibility = View.INVISIBLE
            mCurrentLinkDetails = it
            setupViews(it)
        })



        viewModel.archivePaymentLinkResponse.observe(this, Observer {
            flLoading.visibility = View.INVISIBLE
            Toast.makeText(this,"Archive successful", Toast.LENGTH_SHORT).show();
            updateArchivedView()

        })

        viewModel.uiState.observe(this, Observer {
            it.getContentIfNotHandled().let { event ->
                when (event) {
                    is UiState.Error -> {
                        flLoading.visibility = View.INVISIBLE
                        handleOnError(event.throwable)
                    }
                }
            }
        })
    }


    private fun updateArchivedView(){
        tvStatus.text = "ARCHIVED"
        tvStatus.setTextColor(Color.parseColor("#4A4A4A"))
        tvStatus.background = getDrawable(R.drawable.bg_status_card_archived)
        clCyberSure.visibility = View.GONE
        btnGenerateAnotherLink.text = "GENERATE NEW LINK"
        btnArchive.text = "MARK AS UNPAID"
        imgBtnShare.isEnabled = false
        ibURLcopy.isEnabled = false
        btnArchive.isEnabled = false

    }

    private fun setupViews(linkDetailsResponse: GeneratePaymentLinkResponse) {


        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val formatter = SimpleDateFormat("MMM dd, yyyy hh:mm:aa")

        var createdDateString = "UNAVAILABLE"
        linkDetailsResponse.transactionData?.createdDate?.let {
            createdDateString = it
            try {
                createdDateString = formatter.format(parser.parse(it))
            } catch (e: Exception){
                Timber.e(e.toString()) // this never gets called either
            }
        }

        linkDetailsCreatedDate.text = createdDateString

        var expiryDateString = "UNAVAILABLE"
        linkDetailsResponse.expireDate?.let {
            expiryDateString = it
            try {
                expiryDateString = formatter.format(parser.parse(it))
            } catch (e: Exception){
                Timber.e(e.toString()) // this never gets called either
            }
        }
        tv_link_details_expiry.text = expiryDateString

        val amountParse = DecimalFormat("####.##")
        val amountFormat = DecimalFormat("#,###.##")
        val finalAmount = amountFormat.format(amountParse.parse(linkDetailsResponse.amount))

        linkDetailsRefNo.text = linkDetailsResponse.referenceId.toString()

        linkDetailsAmount.text = finalAmount
        linkDetailsDescription.text = linkDetailsResponse.paymentFor
        linkDetailsNotes.text = linkDetailsResponse.description

        linkDetailsPaymentLink.text = linkDetailsResponse.link

    }


    private fun copyLink(){
        ibURLcopy.setOnClickListener{
            val copiedUrl = linkDetailsPaymentLink.text
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied to clipboard", copiedUrl)

            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()

            clipboard.setPrimaryClip(clip)

//            showToast("Copied to clipboard")
        }
    }

    private fun shareLink(){
        imgBtnShare.setOnClickListener {
            val intent = Intent()
            intent.action= Intent.ACTION_SEND
            intent.putExtra(Intent.EXTRA_TEXT, linkDetailsPaymentLink.text.toString())
            intent.type="text/plain"
            startActivity(Intent.createChooser(intent, "Share To:"))
        }
    }



    companion object {
        const val EXTRA_AMOUNT = "amount"
        const val EXTRA_PAYMENT_FOR = "pament for"
        const val EXTRA_NOTES = "notes"
        const val EXTRA_SELECTED_EXPIRY = "selected expiry"
        const val EXTRA_MOBILE_NUMBER = "mobile_number"
    }

}


