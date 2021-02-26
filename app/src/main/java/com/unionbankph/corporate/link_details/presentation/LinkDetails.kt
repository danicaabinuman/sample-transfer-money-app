package com.unionbankph.corporate.link_details.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.unionbankph.corporate.R
import kotlinx.android.synthetic.main.activity_link_details.*

class LinkDetails : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_link_details)

        copyLink()

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
}