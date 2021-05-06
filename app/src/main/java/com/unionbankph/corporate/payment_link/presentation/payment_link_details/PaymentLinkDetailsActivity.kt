package com.unionbankph.corporate.payment_link.presentation.payment_link_details

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.opengl.Visibility
import android.os.Build
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.setColor
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.payment_link.domain.model.response.GeneratePaymentLinkResponse
import com.unionbankph.corporate.payment_link.presentation.payment_link.PaymentLinkActivity
import com.unionbankph.corporate.payment_link.presentation.request_payment.RequestForPaymentActivity
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.nominate_settlement_account.NominateSettlementActivity
import kotlinx.android.synthetic.main.activity_archive_success.*
import kotlinx.android.synthetic.main.activity_link_details.*
import timber.log.Timber
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

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
        setupOutputs()
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
            generateNewPaymentLinkAsResult()
        }

        btnArchive.setOnClickListener{
            flLoading.visibility = View.VISIBLE
            when(btnArchive.text){
                "ARCHIVE" -> {
                    mCurrentLinkDetails?.let {
                        viewModel.getPaymentLinkDetailsThenPut(
                            it.referenceId!!,
                            LinkDetailsViewModel.STATUS_ARCHIVE
                        )
                    }
                }

                "MARK AS UNPAID" -> {
                    mCurrentLinkDetails?.let {
                        viewModel.getPaymentLinkDetailsThenPut(
                            it.referenceId!!,
                            LinkDetailsViewModel.STATUS_UNPAID
                        )
                    }
                }

                "VIEW MORE DETAILS" -> {
                    //TODO NAVIGATE TO VIEW MORE DETAILS
                }
            }
        }

        btnOkay.setOnClickListener {
            archiveSuccess.visibility = View.GONE
            generateNewPaymentLinkAsResult()
        }

        val responseString = intent.getStringExtra(EXTRA_GENERATE_PAYMENT_LINK_RESPONSE).toString()
        mCurrentLinkDetails = JsonHelper.fromJson(responseString)
        setupViews(mCurrentLinkDetails!!)

    }

    private fun setupOutputs() {

        viewModel.putPaymentLinkStatusResponse.observe(this, Observer {
            flLoading.visibility = View.INVISIBLE
            archiveSuccess.visibility = View.VISIBLE

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
        imgBtnShare.background = getDrawable(R.drawable.ic_share_gray_archive)
        ibURLcopy.isEnabled = false
        ibURLcopy.background = getDrawable(R.drawable.ic_content_copy_gray)
        btnArchive.isEnabled = true
        tvReferenceNumber.background = getDrawable(R.drawable.bg_half_card_view_gradient_gray)
        tvReferenceNo.setTextColor(Color.parseColor("#4A4A4A"))
        linkDetailsRefNo.setTextColor(Color.parseColor("#4A4A4A"))
        tvDateCreated.setTextColor(Color.parseColor("#4A4A4A"))
        linkDetailsCreatedDate.setTextColor(Color.parseColor("#4A4A4A"))

    }

    private fun updatePendingView(){
        tvStatus.text = "PENDING"
        tvStatus.setTextColor(Color.parseColor("#FF8200"))
        tvStatus.background = getDrawable(R.drawable.bg_status_card_pending)
        clCyberSure.visibility = View.GONE
        btnGenerateAnotherLink.text = "GENERATE NEW LINK"
        btnArchive.visibility = View.GONE
        btnArchive.text = "ARCHIVE"
        imgBtnShare.isEnabled = false
        imgBtnShare.background = getDrawable(R.drawable.ic_share_gray_archive)
        ibURLcopy.isEnabled = false
        ibURLcopy.background = getDrawable(R.drawable.ic_content_copy_gray)
        btnArchive.isEnabled = false
        tvReferenceNumber.background = getDrawable(R.drawable.bg_half_card_view_gradient_gray)
        tvReferenceNo.setTextColor(Color.parseColor("#4A4A4A"))
        linkDetailsRefNo.setTextColor(Color.parseColor("#4A4A4A"))
        tvDateCreated.setTextColor(Color.parseColor("#4A4A4A"))
        linkDetailsCreatedDate.setTextColor(Color.parseColor("#4A4A4A"))
    }

    private fun updateUnpaidView(){
        tvStatus.text = "UNPAID"
        tvStatus.setTextColor(Color.parseColor("#F6B000 "))
        tvStatus.background = getDrawable(R.drawable.bg_status_card_unpaid)
        clCyberSure.visibility = View.VISIBLE
        btnGenerateAnotherLink.text = "GENERATE ANOTHER LINK"
        btnArchive.text = "ARCHIVE"
        imgBtnShare.isEnabled = true
        imgBtnShare.background = getDrawable(R.drawable.ic_share_orange)
        ibURLcopy.isEnabled = true
        ibURLcopy.background = getDrawable(R.drawable.ic_content_copy_orange)
        btnArchive.isEnabled = true
        tvReferenceNumber.background = getDrawable(R.drawable.bg_half_card_view_gradient_gray)
        tvReferenceNo.setTextColor(Color.parseColor("#4A4A4A"))
        linkDetailsRefNo.setTextColor(Color.parseColor("#4A4A4A"))
        tvDateCreated.setTextColor(Color.parseColor("#4A4A4A"))
        linkDetailsCreatedDate.setTextColor(Color.parseColor("#4A4A4A"))
    }

    private fun updateExpiredView(){
        tvStatus.text = "EXPIRED"
        tvStatus.setTextColor(Color.parseColor("#E83C18"))
        tvStatus.background = getDrawable(R.drawable.bg_status_card_expired)
        clCyberSure.visibility = View.GONE
        btnGenerateAnotherLink.text = "GENERATE NEW LINK"
        btnArchive.visibility = View.GONE
        btnArchive.text = "ARCHIVE"
        imgBtnShare.isEnabled = false
        imgBtnShare.background = getDrawable(R.drawable.ic_share_gray_archive)
        ibURLcopy.isEnabled = false
        ibURLcopy.background = getDrawable(R.drawable.ic_content_copy_gray)
        btnArchive.isEnabled = false
        tvReferenceNumber.background = getDrawable(R.drawable.bg_half_card_view_gradient_gray)
        tvReferenceNo.setTextColor(Color.parseColor("#4A4A4A"))
        linkDetailsRefNo.setTextColor(Color.parseColor("#4A4A4A"))
        tvDateCreated.setTextColor(Color.parseColor("#4A4A4A"))
        linkDetailsCreatedDate.setTextColor(Color.parseColor("#4A4A4A"))
    }

    private fun updatePaidView(){
        tvStatus.text = "PAID"
        tvStatus.setTextColor(Color.parseColor("#5CA500"))
        tvStatus.background = getDrawable(R.drawable.bg_status_card_paid)
        clCyberSure.visibility = View.VISIBLE
        btnGenerateAnotherLink.text = "GENERATE NEW LINK"
        btnArchive.text = "VIEW MORE DETAILS"
        imgBtnShare.isEnabled = false
        imgBtnShare.background = getDrawable(R.drawable.ic_share_gray_archive)
        ibURLcopy.isEnabled = false
        ibURLcopy.background = getDrawable(R.drawable.ic_content_copy_gray)
        btnArchive.isEnabled = false
        tvReferenceNumber.background = getDrawable(R.drawable.bg_half_card_view_gradient_orange)
        tvReferenceNo.setTextColor(Color.parseColor("#FFFFFF"))
        linkDetailsRefNo.setTextColor(Color.parseColor("#FFFFFF"))
        tvDateCreated.setTextColor(Color.parseColor("#FFFFFF"))
        linkDetailsCreatedDate.setTextColor(Color.parseColor("#FFFFFF"))
    }

    private fun setupViews(linkDetailsResponse: GeneratePaymentLinkResponse) {

        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",Locale.ENGLISH)
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val formatter = SimpleDateFormat("MMM dd, yyyy , hh:mm aa", Locale.ENGLISH)
        formatter.timeZone = TimeZone.getDefault()

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

        if(linkDetailsResponse.status.equals(LinkDetailsViewModel.STATUS_ARCHIVE)){
            updateArchivedView()
        }else if(linkDetailsResponse.status.equals(LinkDetailsViewModel.STATUS_EXPIRED)){
            updateExpiredView()
        }else if(linkDetailsResponse.status.equals(LinkDetailsViewModel.STATUS_PAID)){
            updatePaidView()
        }else if(linkDetailsResponse.status.equals(LinkDetailsViewModel.STATUS_UNPAID)){
            updateUnpaidView()
        }else if(linkDetailsResponse.status.equals(LinkDetailsViewModel.STATUS_PENDING)){
            updatePendingView()
        }

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

    private fun generateNewPaymentLinkAsResult() {
        val data = Intent()
        data.putExtra(RESULT_SHOULD_GENERATE_NEW_LINK, true);

        setResult(RESULT_OK, data);
        finish()
    }


    companion object {
        const val REQUEST_CODE = 1209
        const val RESULT_SHOULD_GENERATE_NEW_LINK = "result_should_generate_new_link"
        const val EXTRA_AMOUNT = "amount"
        const val EXTRA_PAYMENT_FOR = "pament for"
        const val EXTRA_NOTES = "notes"
        const val EXTRA_SELECTED_EXPIRY = "selected expiry"
        const val EXTRA_MOBILE_NUMBER = "mobile_number"
        
        const val EXTRA_GENERATE_PAYMENT_LINK_RESPONSE = "extra_generate_payment_link_response"
    }

}


