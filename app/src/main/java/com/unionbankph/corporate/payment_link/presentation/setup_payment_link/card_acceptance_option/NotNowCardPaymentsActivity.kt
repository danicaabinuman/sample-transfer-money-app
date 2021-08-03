package com.unionbankph.corporate.payment_link.presentation.setup_payment_link.card_acceptance_option

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.nominate_settlement_account.NominateSettlementActivity
import io.supercharge.shimmerlayout.ShimmerLayout
import kotlinx.android.synthetic.main.activity_not_now_accept_card_payments.*
import kotlinx.android.synthetic.main.item_nominate_account.*
import java.text.DecimalFormat

class NotNowCardPaymentsActivity : BaseActivity<NotNowCardPaymentsViewModel>(R.layout.activity_not_now_accept_card_payments) {

    private var accounts = mutableListOf<Account>()

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel = ViewModelProviders.of(
            this,
            viewModelFactory
        )[NotNowCardPaymentsViewModel::class.java]

    }
    override fun onViewsBound() {
        super.onViewsBound()

        initViews()

    }

    private fun initViews(){

        setupInputs()
        setupOutputs()
        enterMonthlyVolume()
        enterTransactionAmount()
        selectAccount()
        sliderMonthly()
        sliderTransaction()
    }

    private fun enterMonthlyVolume(){
        etMonthlyVolume.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tvForSliderMonthly.visibility = View.GONE

                val monthlyAmount = s.toString()
                if (start < 5){
                    sliderMonthlyVolume.value = 10000F
                } else if (start >= 5){
                    sliderMonthlyVolume.value = monthlyAmount.toFloat()
                } else if (start == 9){
                    sliderMonthlyVolume.value = 3000000F
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }

    private fun enterTransactionAmount(){
        etTransactionAmount.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tvForSliderTransaction.visibility = View.GONE

                val transactionAmount = s.toString()
                if (start < 5){
                    sliderTransactionAmount.value = 10000F
                } else if (start >= 5){
                    sliderTransactionAmount.value = transactionAmount.toFloat()
                } else if (start >= 9){
                    sliderTransactionAmount.value = 3000000F
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }

    private fun sliderMonthly(){
        sliderMonthlyVolume.addOnChangeListener { slider, value, fromUser ->
            if (fromUser){
                val sliderValue = sliderMonthlyVolume.value
                val amountParse = DecimalFormat("#,###")

                tvForSliderMonthly.visibility = View.VISIBLE
                tvForSliderMonthly.text = amountParse.format(sliderValue)
            }

        }
    }

    private fun sliderTransaction(){
        sliderTransactionAmount.addOnChangeListener { slider, value, fromUser ->
            if (fromUser){
                val sliderValue = sliderTransactionAmount.value
                val amountParse = DecimalFormat("#,###")

                tvForSliderMonthly.visibility = View.VISIBLE
                tvForSliderMonthly.text = amountParse.format(sliderValue)
            }

        }
    }


    private fun selectAccount(){
        btnSelectAccount.setOnClickListener {
            openNominateAccounts()
        }
    }

    private fun openNominateAccounts(){
        if(accounts.size>1){
            val bundle = Bundle().apply {
                putString(
                    NominateSettlementActivity.EXTRA_ACCOUNTS_ARRAY,
                    JsonHelper.toJson(accounts)
                )
            }
            navigator.navigateForResult(
                this,
                NominateSettlementActivity::class.java,
                bundle,
                isClear = false,
                isAnimated = true,
                resultCode = REQUEST_CODE,
                transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
            )
        }else{
            noOtherAvailableAccounts.visibility = View.VISIBLE
        }

    }

    private fun setupInputs(){
        viewModel.getAccounts()
    }

    private fun setupOutputs(){
        viewModel.soleAccount.observe(this, Observer {
            cardPaymentsLoading.visibility = View.GONE
            populateNominatedSettlementAccount(it)
            accounts = mutableListOf()
            accounts.add(it)

        })

        viewModel.accounts.observe(this, Observer {
            cardPaymentsLoading.visibility = View.GONE
            populateNominatedSettlementAccount(it.first())
            accounts = it
        })

        viewModel.accountsBalances.observe(this, Observer {
            cardPaymentsLoading.visibility = View.GONE
            populateNominatedSettlementAccount(it.first())
        })

        viewModel.uiState.observe(this, Observer {
            it.getContentIfNotHandled().let { event ->
                when (event) {
                    is UiState.Loading -> {
                        cardPaymentsLoading.visibility = View.VISIBLE
                    }
                    is UiState.Complete -> {
                        cardPaymentsLoading.visibility = View.GONE
                    }
                    is UiState.Error -> {
                        handleOnError(event.throwable)
                    }
                }
            }
        })

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

    companion object {
        const val REQUEST_CODE = 200
    }
}