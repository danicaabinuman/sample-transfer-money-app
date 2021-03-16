package com.unionbankph.corporate.link_details.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.common.extension.showToast
import com.unionbankph.corporate.link_details.data.form.LinkDetailsForm
import kotlinx.android.synthetic.main.activity_link_details.*
import kotlinx.android.synthetic.main.footer_error.*
import java.text.SimpleDateFormat

class LinkDetails : AppCompatActivity() {

    private lateinit var viewModel: LinkDetailsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_link_details)

        initViewModel()

        generatedLinkResults()
        dateFormat()
        copyLink()
        shareLink()

        ivBackButton.setOnClickListener{
            finish()
        }

        displayLinkDetails()
    }

    private fun initViewModel(){
        viewModel = ViewModelProvider(this).get(LinkDetailsViewModel::class.java)
    }

    private fun displayLinkDetails(){

        val amount = tv_link_details_amount.text.toString().toDouble()
        val desc = tv_link_details_description.text.toString()
        val note = tv_link_details_notes.text.toString()
        var expiry = 12
        val expiryText = tv_link_details_expiry.text.toString()

        if(expiryText.equals("6 hours",true)){
            expiry = 6
        }else if (expiryText.equals("12 hours", true)){
            expiry = 12
        }else if (expiryText.equals("1 day", true)){
            expiry = 24
        }else if (expiryText.equals("2 days", true)){
            expiry = 48
        }else if (expiryText.equals("3 days", true)){
            expiry = 72
        }else if (expiryText.equals("7 days", true)){
            expiry = 168
        }



        viewModel.generateLinkDetails(
                LinkDetailsForm(
                    amount,
                    desc,
                    note,
                    expiry
                )
        )
    }

    private fun copyLink(){
        ibURLcopy.setOnClickListener{
            val copiedUrl = sampleLink.text
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
            intent.putExtra(Intent.EXTRA_TEXT, sampleLink.text.toString())
            intent.type="text/plain"
            startActivity(Intent.createChooser(intent,"Share To:"))
        }
    }

    private fun dateFormat(){
        var date : Long
        val sdf : SimpleDateFormat
        var dateString : String

        date = System.currentTimeMillis()
        sdf = SimpleDateFormat("MMM dd, yyyy / h:mm a")

        dateString = sdf.format(date)
        tv_link_details_date_and_time.setText(dateString)
    }

    private fun generatedLinkResults(){

        val amount: String = intent.getStringExtra("amount").toString()
        val paymentFor: String = intent.getStringExtra("payment for").toString()
        val notes: String = intent.getStringExtra("notes").toString()
        val linkExpiry: String = intent.getStringExtra("selected expiry").toString()

        tv_link_details_amount.setText(amount)
        tv_link_details_description.setText(paymentFor)
        tv_link_details_notes.setText(notes)
        tv_link_details_expiry.setText(linkExpiry)
    }
}