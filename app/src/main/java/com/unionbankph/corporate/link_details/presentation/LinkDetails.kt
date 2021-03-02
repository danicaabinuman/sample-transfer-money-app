package com.unionbankph.corporate.link_details.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.unionbankph.corporate.R
import kotlinx.android.synthetic.main.activity_link_details.*
import java.text.SimpleDateFormat

class LinkDetails : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_link_details)

        generatedLinkResults()
        dateFormat()
        copyLink()
        shareLink()
    }

    private fun copyLink(){
        ibURLcopy.setOnClickListener{
            val copiedUrl = sampleLink.text
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied to clipboard", copiedUrl)
            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
            clipboard.setPrimaryClip(clip)
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