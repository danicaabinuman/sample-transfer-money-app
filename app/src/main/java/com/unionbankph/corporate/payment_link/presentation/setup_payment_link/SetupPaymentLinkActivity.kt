package com.unionbankph.corporate.payment_link.presentation.setup_payment_link

import android.content.Intent
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.dashboard.*
import com.unionbankph.corporate.auth.data.model.Role
import com.unionbankph.corporate.common.data.source.local.cache.CacheManager
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.databinding.ActivitySetupPaymentLinksBinding
import com.unionbankph.corporate.payment_link.domain.model.form.CreateMerchantForm
import com.unionbankph.corporate.payment_link.presentation.onboarding.RequestPaymentSplashActivity
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.payment_link_success.SetupPaymentLinkSuccessfulActivity
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.nominate_settlement_account.NominateSettlementActivity
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.terms_of_service.TermsOfServiceActivity
import io.supercharge.shimmerlayout.ShimmerLayout

class SetupPaymentLinkActivity :
    BaseActivity<ActivitySetupPaymentLinksBinding, SetupPaymentLinkViewModel>() {

    private var accounts = mutableListOf<Account>()
    private var fromWhatTab : String? = null

    override fun onViewModelBound() {
        super.onViewModelBound()
    }

    override fun onViewsBound() {
        super.onViewsBound()


        fromWhatTab = intent.getStringExtra(RequestPaymentSplashActivity.EXTRA_FROM_WHAT_TAB)
        if(fromWhatTab == null){
            fromWhatTab = DashboardViewModel.FROM_REQUEST_PAYMENT_BUTTON
        }

        initViews()
        initTermsAndCondition()

        buttonDisable()
        requiredFields()
        setupInputs()
        setupOutputs()
        backButton()

        binding.llSettlementAccount.visibility = View.GONE
        binding.noAvailableAccounts.visibility = View.GONE
    }

    private fun setupInputs(){
        binding.setupPaymentLinkLoading.visibility = View.VISIBLE
        viewModel.validateIfApprover()
    }


    private fun initViews() {


        binding.include1.root.setOnClickListener {
            openNominateAccounts()
        }
        binding.btnSetupBusinessLink.setOnClickListener {

            binding.setupPaymentLinkLoading.visibility = View.VISIBLE
            binding.tilBusinessName.error = null

            val role = cacheManager.getObject(CacheManager.ROLE) as? Role
            val orgName = role?.organizationName
            val businessName = binding.etBusinessName.text.toString()
            val businessWebsite = binding.etBusinessWebsites.text.toString()
            val businessProductsOffered = binding.etBusinessProductsOffered.text.toString()

            viewModel.createMerchant(
                    CreateMerchantForm(
                            orgName!!,
                            businessName,
                            binding.include1.textViewAccountNumber.text.toString(),
                            binding.include1.textViewCorporateName.text.toString(),
                            businessWebsite,
                            businessProductsOffered
                    )
            )
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }

        binding.btnNominate.setOnClickListener {
            openNominateAccounts()
        }

        binding.viewNoAvailableAccounts.btnBackToDashboard.setOnClickListener {
            finish()
        }

        binding.viewDialogFeatureUnavailable.btnFeatureUnavailableBackToDashboard.setOnClickListener {
            finish()
        }

        if( BuildConfig.DEBUG){
            binding.viewDialogFeatureUnavailable.btnFeatureUnavailableBackToDashboard.setOnLongClickListener {
                binding.approverPermissionRequired.visibility = View.GONE
                binding.setupPaymentLinkLoading.visibility = View.VISIBLE
                viewModel.getAccounts()
                return@setOnLongClickListener true
            }
        }


    }

    private fun initTermsAndCondition() {

        val spannableString = SpannableString("I have read the Fees and Charges and agree to the Terms and Conditions of the Request for Payment.")

        val clickableSpan1 = object : ClickableSpan(){
            override fun onClick(widget: View) {
                binding.cbFncTnc.isChecked = true
                val intent = Intent(this@SetupPaymentLinkActivity, TermsOfServiceActivity::class.java)
                startActivity(intent)
            }
        }

        val clickableSpan2 = object : ClickableSpan(){
            override fun onClick(widget: View) {
                binding.cbFncTnc.isChecked = true
                val intent = Intent(this@SetupPaymentLinkActivity, TermsOfServiceActivity::class.java)
                intent.putExtra("termsAndConditions", "TC")
                startActivity(intent)
            }

        }

        spannableString.setSpan(clickableSpan1, 16, 32, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(clickableSpan2, 50, 70, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)

        binding.tvLinkFncTnc.text = spannableString
        binding.tvLinkFncTnc.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun buttonDisable(){
        binding.btnSetupBusinessLink.isEnabled = false
        binding.btnSetupBusinessLink.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorWhite))
        binding.btnSetupBusinessLink.setBackgroundResource(R.drawable.bg_splash_payment_request_button_disabled)
    }

    private fun buttonEnable(){
        binding.btnSetupBusinessLink.isEnabled = true
        binding.btnSetupBusinessLink.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorWhite))
        binding.btnSetupBusinessLink.setBackgroundResource(R.drawable.bg_splash_payment_request_button)
    }

    private fun requiredFields(){
        binding.etBusinessName.setOnFocusChangeListener { v, hasFocus ->
            binding.tilBusinessName.error = null
        }
        binding.etBusinessName.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateForm()
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        binding.etBusinessProductsOffered.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateForm()
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        binding.cbFncTnc.setOnCheckedChangeListener { _, _ ->
            validateForm()
        }
    }

    private fun setupOutputs() {
        viewModel.createMerchantResponse.observe(this, Observer {
            binding.setupPaymentLinkLoading.visibility = View.GONE
            if(it.message.equals("Success",true)){

                val intent = Intent(this, SetupPaymentLinkSuccessfulActivity::class.java)
                intent.putExtra(RequestPaymentSplashActivity.EXTRA_FROM_WHAT_TAB,fromWhatTab)
                startActivity(intent)
                finish()
            }else{
                Toast.makeText(this@SetupPaymentLinkActivity, "Error", Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.soleAccount.observe(this, Observer {
            binding.setupPaymentLinkLoading.visibility = View.GONE
            populateNominatedSettlementAccount(it)
            accounts = mutableListOf()
            accounts.add(it)

        })

        viewModel.accounts.observe(this, Observer {
            binding.setupPaymentLinkLoading.visibility = View.GONE
            accounts = it
        })

        viewModel.accountsBalances.observe(this, Observer {
            binding.setupPaymentLinkLoading.visibility = View.GONE
            populateNominatedSettlementAccount(it.first())
        })

        viewModel.uiState.observe(this, Observer {
            it.getContentIfNotHandled().let { event ->
                when (event) {
                    is UiState.Loading -> {
                        binding.setupPaymentLinkLoading.visibility = View.VISIBLE
                    }
                    is UiState.Complete -> {
                        binding.setupPaymentLinkLoading.visibility = View.GONE
                    }
                    is UiState.Error -> {
                        handleOnError(event.throwable)
                    }
                }
            }
        })

        viewModel.setupPaymentLinkState.observe(this, Observer {
            binding.setupPaymentLinkLoading.visibility = View.GONE
            when (it) {
                is ShowNoAvailableAccounts -> {
                    binding.noAvailableAccounts.visibility = View.VISIBLE
                }
                is ShowHandleNotAvailable -> {
                    binding.tilBusinessName.error = "This handle is no longer available. Please try another one"
                }
                is ShowApproverPermissionRequired -> {
                    binding.approverPermissionRequired.visibility = View.VISIBLE
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
            if(binding.include1.root.visibility == View.VISIBLE){
                //DO NOTHING
            }
        }else {
            binding.noAvailableAccounts.visibility = View.VISIBLE
        }

    }

    private fun backButton(){

        binding.ivBackButton.setOnClickListener{
            val intent = Intent (this, DashboardActivity::class.java)
            startActivity(intent)
        }

    }

    private fun validateForm(){
        val businessName = binding.etBusinessName.text.toString()
        val businessProductsOffered = binding.etBusinessProductsOffered.text.toString()
        val isChecked = binding.cbFncTnc.isChecked

        if (
            businessName.isNotEmpty() &&
            businessProductsOffered.isNotEmpty() &&
            binding.llSettlementAccount.visibility == View.VISIBLE
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
        binding.llSettlementAccount.visibility = View.VISIBLE
        val tvCorporateName: AppCompatTextView = binding.include1.textViewCorporateName
        val tvAccountName: AppCompatTextView = binding.include1.textViewAccountName
        val tvAccountNumber: AppCompatTextView = binding.include1.textViewAccountNumber
        val tvAvailableBalance: AppCompatTextView = binding.include1.textViewAvailableBalance
        val slAmount: ShimmerLayout = binding.include1.shimmerLayoutAmount
        val viewShimmer: View = binding.include1.viewShimmer

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
        binding.llNominateSettlementAccount.visibility = View.GONE
    }

    companion object {
        const val REQUEST_CODE = 1216
    }

    override val viewModelClassType: Class<SetupPaymentLinkViewModel>
        get() = SetupPaymentLinkViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivitySetupPaymentLinksBinding
        get() = ActivitySetupPaymentLinksBinding::inflate
}