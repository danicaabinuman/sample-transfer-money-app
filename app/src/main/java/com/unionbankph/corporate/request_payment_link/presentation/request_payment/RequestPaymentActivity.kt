package com.unionbankph.corporate.request_payment_link.presentation.request_payment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.app.di.ViewModelFactory
import com.unionbankph.corporate.link_details.presentation.LinkDetails
import com.unionbankph.corporate.request_payment_link.data.form.RequestPaymentForm
import kotlinx.android.synthetic.main.activity_check_deposit_form.*
import kotlinx.android.synthetic.main.activity_check_deposit_form.et_amount
import kotlinx.android.synthetic.main.activity_request_payment.*
import kotlinx.android.synthetic.main.fragment_send_request.*

class RequestPaymentActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    var time = arrayOf("6 hours", "12 hours", "1 day", "2 days", "3 days", "7 days")
    val NEW_SPINNER_ID = 1
    var linkExpiry = "12 hours"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_payment)

        btnRequestPaymentGenerate.setOnClickListener{
            generatePaymentLink()
        }

        initListener()
        buttonDisable()

        requiredFields()
        paymentLinkExpiry()
        finishRequestPayment()
    }

    private fun generatePaymentLink(){


        //After success call this
        navigateToLinkDetails()
    }
    private fun validateForm(){
        var amountString = et_amount.text.toString()
        var paymentForString = et_paymentFor.text.toString()
        var notesString = et_notes.text.toString()

        if (
                amountString.length > 4 &&
                (paymentForString.length in 1..1000) &&
                (notesString.length >= 0)

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
                val length : Int = et_paymentFor.length()
                val counter : String = length.toString()
                tv_text_counter.text = counter
                tv_text_counter.setHorizontallyScrolling(true)
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
            }
        })
    }

    private fun paymentLinkExpiry(){
        var aa = ArrayAdapter(this, android.R.layout.simple_list_item_1, time)
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        with(dropdownPaymentLinkExport){
            adapter = aa
            setSelection(1, false)
            onItemSelectedListener = this@RequestPaymentActivity
            prompt = "SAMPLE PROMPT MESSAGE"
            gravity = Gravity.CENTER
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        linkExpiry = parent?.getItemAtPosition(position).toString()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    private fun navigateToLinkDetails(){
        val intent = Intent(this, LinkDetails::class.java)
        intent.putExtra("amount", et_amount.text.toString())
        intent.putExtra("payment for", et_paymentFor.text.toString())
        intent.putExtra("notes", et_notes.text.toString())
        intent.putExtra("selected expiry", linkExpiry)
        startActivity(intent)
    }

    private fun finishRequestPayment() {

        ivBackButton.setOnClickListener {
            val intent = Intent (this, DashboardActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        val intent = Intent (this, DashboardActivity::class.java)
        startActivity(intent)
    }


}