package com.unionbankph.corporate.dao.presentation.online_banking_products

import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.dao.presentation.DaoActivity
import kotlinx.android.synthetic.main.fragment_dao_online_banking_products.*

class DaoOnlineBankingProductsFragment :
    BaseFragment<DaoOnlineBankingProductsViewModel>(R.layout.fragment_dao_online_banking_products),
    DaoActivity.ActionEvent {

    private val daoActivity by lazyFast { getAppCompatActivity() as DaoActivity }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initDaoActivity()
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel =
            ViewModelProviders.of(
                this,
                viewModelFactory
            )[DaoOnlineBankingProductsViewModel::class.java]
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
        cb_electronic_fund_transfer.setOnCheckedChangeListener { _, isChecked ->
            viewModel.fundTransferInput.onNext(isChecked)
        }
        cb_bills_payment.setOnCheckedChangeListener { _, isChecked ->
            viewModel.billsPaymentInput.onNext(isChecked)
        }
        cb_business_check.setOnCheckedChangeListener { _, isChecked ->
            viewModel.businessCheckInput.onNext(isChecked)
        }
        cb_government_payments.setOnCheckedChangeListener { _, isChecked ->
            viewModel.governmentPaymentsCheckInput.onNext(isChecked)
        }
        cb_branch_transaction.setOnCheckedChangeListener { _, isChecked ->
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
}
