package com.unionbankph.corporate.payment_link.presentation.request_payment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.app.dashboard.DashboardViewModel
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.payment_link.domain.model.response.GeneratePaymentLinkResponse
import com.unionbankph.corporate.payment_link.presentation.onboarding.RequestPaymentSplashActivity
import com.unionbankph.corporate.payment_link.presentation.payment_link_details.LinkDetailsActivity
import com.unionbankph.corporate.payment_link.presentation.request_payment.fee_calculator.FeeCalculatorActivity
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.nominate_settlement_account.NominateSettlementAccountFragment
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.nominate_settlement_account.NominateSettlementActivity
import io.supercharge.shimmerlayout.ShimmerLayout
import kotlinx.android.synthetic.main.activity_no_available_accounts.*
import kotlinx.android.synthetic.main.activity_request_payment.*
import kotlinx.android.synthetic.main.activity_request_payment.errorMerchantDisabled
import kotlinx.android.synthetic.main.activity_request_payment.ivBackButton
import kotlinx.android.synthetic.main.activity_setup_payment_links.*
import kotlinx.android.synthetic.main.dialog_failed_merchant_diasbled.*
import kotlinx.android.synthetic.main.fragment_send_request.*
import timber.log.Timber

class RequestForPaymentActivity : BaseActivity<RequestForPaymentViewModel>(R.layout.activity_request_payment),
    AdapterView.OnItemSelectedListener,
    NominateSettlementAccountFragment.OnNominateSettlementAccountListener{

    private var accounts = mutableListOf<Account>()
    private var time = arrayOf("6 hours", "12 hours", "1 day", "2 days", "3 days", "7 days")
    private val NEW_SPINNER_ID = 1
    private var linkExpiry = "12 hours"
    private var nominateSettlementAccountFragment: NominateSettlementAccountFragment? = null

    private var currentAccount: Account = Account()

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel = ViewModelProviders.of(
            this,
            viewModelFactory
        )[RequestForPaymentViewModel::class.java]
        setupOutputs()
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initViews()

        setupInputs()

        buttonDisable()
        buttonCalculatorDisabled()

        requiredFields()
        paymentLinkExpiry()
        finishRequestPayment()
    }


    private fun initViews(){
        requestPaymentLoading.visibility = View.VISIBLE

        include_settlement_account.setOnClickListener {
            openNominateAccounts()
        }

        btnBackToDashboard.setOnClickListener {
            finish()
        }

        btnRequestPaymentGenerate.setOnClickListener{
            val amount = et_amount.text.toString()
            val paymentFor = et_paymentFor.text.toString()
            val notes = et_notes.text.toString()
            val mobileNumber = textInputEditTextMobileNumber.text.toString()
            val accountNo = include1.findViewById<TextView>(R.id.textViewAccountNumber).text.toString()
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

        btnCalculator.setOnClickListener{
            val amountString = et_amount.text.toString()
            val amountChecker = amountString.replace("PHP","").replace(",","")

            if(!amountString.isEmpty())btnCalculator.isEnabled
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

    private fun setupInputs(){
        viewModel.getDefaultMerchantSettlementAccount()
    }

    private fun setupOutputs(){
        viewModel.linkDetailsResponse.observe(this, Observer {
            requestPaymentLoading.visibility = View.GONE
            navigateToLinkDetails(it)
        })

        viewModel._linkDetailsState.observe(this, Observer {
            when(it){
                is ErrorMerchantDisabled -> {
                    errorMerchantDisabled.visibility = View.VISIBLE
                    btnErrorMerchantDisabled.setOnClickListener {
                        finish()
                    }
                }
                is ShowNoOtherAvailableAccounts -> {
                    noOtherAvailableAccounts.visibility = View.VISIBLE
                }

                is ShowTheApproverPermissionRequired -> {
                    theApproverPermissionRequired.visibility = View.VISIBLE
                }
            }
        })

        viewModel.defaultMerchantSA.observe(this, Observer {
            currentAccount = it
            populateNominatedSettlementAccount(it)
        })

        viewModel.soleAccount.observe(this, Observer {
            currentAccount = it

            requestPaymentLoading.visibility = View.GONE
            populateNominatedSettlementAccount(it)
            accounts = mutableListOf()
            accounts.add(it)

        })

        viewModel.accounts.observe(this, Observer {
            requestPaymentLoading.visibility = View.GONE
            populateNominatedSettlementAccount(it.first())
            accounts = it
        })

        viewModel.accountsBalances.observe(this, Observer {
            requestPaymentLoading.visibility = View.GONE
            populateNominatedSettlementAccount(it.first())
        })

        viewModel.updateSettlementOnRequestPaymentResponse.observe(this, Observer {
            populateNominatedSettlementAccount(currentAccount)
        })

        viewModel.uiState.observe(this, Observer {
            it.getContentIfNotHandled().let { event ->
                when (event) {
                    is UiState.Loading -> {
                        requestPaymentLoading.visibility = View.VISIBLE
                    }
                    is UiState.Complete -> {
                        requestPaymentLoading.visibility = View.GONE
                    }
                    is UiState.Error -> {
                        handleOnError(event.throwable)
                    }
                }
            }
        })


    }

    private fun validateForm(){
        val amountString = et_amount.text.toString()
        val paymentForString = et_paymentFor.text.toString()

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
        btnCalculator?.isEnabled = false
    }

    private fun buttonCalculatorEnabled(){
        btnCalculator?.isEnabled = true
    }

    private fun buttonDisable(){
        btnRequestPaymentGenerate?.isEnabled = false
        btnRequestPaymentGenerate?.setTextColor(ContextCompat.getColor(applicationContext, R.color.dsColorLightGray))
    }

    private fun buttonEnable(){
        btnRequestPaymentGenerate?.isEnabled = true
        btnRequestPaymentGenerate?.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorWhite))
    }

    private fun requiredFields(){
        et_amount.addTextChangedListener(object : TextWatcher {
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
                            til_amount.error = "Minimum amount is Php 100.00"
                        } else {
                            til_amount.error = ""
                            buttonCalculatorEnabled()
                        }
                    }catch (e: NumberFormatException){
                        Timber.e(e)
                        e.printStackTrace()
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
        intent.putExtra(RequestPaymentSplashActivity.EXTRA_FROM_WHAT_TAB, DashboardViewModel.FROM_REQUEST_PAYMENT_BUTTON)
        startActivityForResult(intent, LinkDetailsActivity.REQUEST_CODE)
    }

    private fun navigateToFeeCalculator(){
        val intent = Intent(this, FeeCalculatorActivity::class.java)
        intent.putExtra(FeeCalculatorActivity.FROM_WHAT_TAB, DashboardViewModel.FROM_REQUEST_PAYMENT_BUTTON)
        startActivity(intent)
    }

    private fun openNominateAccounts(){
        if(accounts.size>1){
            nominateSettlementAccountFragment = NominateSettlementAccountFragment.newInstance(JsonHelper.toJson(accounts))
            nominateSettlementAccountFragment?.setOnNominateSettlementAccountListener(this)
            nominateSettlementAccountFragment?.show(
                supportFragmentManager,
                RequestForPaymentActivity::class.java.simpleName
            )

        }else{
            noOtherAvailableAccounts.visibility = View.VISIBLE
        }

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

    private fun populateNominatedSettlementAccount(accountData: Account){
        val tvCorporateName: AppCompatTextView = include_settlement_account.findViewById(R.id.textViewCorporateName)
        val tvAccountName: AppCompatTextView = include_settlement_account.findViewById(R.id.textViewAccountName)
        val tvAccountNumber: AppCompatTextView = include_settlement_account.findViewById(R.id.textViewAccountNumber)
        val tvAvailableBalance: AppCompatTextView = include_settlement_account.findViewById(R.id.textViewAvailableBalance)
        val slAmount: ShimmerLayout = include_settlement_account.findViewById(R.id.shimmerLayoutAmount)
        val viewShimmer: View = include_settlement_account.findViewById(R.id.viewShimmer)

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

    }

    override fun onAccountSelected(account: Account) {
//        Timber.e()
        currentAccount = account
        // Call Put Merchant Here.
        viewModel.updateDefaultSettlementAccount(account.accountNumber!!)
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
        }else if(requestCode == REQUEST_CODE){
            if (resultCode == RESULT_OK) {
                val accountData = JsonHelper.fromJson<Account>(data?.getStringExtra(NominateSettlementActivity.RESULT_DATA))
                populateNominatedSettlementAccount(accountData)
            }
        }
    }
    companion object {
        const val REQUEST_CODE = 1226

    }


}