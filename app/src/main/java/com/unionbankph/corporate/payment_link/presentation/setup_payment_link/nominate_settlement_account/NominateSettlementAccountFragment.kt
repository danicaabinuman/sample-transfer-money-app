package com.unionbankph.corporate.payment_link.presentation.setup_payment_link.nominate_settlement_account

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.app.base.BaseBottomSheetDialog
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.databinding.FragmentNominateSettlementAccountBinding

class NominateSettlementAccountFragment :
    BaseBottomSheetDialog<FragmentNominateSettlementAccountBinding, NominateSettlementViewModel>() {

    private var callback: OnNominateSettlementAccountListener? = null

    override fun onViewsBound() {
        super.onViewsBound()
        binding.buttonCloseDialog.setOnClickListener { this.dismiss() }
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

    private fun updateRecyclerView(accounts: MutableList<Account>) {
        val accountsAdapter = NominateSettlementAccountsAdapter(accounts)
        binding.rvBSNominateSettlementAccounts.adapter = accountsAdapter
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

    override val bindingBinder: (View) -> FragmentNominateSettlementAccountBinding
        get() = FragmentNominateSettlementAccountBinding::bind

    override val layoutId: Int
        get() = R.layout.fragment_nominate_settlement_account

    override val viewModelClassType: Class<NominateSettlementViewModel>
        get() = NominateSettlementViewModel::class.java
}