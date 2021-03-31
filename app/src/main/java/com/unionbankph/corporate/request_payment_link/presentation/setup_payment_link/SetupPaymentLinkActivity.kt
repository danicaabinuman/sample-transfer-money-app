package com.unionbankph.corporate.request_payment_link.presentation.setup_payment_link

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import androidx.core.content.ContextCompat
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.request_payment_link.presentation.request_payment.RequestForPaymentActivity
import com.unionbankph.corporate.request_payment_link.presentation.setup_payment_link.terms_of_service.TermsOfServiceActivity
import kotlinx.android.synthetic.main.activity_request_payment.*
import kotlinx.android.synthetic.main.activity_setup_payment_links.*

class SetupPaymentLinkActivity : BaseActivity<SetupPaymentLinkViewModel>(R.layout.activity_setup_payment_links) {

    private val PREF_NAME = "setup_payment_link"
    override fun onViewsBound() {
        super.onViewsBound()
        initViews()
        initTermsAndCondition()

        sharedPref()
        buttonDisable()
        requiredFields()
    }

    private fun initViews() {

        btnSetupBusinessLink.setOnClickListener {
            val intent = Intent(this, RequestForPaymentActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnCancel.setOnClickListener {
            finish()
        }

    }

    private fun initTermsAndCondition() {
        tv_link_fnc_tnc.text = Html.fromHtml("I have read the " +
                "<a href=''> Fees and Charges</a>" +
                " and agree to the " +
                "<a href=''>Terms and Condition</a>" +
                " of the Request for Payment")
        tv_link_fnc_tnc.isClickable = true
        tv_link_fnc_tnc.setOnClickListener {
            val intent = Intent(this, TermsOfServiceActivity::class.java)
            startActivity(intent)
        }
//        cb_fnc_tnc.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun sharedPref() {
        val sharedPref: SharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        if (sharedPref.getBoolean(PREF_NAME, false)) {
            val intent = Intent(this, RequestForPaymentActivity::class.java)
            startActivity(intent)
            finish()
        } else {
//            setContentView(R.layout.activity_request_payment_splash_frame_screen)
//            setViewPager()
            val editor = sharedPref.edit()
            editor.putBoolean(PREF_NAME, true)
            editor.apply()
        }
    }

    private fun buttonDisable(){
        btnSetupBusinessLink?.isEnabled = false
        btnSetupBusinessLink?.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorWhite))
        btnSetupBusinessLink?.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.colorGrey))
    }

    private fun buttonEnable(){
        btnSetupBusinessLink?.isEnabled = true
        btnSetupBusinessLink?.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorWhite))
        btnSetupBusinessLink?.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.colorButtonOrange))
    }

    private fun requiredFields(){
        et_business_name.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateForm()
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        et_business_products_offered.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateForm()
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
    }

    private fun validateForm(){
        val businessName = et_business_name.text.toString()
        val businessWebsite = et_business_websites.text.toString()
        val businessProductsOffered = et_business_products_offered.text.toString()

        if (
            businessName.isNotEmpty() &&
            businessProductsOffered.isNotEmpty()
        ){
            buttonEnable()
        } else {
            buttonDisable()
        }
    }
}