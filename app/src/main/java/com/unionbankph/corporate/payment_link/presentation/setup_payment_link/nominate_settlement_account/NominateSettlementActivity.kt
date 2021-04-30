package com.unionbankph.corporate.payment_link.presentation.setup_payment_link.nominate_settlement_account

import android.content.Intent
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.common.presentation.callback.AccountAdapterCallback
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.payment_link.presentation.billing_details.BillingDetailsActivity
import kotlinx.android.synthetic.main.activity_billing_details.*
import kotlinx.android.synthetic.main.activity_nominate_settlement.*

class NominateSettlementActivity :
        BaseActivity<NominateSettlementViewModel>(R.layout.activity_nominate_settlement),
        AccountAdapterCallback
{
    override fun onViewsBound() {
        super.onViewsBound()

        setupInputs()
        btnClose.setOnClickListener{
            finish()
        }
    }


    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel = ViewModelProviders.of(
                this,
                viewModelFactory
        )[NominateSettlementViewModel::class.java]

        viewModel.accountsBalances.observe(this, Observer {
            it?.let {
                updateRecyclerView(it)
            }
        })
    }

    private fun updateRecyclerView(accounts: MutableList<Account>){
        val accountsAdapter = NominateSettlementAccountsAdapter(accounts)
        rvNominateSettlementAccounts.adapter = accountsAdapter
        accountsAdapter.onItemClick = {
            passAccountAsResult(it)
        }

    }

    private fun passAccountAsResult(account: Account) {
        val data = Intent()
        data.putExtra(RESULT_DATA, JsonHelper.toJson(account));

        setResult(RESULT_OK, data);
        finish()
    }

    private fun setupInputs() {

        val accountsArrayJson = intent.getStringExtra(EXTRA_ACCOUNTS_ARRAY).toString()
        viewModel.initBundleData(
            accountsArrayJson
        )

    }

    companion object{
        const val RESULT_DATA = "RESULT_DATA"
        const val EXTRA_ACCOUNTS_ARRAY = "EXTRA_ACCOUNTS_ARRAY"
    }
}