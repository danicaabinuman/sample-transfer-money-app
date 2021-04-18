package com.unionbankph.corporate.payment_link.presentation.setup_payment_link

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.payment_link.domain.model.form.CreateMerchantForm
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.payment_link_success.SetupPaymentLinkSuccessfulActivity
import com.unionbankph.corporate.payment_link.presentation.request_payment.RequestForPaymentActivity
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.nominate_settlement_account.NominateSettlementActivity
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.terms_of_service.TermsOfServiceActivity
import io.supercharge.shimmerlayout.ShimmerLayout
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

        llSettlementAccount.visibility = View.GONE
        include1.findViewById<TextView>(R.id.textViewCorporateName).text = "UPASS CA001 TEST ACCOUNT"
        include1.findViewById<TextView>(R.id.textViewAccountNumber).text = "0005 9008 0118"
        include1.findViewById<TextView>(R.id.textViewAccountName).text = "RETAIL REGULAR CHECKING"
        include1.findViewById<TextView>(R.id.textViewAccountNumber).text = "PHP 338,989,378.27"
        llNominateSettlementAccount.visibility = View.VISIBLE
    }


    private fun initViews() {


        include1.setOnClickListener {
            openNominateAccounts()
        }
        btnSetupBusinessLink.setOnClickListener {

            val businessName = et_business_name.text.toString()
            val businessWebsite = et_business_websites.text.toString()
            val businessProductsOffered = et_business_products_offered.text.toString()

            viewModel.createMerchant(
                    CreateMerchantForm(
                            "",
                            businessName,
                            businessName,
                            include1.findViewById<TextView>(R.id.textViewAccountNumber).text.toString(),
                            include1.findViewById<TextView>(R.id.textViewCorporateName).text.toString(),
                            businessWebsite,
                            businessProductsOffered
                    )
            )
        }

        btnCancel.setOnClickListener {
            finish()
        }

        btnNominate.setOnClickListener {
            openNominateAccounts()
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

                val sharedPref: SharedPreferences = getSharedPreferences(SHAREDPREF_IS_DONE_SETUP, Context.MODE_PRIVATE)
                val editor = sharedPref.edit()
                editor.putBoolean(SHAREDPREF_IS_DONE_SETUP, true)
                editor.apply()

                val intent = Intent(this, SetupPaymentLinkSuccessfulActivity::class.java)
                startActivity(intent)
                finish()
            }else{
                Toast.makeText(this@SetupPaymentLinkActivity, "Error", Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.uiState.observe(this, Observer {
            it.getContentIfNotHandled().let { event ->
                when (event) {
                    is UiState.Error -> {
                        handleOnError(event.throwable)
                    }
                }
            }
        })
    }

    private fun openNominateAccounts(){
        val intent = Intent(this@SetupPaymentLinkActivity, NominateSettlementActivity::class.java)
        startActivityForResult(intent,REQUEST_CODE)
    }

    private fun validateForm(){
        val businessName = et_business_name.text.toString()
        val businessProductsOffered = et_business_products_offered.text.toString()

        if (
            businessName.isNotEmpty() &&
            businessProductsOffered.isNotEmpty() &&
            llSettlementAccount.visibility == View.VISIBLE
        ){
            buttonEnable()
        } else {
            buttonDisable()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_CODE){
            if (resultCode == RESULT_OK) {
                val accountData = JsonHelper.fromJson<Account>(data?.getStringExtra(NominateSettlementActivity.RESULT_DATA))
                llSettlementAccount.visibility = View.VISIBLE
                val tvCorporateName: AppCompatTextView = include1.findViewById(R.id.textViewCorporateName)
                val tvAccountName: AppCompatTextView = include1.findViewById(R.id.textViewAccountName)
                val tvAccountNumber: AppCompatTextView = include1.findViewById(R.id.textViewAccountNumber)
                val tvAvailableBalance: AppCompatTextView = include1.findViewById(R.id.textViewAvailableBalance)
                val slAmount: ShimmerLayout = include1.findViewById(R.id.shimmerLayoutAmount)
                val viewShimmer: View = include1.findViewById(R.id.viewShimmer)

                tvCorporateName.text = accountData.name
                tvAccountNumber.text = accountData.accountNumber
                tvAccountName.text = accountData.productCodeDesc

                accountData.headers.forEach{ header ->
                    header.name?.let { headerName ->
                        if(headerName.equals("CURBAL",true)){
                            header.value?.let{ headerValue ->
                                slAmount.stopShimmerAnimation()
                                viewShimmer.visibility = View.GONE
                                tvAvailableBalance.visibility = View.VISIBLE
                                tvAvailableBalance.text = headerValue
                            }
                        }
                    }
                }
                llNominateSettlementAccount.visibility = View.GONE
                validateForm()
            }
        }
    }

    companion object {
        const val SHAREDPREF_IS_DONE_SETUP = "sharedpref_is_done_setup"
        const val REQUEST_CODE = 1216
    }
}