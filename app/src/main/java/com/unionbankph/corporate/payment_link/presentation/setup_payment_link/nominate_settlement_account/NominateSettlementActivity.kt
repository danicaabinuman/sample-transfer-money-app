package com.unionbankph.corporate.payment_link.presentation.setup_payment_link.nominate_settlement_account

import androidx.lifecycle.Observer
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.common.presentation.callback.AccountAdapterCallback
import kotlinx.android.synthetic.main.activity_nominate_settlement.*

class NominateSettlementActivity :
        BaseActivity<NominateSettlementViewModel>(R.layout.activity_nominate_settlement),
        AccountAdapterCallback
{
    override fun onViewsBound() {
        super.onViewsBound()
        setupInputs()
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel.settlementAccounts.observe(this, Observer {
            it?.let {
                updateRecyclerView(it)
            }
        })
    }
    private fun setupInputs() {
        viewModel.initBundleData(
                intent.getStringExtra(EXTRA_SETTLEMENT_ACCOUNTS)
        )
    }

    private fun updateRecyclerView(accounts: MutableList<Account>){
        rvNominateSettlementAccounts.adapter = NominateSettlementAccountsAdapter(accounts)
    }

    companion object{
        const val EXTRA_SETTLEMENT_ACCOUNTS = "extra_settlement_accounts"
    }
}