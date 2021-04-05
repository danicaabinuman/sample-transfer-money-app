package com.unionbankph.corporate.link_details.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.showToast
import com.unionbankph.corporate.link_details.data.LinkDetailsResponse
import kotlinx.android.synthetic.main.activity_link_details.*
import java.text.SimpleDateFormat

class LinkDetailsActivity : BaseActivity<LinkDetailsViewModel>(R.layout.activity_link_details) {



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
            finish()
        }

        imgBtnShare.setOnClickListener{
            shareLink()
        }

        ibURLcopy.setOnClickListener{
            copyLink()
        }

    }

    private fun setupInputs() {

        val amount = intent.getStringExtra(LinkDetailsActivity.EXTRA_AMOUNT).toString()
        val paymentFor = intent.getStringExtra(LinkDetailsActivity.EXTRA_PAYMENT_FOR).toString()
        val notes = intent.getStringExtra(LinkDetailsActivity.EXTRA_NOTES).toString()
        val selectedExpiry = intent.getStringExtra(LinkDetailsActivity.EXTRA_SELECTED_EXPIRY).toString()
        val mobileNumber = intent.getStringExtra(LinkDetailsActivity.EXTRA_MOBILE_NUMBER).toString()
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
            setupViews(it)
        })
    }

    private fun setupViews(linkDetailsResponse: LinkDetailsResponse) {


        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
        val formatter = SimpleDateFormat("MM dd, yyyy hh:mm:aa  ")

        val createdDate = formatter.format(parser.parse(linkDetailsResponse.createdDate))
        val expiryDate = formatter.format(parser.parse(linkDetailsResponse.expireDate))

        linkDetailsRefNo.text = linkDetailsResponse.referenceId.toString()
        linkDetailsCreatedDate.text = createdDate
        linkDetailsAmount.text = linkDetailsResponse.amount
        linkDetailsDescription.text = linkDetailsResponse.paymentFor
        linkDetailsNotes.text = linkDetailsResponse.description
        tv_link_details_expiry.text = expiryDate
        linkDetailsPaymentLink.text = linkDetailsResponse.link

    }


    private fun copyLink(){
        ibURLcopy.setOnClickListener{
            val copiedUrl = linkDetailsPaymentLink.text
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied to clipboard", copiedUrl)

//            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()

            clipboard.setPrimaryClip(clip)

            showToast("Copied to clipboard")
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

    private fun dateFormat(){
        var date : Long
        val sdf : SimpleDateFormat
        var dateString : String

        date = System.currentTimeMillis()
        sdf = SimpleDateFormat("MMM dd, yyyy / h:mm a")

        dateString = sdf.format(date)
        linkDetailsCreatedDate.setText(dateString)
    }

    private fun generatedLinkResults(){

        val amount: String = intent.getStringExtra("amount").toString()
        val paymentFor: String = intent.getStringExtra("payment for").toString()
        val notes: String = intent.getStringExtra("notes").toString()
        val linkExpiry: String = intent.getStringExtra("selected expiry").toString()

        linkDetailsAmount.setText(amount)
        linkDetailsDescription.setText(paymentFor)
        linkDetailsNotes.setText(notes)
        tv_link_details_expiry.setText(linkExpiry)
    }



    companion object {
        const val EXTRA_AMOUNT = "amount"
        const val EXTRA_PAYMENT_FOR = "pament for"
        const val EXTRA_NOTES = "notes"
        const val EXTRA_SELECTED_EXPIRY = "selected expiry"
        const val EXTRA_MOBILE_NUMBER = "mobile_number"
    }

}


