package com.unionbankph.corporate.payment_link.presentation.setup_payment_link.nominate_settlement_account

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.common.presentation.callback.AccountAdapterCallback
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.databinding.ActivityNominateSettlementBinding
import com.unionbankph.corporate.payment_link.presentation.billing_details.BillingDetailsActivity

class NominateSettlementActivity :
        BaseActivity<ActivityNominateSettlementBinding, NominateSettlementViewModel>(),
        AccountAdapterCallback
{
    override fun onViewsBound() {
        super.onViewsBound()

        setupInputs()
        binding.btnClose.setOnClickListener{
            finish()
        }
    }


    override fun onViewModelBound() {
        super.onViewModelBound()

        viewModel.accountsBalances.observe(this, Observer {
            it?.let {
                updateRecyclerView(it)
            }
        })
    }

    private fun updateRecyclerView(accounts: MutableList<Account>){
        val accountsAdapter = NominateSettlementAccountsAdapter(accounts)
        binding.rvNominateSettlementAccounts.adapter = accountsAdapter
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

    override val viewModelClassType: Class<NominateSettlementViewModel>
        get() = NominateSettlementViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityNominateSettlementBinding
        get() = ActivityNominateSettlementBinding::inflate
}