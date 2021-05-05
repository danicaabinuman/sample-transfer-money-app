package com.unionbankph.corporate.payment_link.presentation.request_payment

import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.payment_link.presentation.payment_link_details.LinkDetailsActivity
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.SetupPaymentLinkActivity
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.SetupPaymentLinkViewModel
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.nominate_settlement_account.NominateSettlementActivity
import kotlinx.android.synthetic.main.activity_check_deposit_form.*
import kotlinx.android.synthetic.main.activity_check_deposit_form.et_amount
import kotlinx.android.synthetic.main.activity_request_payment.*
import kotlinx.android.synthetic.main.fragment_send_request.*

class RequestForPaymentActivity : BaseActivity<SetupPaymentLinkViewModel>(R.layout.activity_request_payment), AdapterView.OnItemSelectedListener {

    var time = arrayOf("6 hours", "12 hours", "1 day", "2 days", "3 days", "7 days")
    val NEW_SPINNER_ID = 1
    var linkExpiry = "12 hours"

    override fun onViewsBound() {
        super.onViewsBound()
        initViews()


        buttonDisable()

        requiredFields()
        paymentLinkExpiry()
        finishRequestPayment()
    }

    private fun initViews(){
        btnRequestPaymentGenerate.setOnClickListener{

            val mobileNumber = textInputEditTextMobileNumber.text.toString()
            if(mobileNumber.isNotEmpty()){
                if(mobileNumber.length<10){
                    Toast.makeText(this@RequestForPaymentActivity, "Mobile Number length is invalid",Toast.LENGTH_SHORT).show()
                }else{
                    navigateToLinkDetails()
                }
            }else{
                navigateToLinkDetails()
            }
        }

        btnRequestPaymentCancel.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
        }
    }

    private fun validateForm(){
        val amountString = et_amount.text.toString()
        val paymentForString = et_paymentFor.text.toString()

        if (
            (amountString.length) > 4 &&
            (paymentForString.length in 1..255)
        ){
            if (amountString == "PHP 0"){buttonDisable()}
            else{
            buttonEnable()}
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

    private fun navigateToLinkDetails(){
        val amount = et_amount.text.toString()
        val paymentFor = et_paymentFor.text.toString()
        val notes = et_notes.text.toString()
        val mobileNumebr = textInputEditTextMobileNumber.text.toString()

        val intent = Intent(this, LinkDetailsActivity::class.java)
        intent.putExtra(LinkDetailsActivity.EXTRA_AMOUNT, amount)
        intent.putExtra(LinkDetailsActivity.EXTRA_PAYMENT_FOR, paymentFor)
        intent.putExtra(LinkDetailsActivity.EXTRA_NOTES, notes)
        intent.putExtra(LinkDetailsActivity.EXTRA_SELECTED_EXPIRY, linkExpiry)
        intent.putExtra(LinkDetailsActivity.EXTRA_MOBILE_NUMBER, mobileNumebr)
        startActivityForResult(intent, LinkDetailsActivity.REQUEST_CODE)
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


}