package com.unionbankph.corporate.branch.presentation.transactiondetail

import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.notEmpty
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.branch.presentation.constant.BranchVisitTypeEnum
import com.unionbankph.corporate.branch.presentation.model.BranchTransactionForm
import com.unionbankph.corporate.common.presentation.constant.DateFormatEnum
import kotlinx.android.synthetic.main.activity_branch_transaction_detail.*
import kotlinx.android.synthetic.main.widget_transparent_appbar.*


class BranchTransactionDetailActivity :
    BaseActivity<BranchTransactionDetailViewModel>(R.layout.activity_branch_transaction_detail) {

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
        setToolbarTitle(tvToolbar, formatString(R.string.title_cash_or_check_deposit))
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initViewModel()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initViewModel() {
        viewModel =
            ViewModelProviders.of(
                this,
                viewModelFactory
            )[BranchTransactionDetailViewModel::class.java]
        viewModel.branchTransactionDetailLiveData.observe(this, Observer {
            when (it) {
                is ShowBranchTransactionDetailLoading -> {

                }
                is ShowBranchTransactionDetailDismissLoading -> {

                }
                is ShowBranchTransactionDetailError -> {
                    handleOnError(it.throwable)
                }
            }
        })
        viewModel.branchTransactionFormLiveData.observe(this, Observer {
            initBranchTransactionDetail(it)
        })
        viewModel.setBranchTransactionForm(
            intent.getStringExtra(EXTRA_BRANCH_TRANSACTION_DETAIL).notNullable()
        )
    }

    private fun initBranchTransactionDetail(branchTransactionForm: BranchTransactionForm) {
        setToolbarTitle(tvToolbar, mapBranchType(branchTransactionForm.type))
        textViewCheckAccountNumber.text = branchTransactionForm.accountNumber
        textViewCheckNumber.text = branchTransactionForm.checkNumber
        textViewCheckDate.text = viewUtil.getDateFormatByDateString(
            branchTransactionForm.checkDate,
            DateFormatEnum.DATE_FORMAT_ISO_DATE.value,
            DateFormatEnum.DATE_FORMAT_DATE.value
        )
        textViewAmount.text = autoFormatUtil.formatWithTwoDecimalPlaces(
            branchTransactionForm.amount,
            branchTransactionForm.currency
        )
        textViewRemarks.text = branchTransactionForm.remarks.notEmpty()
        when (branchTransactionForm.type) {
            BranchVisitTypeEnum.CASH_DEPOSIT.value,
            BranchVisitTypeEnum.CASH_WITHDRAW.value -> {
                initCashDeposit()
            }
            BranchVisitTypeEnum.CHECK_DEPOSIT_ON_US.value -> {
                initCheckDeposit(true)
            }
            BranchVisitTypeEnum.CHECK_DEPOSIT_OFF_US.value -> {
                initCheckDeposit(false)
            }
        }
    }

    private fun initCashDeposit() {
        textViewCheckAccountNumberTitle.visibility(false)
        textViewCheckAccountNumber.visibility(false)
        viewBorderCheckAccountNumber.visibility(false)
        textViewCheckNumberTitle.visibility(false)
        textViewCheckNumber.visibility(false)
        viewBorderCheckNumber.visibility(false)
        textViewCheckDateTitle.visibility(false)
        textViewCheckDate.visibility(false)
        viewBorderCheckDate.visibility(false)
    }

    private fun initCheckDeposit(isOnUs: Boolean) {
        textViewCheckAccountNumberTitle.visibility(isOnUs)
        textViewCheckAccountNumber.visibility(isOnUs)
        viewBorderCheckAccountNumber.visibility(isOnUs)
        textViewCheckNumberTitle.visibility(true)
        textViewCheckNumber.visibility(true)
        viewBorderCheckNumber.visibility(true)
        textViewCheckDateTitle.visibility(isOnUs)
        textViewCheckDate.visibility(isOnUs)
        viewBorderCheckDate.visibility(isOnUs)
    }

    private fun mapBranchType(type: String?): String {
        return context.formatString(
            when (type) {
                BranchVisitTypeEnum.CASH_WITHDRAW.value -> {
                    R.string.title_cash_withdrawal
                }
                BranchVisitTypeEnum.CASH_DEPOSIT.value -> {
                    R.string.title_cash_deposit
                }
                BranchVisitTypeEnum.CHECK_DEPOSIT_ON_US.value -> {
                    R.string.title_check_deposit_on_us
                }
                BranchVisitTypeEnum.CHECK_DEPOSIT_OFF_US.value -> {
                    R.string.title_check_deposit_off_us
                }
                else -> {
                    R.string.title_unknown
                }
            }
        )
    }

    companion object {
        const val EXTRA_BRANCH_TRANSACTION_DETAIL = "branch_transaction_detail"
    }

}
