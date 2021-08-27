package com.unionbankph.corporate.payment_link.presentation.payment_link_details

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.dashboard.DashboardViewModel
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.databinding.ActivityLinkDetailsBinding
import com.unionbankph.corporate.payment_link.domain.model.response.GeneratePaymentLinkResponse
import com.unionbankph.corporate.payment_link.presentation.billing_details.BillingDetailsActivity
import com.unionbankph.corporate.payment_link.presentation.onboarding.RequestPaymentSplashActivity
import com.unionbankph.corporate.payment_link.presentation.request_payment.RequestForPaymentActivity
import timber.log.Timber
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class LinkDetailsActivity :
    BaseActivity<ActivityLinkDetailsBinding, LinkDetailsViewModel>() {

    var mCurrentLinkDetails: GeneratePaymentLinkResponse? = null
    private var fromWhatTab : String? = null

    override fun onViewsBound() {
        super.onViewsBound()
        initViews()
        setupOutputs()
        initIntentData()
    }

    private fun initIntentData(){
        fromWhatTab = intent.getStringExtra(RequestPaymentSplashActivity.EXTRA_FROM_WHAT_TAB)
        if(fromWhatTab == null){
            fromWhatTab = DashboardViewModel.FROM_REQUEST_PAYMENT_BUTTON
        }
    }

    private fun initViews(){

        binding.ivBackButton.setOnClickListener{
            finish()
        }

        binding.imgBtnShare.setOnClickListener{
            shareLink()
        }

        binding.ibURLcopy.setOnClickListener{
            copyLink()
        }

        binding.btnGenerateAnotherLink.setOnClickListener{
            when(fromWhatTab){
                DashboardViewModel.FROM_REQUEST_PAYMENT_BUTTON -> {
                    finish()
                }
                DashboardViewModel.FROM_TRANSACT_TAB -> {
                    val intent = Intent(this, RequestForPaymentActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }

        binding.btnArchive.setOnClickListener{

            when(binding.btnArchive.text){
                "ARCHIVE" -> {
                    binding.flLoading.visibility = View.VISIBLE
                    mCurrentLinkDetails?.let {
                        viewModel.getPaymentLinkDetailsThenPut(
                            it.referenceNumber!!,
                            LinkDetailsViewModel.STATUS_ARCHIVED
                        )
                    }
                }

                "MARK AS UNPAID" -> {
                    binding.flLoading.visibility = View.VISIBLE
                    mCurrentLinkDetails?.let {
                        viewModel.getPaymentLinkDetailsThenPut(
                            it.referenceNumber!!,
                            LinkDetailsViewModel.STATUS_UNPAID
                        )
                    }
                }

                "VIEW MORE DETAILS" -> {
                    binding.flLoading.visibility = View.GONE
                    val intent = Intent(this@LinkDetailsActivity, BillingDetailsActivity::class.java)
                    mCurrentLinkDetails?.let {
                        intent.putExtra(BillingDetailsActivity.EXTRA_REFERENCE_NUMBER,it.referenceNumber!!)
                    }
                    startActivity(intent)
                }
            }
        }

        binding.viewArchiveSuccess.btnOkay.setOnClickListener {
            binding.archiveSuccess.visibility = View.GONE
            generateNewPaymentLinkAsResult()
        }

        val responseString = intent.getStringExtra(EXTRA_GENERATE_PAYMENT_LINK_RESPONSE).toString()
        mCurrentLinkDetails = JsonHelper.fromJson(responseString)
        setupViews(mCurrentLinkDetails!!)

    }


    private fun setupOutputs() {

        viewModel.putPaymentLinkStatusResponse.observe(this, Observer {
            binding.flLoading.visibility = View.INVISIBLE
            binding.archiveSuccess.visibility = View.VISIBLE
        })

        viewModel.uiState.observe(this, Observer {
            it.getContentIfNotHandled().let { event ->
                when (event) {
                    is UiState.Error -> {
                        binding.flLoading.visibility = View.INVISIBLE
                        handleOnError(event.throwable)
                    }
                }
            }
        })
    }


    private fun updateArchivedView(){
        binding.tvStatus.text = "ARCHIVED"
        binding.tvStatus.setTextColor(Color.parseColor("#4A4A4A"))
        binding.tvStatus.background = getDrawable(R.drawable.bg_status_card_archived)
        binding.clCyberSure.visibility = View.GONE
        binding.btnGenerateAnotherLink.text = "GENERATE NEW LINK"
        binding.btnArchive.text = "MARK AS UNPAID"
        binding.imgBtnShare.isEnabled = false
        binding.imgBtnShare.background = getDrawable(R.drawable.ic_share_gray_archive)
        binding.ibURLcopy.isEnabled = false
        binding.ibURLcopy.background = getDrawable(R.drawable.ic_content_copy_gray)
        binding.btnArchive.isEnabled = true
        binding.tvReferenceNumber.background = getDrawable(R.drawable.bg_half_card_view_gradient_gray)
        binding.tvReferenceNo.setTextColor(Color.parseColor("#4A4A4A"))
        binding.linkDetailsRefNo.setTextColor(Color.parseColor("#4A4A4A"))
        binding.tvDateCreated.setTextColor(Color.parseColor("#4A4A4A"))
        binding.linkDetailsCreatedDate.setTextColor(Color.parseColor("#4A4A4A"))

    }

    private fun updatePendingView(){
        binding.tvStatus.text = "PENDING"
        binding.tvStatus.setTextColor(Color.parseColor("#FF8200"))
        binding.tvStatus.background = getDrawable(R.drawable.bg_status_card_pending)
        binding.clCyberSure.visibility = View.GONE
        binding.btnGenerateAnotherLink.text = "GENERATE NEW LINK"
        binding.btnArchive.visibility = View.GONE
        binding.btnArchive.text = "ARCHIVE"
        binding.imgBtnShare.isEnabled = false
        binding.imgBtnShare.background = getDrawable(R.drawable.ic_share_gray_archive)
        binding.ibURLcopy.isEnabled = false
        binding.ibURLcopy.background = getDrawable(R.drawable.ic_content_copy_gray)
        binding.btnArchive.isEnabled = false
        binding.tvReferenceNumber.background = getDrawable(R.drawable.bg_half_card_view_gradient_gray)
        binding.tvReferenceNo.setTextColor(Color.parseColor("#4A4A4A"))
        binding.linkDetailsRefNo.setTextColor(Color.parseColor("#4A4A4A"))
        binding.tvDateCreated.setTextColor(Color.parseColor("#4A4A4A"))
        binding.linkDetailsCreatedDate.setTextColor(Color.parseColor("#4A4A4A"))
    }

    private fun updateUnpaidView(){
        binding.tvStatus.text = "UNPAID"
        binding.tvStatus.setTextColor(Color.parseColor("#F6B000"))
        binding.tvStatus.background = getDrawable(R.drawable.bg_status_card_unpaid)
        binding.clCyberSure.visibility = View.VISIBLE
        binding.btnGenerateAnotherLink.text = "GENERATE ANOTHER LINK"
        binding.btnArchive.text = "ARCHIVE"
        binding.imgBtnShare.isEnabled = true
        binding.imgBtnShare.background = getDrawable(R.drawable.ic_share_orange)
        binding.ibURLcopy.isEnabled = true
        binding.ibURLcopy.background = getDrawable(R.drawable.ic_content_copy_orange)
        binding.btnArchive.isEnabled = true
        binding.tvReferenceNumber.background = getDrawable(R.drawable.bg_half_card_view_gradient_gray)
        binding.tvReferenceNo.setTextColor(Color.parseColor("#4A4A4A"))
        binding.linkDetailsRefNo.setTextColor(Color.parseColor("#4A4A4A"))
        binding.tvDateCreated.setTextColor(Color.parseColor("#4A4A4A"))
        binding.linkDetailsCreatedDate.setTextColor(Color.parseColor("#4A4A4A"))
    }

    private fun updateExpiredView(){
        binding.tvStatus.text = "EXPIRED"
        binding.tvStatus.setTextColor(Color.parseColor("#E83C18"))
        binding.tvStatus.background = getDrawable(R.drawable.bg_status_card_expired)
        binding.clCyberSure.visibility = View.GONE
        binding.btnGenerateAnotherLink.text = "GENERATE NEW LINK"
        binding.btnArchive.visibility = View.GONE
        binding.btnArchive.text = "ARCHIVE"
        binding.imgBtnShare.isEnabled = false
        binding.imgBtnShare.background = getDrawable(R.drawable.ic_share_gray_archive)
        binding.ibURLcopy.isEnabled = false
        binding.ibURLcopy.background = getDrawable(R.drawable.ic_content_copy_gray)
        binding.btnArchive.isEnabled = false
        binding.tvReferenceNumber.background = getDrawable(R.drawable.bg_half_card_view_gradient_gray)
        binding.tvReferenceNo.setTextColor(Color.parseColor("#4A4A4A"))
        binding.linkDetailsRefNo.setTextColor(Color.parseColor("#4A4A4A"))
        binding.tvDateCreated.setTextColor(Color.parseColor("#4A4A4A"))
        binding.linkDetailsCreatedDate.setTextColor(Color.parseColor("#4A4A4A"))
    }

    private fun updatePaidView(){
        binding.tvStatus.text = "PAID"
        binding.tvStatus.setTextColor(Color.parseColor("#5CA500"))
        binding.tvStatus.background = getDrawable(R.drawable.bg_status_card_paid)
        binding.clCyberSure.visibility = View.VISIBLE
        binding.btnGenerateAnotherLink.text = "GENERATE NEW LINK"
        binding.btnArchive.text = "VIEW MORE DETAILS"
        binding.imgBtnShare.isEnabled = false
        binding.imgBtnShare.background = getDrawable(R.drawable.ic_share_gray_archive)
        binding.ibURLcopy.isEnabled = false
        binding.ibURLcopy.background = getDrawable(R.drawable.ic_content_copy_gray)
        binding.btnArchive.isEnabled = true
        binding.tvReferenceNumber.background = getDrawable(R.drawable.bg_half_card_view_gradient_orange)
        binding.tvReferenceNo.setTextColor(Color.parseColor("#FFFFFF"))
        binding.linkDetailsRefNo.setTextColor(Color.parseColor("#FFFFFF"))
        binding.tvDateCreated.setTextColor(Color.parseColor("#FFFFFF"))
        binding.linkDetailsCreatedDate.setTextColor(Color.parseColor("#FFFFFF"))
    }

    private fun setupViews(linkDetailsResponse: GeneratePaymentLinkResponse) {

        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",Locale.ENGLISH)
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val detailsParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.ENGLISH)
        detailsParser.timeZone = TimeZone.getTimeZone("UTC")
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

            try {
                createdDateString = formatter.format(detailsParser.parse(it))
            } catch (e: Exception){
                Timber.e(e.toString())
            }
        }

        binding.linkDetailsCreatedDate.text = createdDateString

        var expiryDateString = "UNAVAILABLE"
        linkDetailsResponse.expireDate?.let {
            expiryDateString = it
            try {
                expiryDateString = formatter.format(parser.parse(it))
            } catch (e: Exception){
                Timber.e(e.toString()) // this never gets called either
            }

            try {
                expiryDateString = formatter.format(detailsParser.parse(it))
            } catch (e: Exception){
                Timber.e(e.toString())
            }

        }
        binding.tvLinkDetailsExpiry.text = expiryDateString

        val amountParse = DecimalFormat("####.##")
        val amountFormat = DecimalFormat("#,###.00")
        val finalAmount = amountFormat.format(amountParse.parse(linkDetailsResponse.amount))

        binding.linkDetailsRefNo.text = linkDetailsResponse.referenceNumber.toString()

        binding.linkDetailsAmount.text = finalAmount
        binding.linkDetailsDescription.text = linkDetailsResponse.paymentFor
        binding.linkDetailsNotes.text = linkDetailsResponse.description

        binding.linkDetailsPaymentLink.text = linkDetailsResponse.link

        if(linkDetailsResponse.status?.contains("ARCHIVE",true) == true){
            updateArchivedView()
        }else if(linkDetailsResponse.status?.contains("EXPIRE",true) == true){
            updateExpiredView()
        }else if(linkDetailsResponse.status.equals("PAID", true)){
            updatePaidView()
        }else if(linkDetailsResponse.status.equals("UNPAID", true)){
            updateUnpaidView()
        }else if(linkDetailsResponse.status.equals("PENDING", true)){
            updatePendingView()
        }

    }

    private fun copyLink(){
        binding.ibURLcopy.setOnClickListener{
            val copiedUrl = binding.linkDetailsPaymentLink.text
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied to clipboard", copiedUrl)

            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()

            clipboard.setPrimaryClip(clip)

//            showToast("Copied to clipboard")
        }
    }

    private fun shareLink(){
        binding.imgBtnShare.setOnClickListener {
            val intent = Intent()
            intent.action= Intent.ACTION_SEND
            intent.putExtra(Intent.EXTRA_TEXT, binding.linkDetailsPaymentLink.text.toString())
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
        const val EXTRA_GENERATE_PAYMENT_LINK_RESPONSE = "extra_generate_payment_link_response"
    }

    override val viewModelClassType: Class<LinkDetailsViewModel>
        get() = LinkDetailsViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityLinkDetailsBinding
        get() = ActivityLinkDetailsBinding::inflate

}


