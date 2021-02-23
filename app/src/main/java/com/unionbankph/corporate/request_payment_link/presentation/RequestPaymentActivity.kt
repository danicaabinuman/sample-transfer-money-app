package com.unionbankph.corporate.request_payment_link.presentation

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.trimmedLength
import androidx.core.widget.addTextChangedListener
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.asDriver
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_check_deposit_form.*
import kotlinx.android.synthetic.main.activity_check_deposit_form.et_amount
import kotlinx.android.synthetic.main.activity_request_payment.*
import kotlinx.android.synthetic.main.fragment_send_request.*

class RequestPaymentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_payment)

        initListener()
        buttonDisable()

        requiredFields()
    }

    private fun validateForm(){
        var amountString = et_amount.text.toString()
        var paymentForString = et_paymentFor.text.toString()
        var notesString = et_notes.text.toString()
        var paymentLinkExpiry = dropdownPaymentLinkExpory.text.toString()

        if (
                amountString.length > 0 &&
                (paymentForString.length in 1..1000) &&
                (notesString.length > 0)

        ){
            buttonEnable()
        } else {
            buttonDisable()
        }
    }

    private fun buttonDisable(){
        btnRequestPaymentGenerate?.isEnabled = false
        btnRequestPaymentGenerate?.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorWhite))
        btnRequestPaymentGenerate?.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.colorGrey))
    }

    private fun buttonEnable(){
        btnRequestPaymentGenerate?.isEnabled = true
        btnRequestPaymentGenerate?.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorWhite))
        btnRequestPaymentGenerate?.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.colorButtonOrange))
    }

    private fun initListener(){
        btnRequestPaymentCancel.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
        }
    }

    private fun requiredFields(){
        et_amount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateForm()
            }
        })

        et_paymentFor.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateForm()
            }
        })

        et_notes.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateForm()
            }
        })
    }
}