package com.unionbankph.corporate.branch.presentation.transactiondetail

import android.os.Bundle
import android.view.LayoutInflater
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
import com.unionbankph.corporate.databinding.ActivityBranchTransactionDetailBinding


class BranchTransactionDetailActivity :
    BaseActivity<ActivityBranchTransactionDetailBinding, BranchTransactionDetailViewModel>() {

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
        setToolbarTitle(binding.viewToolbar.tvToolbar, formatString(R.string.title_cash_or_check_deposit))
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
        setToolbarTitle(binding.viewToolbar.tvToolbar, mapBranchType(branchTransactionForm.type))
        binding.textViewCheckAccountNumber.text = branchTransactionForm.accountNumber
        binding.textViewCheckNumber.text = branchTransactionForm.checkNumber
        binding.textViewCheckDate.text = viewUtil.getDateFormatByDateString(
            branchTransactionForm.checkDate,
            DateFormatEnum.DATE_FORMAT_ISO_DATE.value,
            DateFormatEnum.DATE_FORMAT_DATE.value
        )
        binding.textViewAmount.text = autoFormatUtil.formatWithTwoDecimalPlaces(
            branchTransactionForm.amount,
            branchTransactionForm.currency
        )
        binding.textViewRemarks.text = branchTransactionForm.remarks.notEmpty()
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
        binding.textViewCheckAccountNumberTitle.visibility(false)
        binding.textViewCheckAccountNumber.visibility(false)
        binding.viewBorderCheckAccountNumber.visibility(false)
        binding.textViewCheckNumberTitle.visibility(false)
        binding.textViewCheckNumber.visibility(false)
        binding.viewBorderCheckNumber.visibility(false)
        binding.textViewCheckDateTitle.visibility(false)
        binding.textViewCheckDate.visibility(false)
        binding.viewBorderCheckDate.visibility(false)
    }

    private fun initCheckDeposit(isOnUs: Boolean) {
        binding.textViewCheckAccountNumberTitle.visibility(isOnUs)
        binding.textViewCheckAccountNumber.visibility(isOnUs)
        binding.viewBorderCheckAccountNumber.visibility(isOnUs)
        binding.textViewCheckNumberTitle.visibility(true)
        binding.textViewCheckNumber.visibility(true)
        binding.viewBorderCheckNumber.visibility(true)
        binding.textViewCheckDateTitle.visibility(isOnUs)
        binding.textViewCheckDate.visibility(isOnUs)
        binding.viewBorderCheckDate.visibility(isOnUs)
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

    override val viewModelClassType: Class<BranchTransactionDetailViewModel>
        get() = BranchTransactionDetailViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityBranchTransactionDetailBinding
        get() = ActivityBranchTransactionDetailBinding::inflate

}
