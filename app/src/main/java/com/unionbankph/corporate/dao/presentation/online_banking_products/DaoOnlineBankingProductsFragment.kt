package com.unionbankph.corporate.dao.presentation.online_banking_products

import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.dao.presentation.DaoActivity
import com.unionbankph.corporate.databinding.FragmentDaoOnlineBankingProductsBinding

class DaoOnlineBankingProductsFragment :
    BaseFragment<FragmentDaoOnlineBankingProductsBinding, DaoOnlineBankingProductsViewModel>(),
    DaoActivity.ActionEvent {

    private val daoActivity by lazyFast { getAppCompatActivity() as DaoActivity }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initDaoActivity()
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
    }

    override fun onViewsBound() {
        super.onViewsBound()
        init()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initCheckListener()
    }

    private fun initCheckListener() {
        binding.cbElectronicFundTransfer.setOnCheckedChangeListener { _, isChecked ->
            viewModel.fundTransferInput.onNext(isChecked)
        }
        binding.cbBillsPayment.setOnCheckedChangeListener { _, isChecked ->
            viewModel.billsPaymentInput.onNext(isChecked)
        }
        binding.cbBusinessCheck.setOnCheckedChangeListener { _, isChecked ->
            viewModel.businessCheckInput.onNext(isChecked)
        }
        binding.cbGovernmentPayments.setOnCheckedChangeListener { _, isChecked ->
            viewModel.governmentPaymentsCheckInput.onNext(isChecked)
        }
        binding.cbBranchTransaction.setOnCheckedChangeListener { _, isChecked ->
            viewModel.branchTransactionCheckInput.onNext(isChecked)
        }
    }

    private fun init() {
    }

    private fun initDaoActivity() {
        daoActivity.setToolBarDesc(formatString(R.string.title_online_banking_products))
        daoActivity.setActionEvent(this)
        daoActivity.setProgressValue(12)
    }

    override fun onClickNext() {
    }

    override val layoutId: Int
        get() = R.layout.fragment_dao_online_banking_products

    override val viewModelClassType: Class<DaoOnlineBankingProductsViewModel>
        get() = DaoOnlineBankingProductsViewModel::class.java
}
