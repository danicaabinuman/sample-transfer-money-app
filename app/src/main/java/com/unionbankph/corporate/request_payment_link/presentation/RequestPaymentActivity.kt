package com.unionbankph.corporate.request_payment_link.presentation

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.trimmedLength
import androidx.core.widget.addTextChangedListener
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.asDriver
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.link_details.presentation.LinkDetails
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_check_deposit_form.*
import kotlinx.android.synthetic.main.activity_check_deposit_form.et_amount
import kotlinx.android.synthetic.main.activity_request_payment.*
import kotlinx.android.synthetic.main.fragment_send_request.*

class RequestPaymentActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    var time = arrayOf("6 hours", "12 hours", "1 day", "2 days", "3 days", "7 days")
    val NEW_SPINNER_ID = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_payment)

        initListener()
        buttonDisable()

        requiredFields()
        paymentLinkExpiry()

    }

    private fun validateForm(){
        var amountString = et_amount.text.toString()
        var paymentForString = et_paymentFor.text.toString()
        var notesString = et_notes.text.toString()

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
        btnRequestPaymentGenerate.setOnClickListener{
            val intent = Intent(this, LinkDetails::class.java)
            startActivity(intent)
        }
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

        val spinner = Spinner(this)
        spinner.id = NEW_SPINNER_ID

        val ll = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        ll.setMargins(10, 100, 10, 10)
        linearlayout.addView(spinner)

        aa = ArrayAdapter(this, R.layout.spinner, time)
        aa.setDropDownViewResource(R.layout.spinner)

        with(spinner)
        {
            adapter = aa
            setSelection(1, false)
            onItemSelectedListener = this@RequestPaymentActivity
            layoutParams = ll
            prompt = "12 hours"
            setPopupBackgroundResource(R.color.material_grey_600)
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        buttonEnable()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        buttonDisable()
    }
}