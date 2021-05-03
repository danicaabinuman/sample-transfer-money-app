package com.unionbankph.corporate.payment_link.presentation.setup_payment_link

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.mtramin.rxfingerprint.RxFingerprint
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.dashboard.*
import com.unionbankph.corporate.common.presentation.constant.PromptTypeEnum
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.payment_link.domain.model.form.CreateMerchantForm
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.payment_link_success.SetupPaymentLinkSuccessfulActivity
import com.unionbankph.corporate.payment_link.presentation.request_payment.RequestForPaymentActivity
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.nominate_settlement_account.NominateSettlementActivity
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.terms_of_service.TermsOfServiceActivity
import io.supercharge.shimmerlayout.ShimmerLayout
import kotlinx.android.synthetic.main.activity_link_details.*
import kotlinx.android.synthetic.main.activity_no_available_accounts.*
import kotlinx.android.synthetic.main.activity_setup_payment_links.*
import kotlinx.android.synthetic.main.activity_setup_payment_links.ivBackButton
import kotlinx.android.synthetic.main.fragment_request_payment_error_dialog.*

class SetupPaymentLinkActivity : BaseActivity<SetupPaymentLinkViewModel>(R.layout.activity_setup_payment_links) {

    private var accounts = mutableListOf<Account>()
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

        buttonDisable()
        requiredFields()
        setupInputs()
        setupOutputs()
        backButton()

        llSettlementAccount.visibility = View.GONE
        noAvailableAccounts.visibility = View.GONE
    }

    private fun setupInputs(){
        setupPaymentLinkLoading.visibility = View.VISIBLE
        viewModel.validateIfApprover()
    }


    private fun initViews() {


        include1.setOnClickListener {
            openNominateAccounts()
        }
        btnSetupBusinessLink.setOnClickListener {

            setupPaymentLinkLoading.visibility = View.VISIBLE
            til_business_name.error = null

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

        btnBackToDashboard.setOnClickListener {
            finish()
        }

        btnFeatureUnavailableBackToDashboard.setOnClickListener {
            finish()
        }

        if( BuildConfig.DEBUG){
            btnFeatureUnavailableBackToDashboard.setOnLongClickListener {
                approverPermissionRequired.visibility = View.GONE
                setupPaymentLinkLoading.visibility = View.VISIBLE
                viewModel.getAccounts()
                return@setOnLongClickListener true
            }
        }


    }

    private fun initTermsAndCondition() {

        val spannableString = SpannableString("I have read the Fees and Charges and agree to the Terms and Conditions of the Request for Payment.")

        val clickableSpan1 = object : ClickableSpan(){
            override fun onClick(widget: View) {
                cb_fnc_tnc.isChecked = true
                val intent = Intent(this@SetupPaymentLinkActivity, TermsOfServiceActivity::class.java)
                startActivity(intent)
            }
        }

        val clickableSpan2 = object : ClickableSpan(){
            override fun onClick(widget: View) {
                cb_fnc_tnc.isChecked = true
                val intent = Intent(this@SetupPaymentLinkActivity, TermsOfServiceActivity::class.java)
                intent.putExtra("termsAndConditions", "TC")
                startActivity(intent)
            }

        }

        spannableString.setSpan(clickableSpan1, 16, 32, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(clickableSpan2, 50, 70, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)

        tv_link_fnc_tnc.text = spannableString
        tv_link_fnc_tnc.movementMethod = LinkMovementMethod.getInstance()
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

        cb_fnc_tnc.setOnCheckedChangeListener { _, _ ->
            validateForm()
        }
    }

    private fun setupOutputs() {
        viewModel.createMerchantResponse.observe(this, Observer {
            setupPaymentLinkLoading.visibility = View.GONE
            if(it.message.equals("Success",true)){

                val intent = Intent(this, SetupPaymentLinkSuccessfulActivity::class.java)
                startActivity(intent)
                finish()
            }else{
                Toast.makeText(this@SetupPaymentLinkActivity, "Error", Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.soleAccount.observe(this, Observer {
            setupPaymentLinkLoading.visibility = View.GONE
            populateNominatedSettlementAccount(it)
            accounts = mutableListOf()
            accounts.add(it)

        })

        viewModel.accounts.observe(this, Observer {
            setupPaymentLinkLoading.visibility = View.GONE
            accounts = it
        })

        viewModel.accountsBalances.observe(this, Observer {
            setupPaymentLinkLoading.visibility = View.GONE
            populateNominatedSettlementAccount(it.first())
        })

        viewModel.uiState.observe(this, Observer {
            it.getContentIfNotHandled().let { event ->
                when (event) {
                    is UiState.Loading -> {
                        setupPaymentLinkLoading.visibility = View.VISIBLE
                    }
                    is UiState.Complete -> {
                        setupPaymentLinkLoading.visibility = View.GONE
                    }
                    is UiState.Error -> {
                        handleOnError(event.throwable)
                    }
                }
            }
        })

        viewModel.setupPaymentLinkState.observe(this, Observer {
            setupPaymentLinkLoading.visibility = View.GONE
            when (it) {
                is ShowNoAvailableAccounts -> {
                    noAvailableAccounts.visibility = View.VISIBLE
                }

                is ShowHandleNotAvailable -> {
                    til_business_name.error = "This handle is no longer available. Please try another one."
                }

                is ShowApproverPermissionRequired -> {
                    approverPermissionRequired.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun openNominateAccounts(){
        if(accounts.size>1){
            val intent = Intent(this@SetupPaymentLinkActivity, NominateSettlementActivity::class.java)
            val accountsJson = JsonHelper.toJson(accounts)
            intent.putExtra(NominateSettlementActivity.EXTRA_ACCOUNTS_ARRAY, accountsJson)
            startActivityForResult(intent,REQUEST_CODE)
        }else if(accounts.size == 1){
            if(include1.visibility == View.VISIBLE){
                //DO NOTHING
            }
        }else {
            noAvailableAccounts.visibility = View.VISIBLE
        }

    }

    private fun backButton(){

        ivBackButton.setOnClickListener{
            val intent = Intent (this, DashboardActivity::class.java)
            startActivity(intent)
        }

    }

    private fun validateForm(){
        val businessName = et_business_name.text.toString()
        val businessProductsOffered = et_business_products_offered.text.toString()
        val isChecked = cb_fnc_tnc.isChecked

        if (
            businessName.isNotEmpty() &&
            businessProductsOffered.isNotEmpty() &&
            llSettlementAccount.visibility == View.VISIBLE
            && isChecked
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
                populateNominatedSettlementAccount(accountData)
                validateForm()
            }
        }
    }

    private fun populateNominatedSettlementAccount(accountData: Account){
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
    }

    companion object {
        const val REQUEST_CODE = 1216
    }
}