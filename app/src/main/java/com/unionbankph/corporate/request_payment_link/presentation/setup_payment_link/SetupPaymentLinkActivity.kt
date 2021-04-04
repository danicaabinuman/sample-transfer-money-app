package com.unionbankph.corporate.request_payment_link.presentation.setup_payment_link

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.link_details.presentation.LinkDetailsViewModel
import com.unionbankph.corporate.request_payment_link.data.form.CreateMerchantForm
import com.unionbankph.corporate.request_payment_link.presentation.setup_payment_link.payment_link_success.SetupPaymentLinkSuccessfulActivity
import com.unionbankph.corporate.request_payment_link.presentation.request_payment.RequestForPaymentActivity
import com.unionbankph.corporate.request_payment_link.presentation.setup_payment_link.terms_of_service.TermsOfServiceActivity
import kotlinx.android.synthetic.main.activity_setup_payment_links.*
import timber.log.Timber

class SetupPaymentLinkActivity : BaseActivity<SetupPaymentLinkViewModel>(R.layout.activity_setup_payment_links) {

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel = ViewModelProviders.of(
            this,
            viewModelFactory
        )[SetupPaymentLinkViewModel::class.java]
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initViews()
        initTermsAndCondition()

        sharedPref()
        buttonDisable()
        requiredFields()
        setupOutputs()
    }


    private fun initViews() {

        val businessName = et_business_name.text.toString()
        val businessWebsite = et_business_websites.text.toString()
        val businessProductsOffered = et_business_products_offered.text.toString()

        btnSetupBusinessLink.setOnClickListener {
            viewModel.createMerchant(
                CreateMerchantForm(
                    "",
                    businessName,
                    businessName,
                    "1096822",
                    "Unnecessary",
                    businessWebsite,
                    businessProductsOffered
                )
            )
        }

        btnSetupBusinessLink.setOnLongClickListener{
            val sharedPref: SharedPreferences = getSharedPreferences(SHAREDPREF_IS_DONE_SETUP, Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putBoolean(SHAREDPREF_IS_DONE_SETUP, true)
            editor.commit()
        }

        btnCancel.setOnClickListener {
            finish()
        }

        btnNominate.setOnClickListener {
            viewModel.getAccounts()
        }

    }

    private fun initTermsAndCondition() {
        tv_link_fnc_tnc.text = Html.fromHtml("I have read the " +
                "<a href=''> Fees and Charges</a>" +
                " and agree to the " +
                "<a href=''>Terms and Conditions</a>" +
                " of the Request for Payment")
        tv_link_fnc_tnc.isClickable = true
        tv_link_fnc_tnc.setOnClickListener {
            val intent = Intent(this, TermsOfServiceActivity::class.java)
            startActivity(intent)
            cb_fnc_tnc.isChecked = true
        }
    }

    private fun sharedPref() {
        val sharedPref: SharedPreferences = getSharedPreferences(SHAREDPREF_IS_DONE_SETUP, Context.MODE_PRIVATE)
        if (sharedPref.getBoolean(SHAREDPREF_IS_DONE_SETUP, false)) {
            val intent = Intent(this, RequestForPaymentActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun buttonDisable(){
        btnSetupBusinessLink?.isEnabled = false
        btnSetupBusinessLink?.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorWhite))
        btnSetupBusinessLink?.setBackgroundResource(R.drawable.bg_splash_payment_request_button_disabled)
    }

    private fun buttonEnable(){
        btnSetupBusinessLink?.isEnabled = true
        btnSetupBusinessLink?.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorWhite))
        btnSetupBusinessLink?.setBackgroundResource(R.drawable.bg_splash_payment_request_button)
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

    private fun setupOutputs() {
        viewModel.createMerchantResponse.observe(this, Observer {
            if(it.message.equals("Success",true)){
                val intent = Intent(this, SetupPaymentLinkSuccessfulActivity::class.java)
                startActivity(intent)
                finish()
            }else{
                Toast.makeText(this@SetupPaymentLinkActivity, "Error", Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.accounts.observe(this, Observer {
            it?.let {
                openNominateAccounts(it)
            }
        })
    }

    private fun openNominateAccounts(accounts: MutableList<Account>){
        if(accounts.size>0){
            Timber.d("Size of accounts = ${accounts.size}")
        }else{
            Toast.makeText(this@SetupPaymentLinkActivity, "No Accounts", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateForm(){
        val businessName = et_business_name.text.toString()
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

    companion object {
        const val SHAREDPREF_IS_DONE_SETUP = "sharedpref_is_done_setup"
    }
}