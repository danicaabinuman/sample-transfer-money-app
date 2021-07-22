package com.unionbankph.corporate.payment_link.presentation.request_payment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.app.dashboard.DashboardViewModel
import com.unionbankph.corporate.bills_payment.presentation.organization_payment.OrganizationPaymentActivity
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.databinding.ActivityRequestPaymentBinding
import com.unionbankph.corporate.payment_link.domain.model.response.GeneratePaymentLinkResponse
import com.unionbankph.corporate.payment_link.presentation.onboarding.RequestPaymentSplashActivity
import com.unionbankph.corporate.payment_link.presentation.payment_link_details.LinkDetailsActivity
import com.unionbankph.corporate.payment_link.presentation.request_payment.fee_calculator.FeeCalculatorActivity
import timber.log.Timber

class RequestForPaymentActivity :
    BaseActivity<ActivityRequestPaymentBinding, RequestForPaymentViewModel>(), AdapterView.OnItemSelectedListener {

    var time = arrayOf("6 hours", "12 hours", "1 day", "2 days", "3 days", "7 days")
    val NEW_SPINNER_ID = 1
    var linkExpiry = "12 hours"

    override fun onViewModelBound() {
        super.onViewModelBound()
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initViews()

        setupOutputs()
        buttonDisable()
        buttonCalculatorDisabled()

        requiredFields()
        paymentLinkExpiry()
        finishRequestPayment()
    }

    private fun initViews(){
        binding.btnRequestPaymentGenerate.setOnClickListener{
            val amount = binding.etAmount.text.toString()
            val paymentFor = binding.etPaymentFor.text.toString()
            val notes = binding.etNotes.text.toString()
            val mobileNumber = binding.textInputEditTextMobileNumber.text.toString()
            if(mobileNumber.isNotEmpty()){
                if(mobileNumber.length<10){
                    Toast.makeText(this@RequestForPaymentActivity, "Mobile Number length is invalid",Toast.LENGTH_SHORT).show()
                }else{
                    binding.requestPaymentLoading.visibility = View.VISIBLE
                    viewModel.preparePaymentLinkGeneration(
                        amount,
                        paymentFor,
                        notes,
                        linkExpiry,
                        mobileNumber
                    )
                }
            }else{
                binding.requestPaymentLoading.visibility = View.VISIBLE
                viewModel.preparePaymentLinkGeneration(
                    amount,
                    paymentFor,
                    notes,
                    linkExpiry,
                    mobileNumber
                )
            }
        }

        binding.btnRequestPaymentCancel.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
        }

        binding.btnCalculator.setOnClickListener{
            val amountString = binding.etAmount.text.toString()
            val amountChecker = amountString.replace("PHP","").replace(",","")

            if (amountString.isEmpty()){

            } else {
                binding.btnCalculator.isEnabled
            }
            val bundle = Bundle()
            bundle.putString(FeeCalculatorActivity.AMOUNT_VALUE, amountChecker)

            navigator.navigate(
                this,
                FeeCalculatorActivity::class.java,
                bundle,
                isClear = false,
                isAnimated = true,
                transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_UP
            )
        }
    }

    private fun setupOutputs(){
        viewModel.linkDetailsResponse.observe(this, Observer {
            binding.requestPaymentLoading.visibility = View.GONE
            navigateToLinkDetails(it)
        })

        viewModel._linkDetailsState.observe(this, Observer {
            when(it){
                is ErrorMerchantDisabled -> {
                    binding.errorMerchantDisabled.visibility = View.VISIBLE
                    binding.viewErrorMerchant.btnErrorMerchantDisabled.setOnClickListener{
                        finish()
                    }
                }
            }
        })
    }

    private fun validateForm(){
        val amountString = binding.etAmount.text.toString()
        val paymentForString = binding.etPaymentFor.text.toString()

        val amountChecker = amountString.replace("PHP","").replace(" ","")

        when (amountString) {
            "PHP 0", "PHP 0.", "PHP 0.0", "PHP 0.00" -> {buttonDisable()}
        }

        if (amountChecker.isNotEmpty() && paymentForString.length in 1..100){
            buttonEnable()
        } else {
            buttonDisable()
        }
    }

    private fun buttonCalculatorDisabled(){
        binding.btnCalculator?.isEnabled = false
    }

    private fun buttonCalculatorEnabled(){
        binding.btnCalculator?.isEnabled = true
    }

    private fun buttonDisable(){
        binding.btnRequestPaymentGenerate?.isEnabled = false
        binding.btnRequestPaymentGenerate?.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorWhite))
        binding.btnRequestPaymentGenerate?.setBackgroundResource(R.drawable.bg_splash_payment_request_button_disabled)
    }

    private fun buttonEnable(){
        binding.btnRequestPaymentGenerate?.isEnabled = true
        binding.btnRequestPaymentGenerate?.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorWhite))
        binding.btnRequestPaymentGenerate?.setBackgroundResource(R.drawable.bg_splash_payment_request_button)
    }

    private fun requiredFields(){
        binding.etAmount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val cleanString = s.toString().replace("PHP","").replace(" ","")
                    var amountDouble = 0.00
                    try {
                        amountDouble = cleanString.toDouble()
                        if(amountDouble < 100.00){
                            buttonCalculatorDisabled()
                            binding.tilAmount.error = "Minimum amount is Php 100.00"
                        } else {
                            binding.tilAmount.error = ""
                            buttonCalculatorEnabled()
                        }
                    }catch (e: NumberFormatException){
                        Timber.e(e.message)
                        e.printStackTrace()
                    }

                validateForm()
            }
        })

        binding.etPaymentFor.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val length : Int = binding.etPaymentFor.length()
                val counter : String = length.toString()
                binding.tvTextCounter.text = counter
                binding.tvTextCounter.setHorizontallyScrolling(true)
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateForm()
            }
        })

        binding.textInputEditTextMobileNumber.addTextChangedListener(object : TextWatcher {
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

        with(binding.dropdownPaymentLinkExport){
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
        intent.putExtra(RequestPaymentSplashActivity.EXTRA_FROM_WHAT_TAB, DashboardViewModel.FROM_REQUEST_PAYMENT_BUTTON)
        startActivityForResult(intent, LinkDetailsActivity.REQUEST_CODE)
    }

    private fun navigateToFeeCalculator(){
        val intent = Intent(this, FeeCalculatorActivity::class.java)
        intent.putExtra(FeeCalculatorActivity.FROM_WHAT_TAB, DashboardViewModel.FROM_REQUEST_PAYMENT_BUTTON)
        startActivity(intent)
    }

    private fun finishRequestPayment() {

        binding.ivBackButton.setOnClickListener {
            finish()
        }
    }

    private fun clearAllFields(){
        binding.etAmount.text?.clear()
        binding.etPaymentFor.text?.clear()
        binding.etNotes.text?.clear()
        binding.textInputEditTextMobileNumber.text?.clear()
        binding.etAmount.requestFocus()
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

    override val viewModelClassType: Class<RequestForPaymentViewModel>
        get() = RequestForPaymentViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityRequestPaymentBinding
        get() = ActivityRequestPaymentBinding::inflate
}