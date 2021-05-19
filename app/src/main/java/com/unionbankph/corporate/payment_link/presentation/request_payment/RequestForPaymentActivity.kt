package com.unionbankph.corporate.payment_link.presentation.request_payment

import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.payment_link.domain.model.response.GeneratePaymentLinkResponse
import com.unionbankph.corporate.payment_link.presentation.payment_link_details.LinkDetailsActivity
import kotlinx.android.synthetic.main.activity_check_deposit_filter.*
import kotlinx.android.synthetic.main.activity_check_deposit_form.*
import kotlinx.android.synthetic.main.activity_request_payment.et_amount
import kotlinx.android.synthetic.main.activity_link_details.*
import kotlinx.android.synthetic.main.activity_request_payment.*
import kotlinx.android.synthetic.main.activity_request_payment.ivBackButton
import kotlinx.android.synthetic.main.activity_request_payment.til_amount
import kotlinx.android.synthetic.main.activity_setup_payment_links.*
import kotlinx.android.synthetic.main.fragment_send_request.*
import timber.log.Timber

class RequestForPaymentActivity : BaseActivity<RequestForPaymentViewModel>(R.layout.activity_request_payment), AdapterView.OnItemSelectedListener {

    var time = arrayOf("6 hours", "12 hours", "1 day", "2 days", "3 days", "7 days")
    val NEW_SPINNER_ID = 1
    var linkExpiry = "12 hours"

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel = ViewModelProviders.of(
            this,
            viewModelFactory
        )[RequestForPaymentViewModel::class.java]
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initViews()

        setupOutputs()
        buttonDisable()

        requiredFields()
        paymentLinkExpiry()
        finishRequestPayment()
    }

    private fun initViews(){
        btnRequestPaymentGenerate.setOnClickListener{
            val amount = et_amount.text.toString()
            val paymentFor = et_paymentFor.text.toString()
            val notes = et_notes.text.toString()
            val mobileNumber = textInputEditTextMobileNumber.text.toString()
            if(mobileNumber.isNotEmpty()){
                if(mobileNumber.length<10){
                    Toast.makeText(this@RequestForPaymentActivity, "Mobile Number length is invalid",Toast.LENGTH_SHORT).show()
                }else{
                    requestPaymentLoading.visibility = View.VISIBLE
                    viewModel.preparePaymentLinkGeneration(
                        amount,
                        paymentFor,
                        notes,
                        linkExpiry,
                        mobileNumber
                    )
                }
            }else{
                requestPaymentLoading.visibility = View.VISIBLE
                viewModel.preparePaymentLinkGeneration(
                    amount,
                    paymentFor,
                    notes,
                    linkExpiry,
                    mobileNumber
                )
            }
        }

        btnRequestPaymentCancel.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupOutputs(){
        viewModel.linkDetailsResponse.observe(this, Observer {
            requestPaymentLoading.visibility = View.GONE
            navigateToLinkDetails(it)
        })
    }

//    private fun minimumAmount(){
//        viewModel.linkDetailsResponse.observe(this, Observer{
//            requestPaymentLoading.visibility = View.GONE
//            linkDetailsChecker(it)
//        })
//    }
//
//    private fun linkDetailsChecker(response: GeneratePaymentLinkResponse){
//        if (response.amount.toDouble() < 100){
//            til_amount.error = "Minimum amount is 100 pesos"
//            buttonDisable()
//        }
//
//    }

    private fun validateForm(){
        val amountString = et_amount.text.toString()
        val paymentForString = et_paymentFor.text.toString()

        if ((paymentForString.length in 1..100)){
            when (amountString) {
                "PHP 0" -> {buttonDisable()}
                "PHP 0." -> {buttonDisable()}
                "PHP 0.0" -> {buttonDisable()}
                "PHP 0.00" -> {buttonDisable()}
                else -> {
                    buttonEnable()}
            }
        } else {
            buttonDisable()
        }
    }

    private fun buttonDisable(){
        btnRequestPaymentGenerate?.isEnabled = false
        btnRequestPaymentGenerate?.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorWhite))
        btnRequestPaymentGenerate?.setBackgroundResource(R.drawable.bg_splash_payment_request_button_disabled)
    }

    private fun buttonEnable(){
        btnRequestPaymentGenerate?.isEnabled = true
        btnRequestPaymentGenerate?.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorWhite))
        btnRequestPaymentGenerate?.setBackgroundResource(R.drawable.bg_splash_payment_request_button)
    }

    private fun requiredFields(){
        et_amount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
//                minimumAmount()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//                if (s != null) {
//                    if (count < 7){
//                        til_amount.error = "Minimum amount should be PHP 100.00 and above"
//                    }
//                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(count<9){
                    val cleanString = s.toString().replace("PHP","").replace(" ","")
                    var amountDouble = 0.00
                    try {
                        amountDouble = cleanString.toDouble()
                        if(amountDouble < 100.00){
                            til_amount.error = "Minimum amount is Php 100.00"
                        } else {
                            til_amount.error = ""
                        }
                    }catch (e: NumberFormatException){
                        Timber.e(e.message)
                        e.printStackTrace()
                    }

                }
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

        textInputEditTextMobileNumber.addTextChangedListener(object : TextWatcher {
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
            onItemSelectedListener = this@RequestForPaymentActivity
            prompt = "SAMPLE PROMPT MESSAGE"
            gravity = Gravity.CENTER
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        linkExpiry = parent?.getItemAtPosition(position).toString()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    private fun navigateToLinkDetails(response: GeneratePaymentLinkResponse){
        val responseJson = JsonHelper.toJson(response)
        val intent = Intent(this, LinkDetailsActivity::class.java)
        intent.putExtra(LinkDetailsActivity.EXTRA_GENERATE_PAYMENT_LINK_RESPONSE,responseJson)
        startActivityForResult(intent, LinkDetailsActivity.REQUEST_CODE)
    }

    private fun finishRequestPayment() {

        ivBackButton.setOnClickListener {
            finish()
        }
    }

    private fun clearAllFields(){
        et_amount.text?.clear()
        et_paymentFor.text?.clear()
        et_notes.text?.clear()
        textInputEditTextMobileNumber.text?.clear()
        et_amount.requestFocus()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == LinkDetailsActivity.REQUEST_CODE){
            if (resultCode == RESULT_OK) {
                val shouldGenerateNewPaymentLink = data?.getBooleanExtra(LinkDetailsActivity.RESULT_SHOULD_GENERATE_NEW_LINK,false)
                if(shouldGenerateNewPaymentLink == true){
                    clearAllFields()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        clearAllFields()
    }

}