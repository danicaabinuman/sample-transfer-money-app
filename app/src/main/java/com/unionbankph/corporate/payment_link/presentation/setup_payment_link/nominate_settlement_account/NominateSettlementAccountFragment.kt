package com.unionbankph.corporate.payment_link.presentation.setup_payment_link.nominate_settlement_account

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.app.base.BaseBottomSheetDialog
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.data.model.Account
import kotlinx.android.synthetic.main.fragment_nominate_settlement_account.*

class NominateSettlementAccountFragment : BaseBottomSheetDialog<NominateSettlementViewModel>(R.layout.fragment_nominate_settlement_account)
{
         private var callback: OnNominateSettlementAccountListener? = null


         override fun onViewsBound() {
             super.onViewsBound()
             button_close_dialog.setOnClickListener { this.dismiss() }
             setupInputs()
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
             rvBSNominateSettlementAccounts.adapter = accountsAdapter
             accountsAdapter.onItemClick = {
                 this.dismiss()
                 callback?.onAccountSelected(it)

             }

         }


         private fun setupInputs() {
             val accountsArrayJson = arguments?.getString(EXTRA_ACCOUNTS_ARRAY).toString()
             viewModel.initBundleData(
                 accountsArrayJson
             )
         }

         fun setOnNominateSettlementAccountListener(onNominateSettlementAccountListener: OnNominateSettlementAccountListener) {
             this.callback = onNominateSettlementAccountListener
         }


         interface OnNominateSettlementAccountListener {
             fun onAccountSelected(account: Account)
         }

         companion object {

        const val RESULT_DATA = "RESULT_DATA"
        const val EXTRA_ACCOUNTS_ARRAY = "EXTRA_ACCOUNTS_ARRAY"

        fun newInstance(
            accountsArray: String? = null
        ): NominateSettlementAccountFragment {
            val fragment =
                NominateSettlementAccountFragment()
            val bundle = Bundle()
            bundle.putString(EXTRA_ACCOUNTS_ARRAY, accountsArray)
            fragment.arguments = bundle
            return fragment
        }
    }
}