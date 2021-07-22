package com.unionbankph.corporate.branch.presentation.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.branch.presentation.constant.BranchVisitTypeEnum
import com.unionbankph.corporate.branch.presentation.model.BranchTransactionForm
import com.unionbankph.corporate.branch.presentation.model.BranchVisit
import com.unionbankph.corporate.branch.presentation.transactiondetail.BranchTransactionDetailActivity
import com.unionbankph.corporate.branch.presentation.transactionlist.BranchTransactionActivity
import com.unionbankph.corporate.common.presentation.constant.DateFormatEnum
import com.unionbankph.corporate.common.presentation.helper.ConstantHelper
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.databinding.ActivityBranchVisitDetailBinding

class BranchVisitDetailActivity :
    BaseActivity<ActivityBranchVisitDetailBinding, BranchVisitDetailViewModel>() {

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
        setToolbarTitle(binding.viewToolbar.tvToolbar, formatString(R.string.title_cash_or_check_deposit))
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
        binding.viewHeader.textViewTitle.text = formatString(R.string.title_no_approval_hierarchy)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initViewModel()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        binding.cardViewAll.root.setOnClickListener {
            navigateBranchTransactionScreen()
        }
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

    override fun onBackPressed() {
        onBackPressed(false)
        overridePendingTransition(R.anim.anim_no_change, R.anim.anim_slide_down)
    }

    private fun initViewModel() {
        viewModel.detailState.observe(this, Observer {
            when (it) {
                is ShowBranchVisitDetailLoading -> {
                    binding.viewLoadingState.root.visibility(true)
                    binding.constraintLayout.visibility(false)
                }
                is ShowBranchVisitDetailDismissLoading -> {
                    binding.viewLoadingState.root.visibility(false)
                }
                is ShowBranchVisitDetailError -> {
                    handleOnError(it.throwable)
                }
            }
        })
        viewModel.branchVisitsLiveData.observe(this, Observer {
            initBranchVisitDetails(it)
        })
        viewModel.branchTransactionsFormLiveData.observe(this, Observer {
            initBranchTransactionDetails(it)
        })
        getBranchVisit()
    }

    private fun initBranchVisitDetails(branchVisit: BranchVisit) {
        binding.constraintLayout.visibility(true)
        binding.textViewStatus.setContextCompatTextColor(
            ConstantHelper.Color.getTextColorBranchVisit(
                branchVisit.status
            )
        )
        binding.textViewStatus.text =
            ("<b>${branchVisit.status?.value}</b><br>Reference Number: " +
                    branchVisit.referenceNumber.notEmpty()).toHtmlSpan()
        binding.textViewCreatedDate.text = viewUtil.getDateFormatByDateString(
            branchVisit.createdDate,
            DateFormatEnum.DATE_FORMAT_ISO_WITHOUT_T.value,
            DateFormatEnum.DATE_FORMAT_DATE.value
        )
        binding.textViewCreatedBy.text = branchVisit.createdBy
        binding.textViewTransactionDate.text = viewUtil.getDateFormatByDateString(
            branchVisit.transactionDate,
            DateFormatEnum.DATE_FORMAT_ISO_DATE.value,
            DateFormatEnum.DATE_FORMAT_DATE.value
        )
        binding.textViewBranchName.text = branchVisit.branchName.notEmpty()
        binding.textViewBranchAddress.text = branchVisit.branchAddress.notEmpty()
        binding.textViewAccount.text = branchVisit.depositTo
        if (branchVisit.isBatch) {
            val cashDepositTitle = formatString(
                if (branchVisit.cashDepositSize > 1)
                    R.string.title_cash_deposits
                else
                    R.string.title_cash_deposit
            )
            val checkDepositOnUsTitle = formatString(
                if (branchVisit.checkDepositOnSize > 1)
                    R.string.title_on_us_check_deposits
                else
                    R.string.title_on_us_check_deposit
            )
            val checkDepositOffUsTitle = formatString(
                if (branchVisit.checkDepositOffSize > 1)
                    R.string.title_off_us_check_deposits
                else
                    R.string.title_off_us_check_deposit
            )
            val totalDepositHeaderText =
                "${branchVisit.numberOfTransactions} ${formatString(R.string.title_transactions)}<br><br>"
            val cashDepositText = if (branchVisit.cashDepositSize > 0) {
                "${branchVisit.cashDepositSize} $cashDepositTitle<br>" +
                        "${formatString(
                            R.string.param_color,
                            convertColorResourceToHex(R.color.colorSubText),
                            autoFormatUtil.formatWithTwoDecimalPlaces(
                                branchVisit.totalAmountCashDeposit.toString(),
                                branchVisit.currency
                            )
                        )}<br><br>"
            } else ""
            val checkDepositOnUsText = if (branchVisit.checkDepositOnSize > 0) {
                "${branchVisit.checkDepositOnSize} $checkDepositOnUsTitle<br>" +
                        "${formatString(
                            R.string.param_color,
                            convertColorResourceToHex(R.color.colorSubText),
                            autoFormatUtil.formatWithTwoDecimalPlaces(
                                branchVisit.totalAmountCheckDepositOn.toString(),
                                branchVisit.currency
                            )
                        )}<br><br>"
            } else ""
            val checkDepositOffUsText = if (branchVisit.checkDepositOffSize > 0) {
                "${branchVisit.checkDepositOffSize} $checkDepositOffUsTitle<br>" +
                        "${formatString(
                            R.string.param_color,
                            convertColorResourceToHex(R.color.colorSubText),
                            autoFormatUtil.formatWithTwoDecimalPlaces(
                                branchVisit.totalAmountCheckDepositOn.toString(),
                                branchVisit.currency
                            )
                        )}<br><br>"
            } else ""
            val stringBuffer = StringBuffer()
            stringBuffer.append(totalDepositHeaderText)
            stringBuffer.append(cashDepositText)
            stringBuffer.append(checkDepositOnUsText)
            stringBuffer.append(checkDepositOffUsText)
            stringBuffer.append(
                "Total ${autoFormatUtil.formatWithTwoDecimalPlaces(
                    branchVisit.amount,
                    branchVisit.currency
                )}"
            )
            binding.textViewTotalDepositAmount.text = stringBuffer.toString().toHtmlSpan()
        } else {
            binding.textViewTotalDepositAmount.text = autoFormatUtil.formatWithTwoDecimalPlaces(
                branchVisit.amount,
                branchVisit.currency
            )
        }

        binding.textViewServiceFee.text = if (branchVisit.serviceFee != null) {
            formatString(
                R.string.value_service,
                autoFormatUtil.formatWithTwoDecimalPlaces(
                    branchVisit.serviceFee?.value,
                    branchVisit.serviceFee?.currency
                )
            )
        } else {
            formatString(R.string.value_service_fee_free)
        }
        if (branchVisit.channel == formatString(R.string.title_cash_withdrawal)) {
            setToolbarTitle(binding.viewToolbar.tvToolbar, formatString(R.string.title_cash_withdrawal_details))
        } else {
            setToolbarTitle(binding.viewToolbar.tvToolbar, formatString(R.string.title_cash_deposit_details))
        }
        binding.textViewChannel.text = branchVisit.channel
        binding.textViewRemarks.text = branchVisit.remarks.notEmpty()
        binding.textViewReferenceNumber.text = branchVisit.referenceNumber.notEmpty()
    }

    private fun initBranchTransactionDetails(
        branchTransactionsForm: MutableList<BranchTransactionForm>
    ) {
        binding.textViewHeaderBranchTransactionDetails.visibility(branchTransactionsForm.size > 1)
        binding.cardViewBranchTransaction.visibility(branchTransactionsForm.size > 1)
        binding.cardViewAll.root.visibility(branchTransactionsForm.size > 3)
        binding.linearLayoutBranchTransaction.removeAllViews()
        branchTransactionsForm.take(3).forEachIndexed { index, branchTransactionForm ->
            val viewBranchTransactionItem =
                LayoutInflater.from(context)
                    .inflate(R.layout.item_branch_transaction, null)
            val textViewTypeOfCheck =
                viewBranchTransactionItem.findViewById<TextView>(R.id.textViewTypeOfCheck)
            val textViewAccountNumber =
                viewBranchTransactionItem.findViewById<TextView>(R.id.textViewAccountNumber)
            val textViewRemarks =
                viewBranchTransactionItem.findViewById<TextView>(R.id.textViewRemarks)
            val textViewAmount =
                viewBranchTransactionItem.findViewById<TextView>(R.id.textViewAmount)
            val viewBorderTop =
                viewBranchTransactionItem.findViewById<View>(R.id.viewBorderTop)
            val viewBorderBottom =
                viewBranchTransactionItem.findViewById<View>(R.id.viewBorderBottom)
            viewBorderTop.visibility(false)
            viewBorderBottom.visibility(index.plus(1) != branchTransactionsForm.take(3).size)
            textViewAccountNumber.visibility(
                branchTransactionForm.type != BranchVisitTypeEnum.CASH_DEPOSIT.value
            )
            textViewTypeOfCheck.text = mapBranchType(branchTransactionForm.type)
            textViewAccountNumber.text = branchTransactionForm.checkNumber
            textViewAmount.text = autoFormatUtil.formatWithTwoDecimalPlaces(
                branchTransactionForm.amount,
                branchTransactionForm.currency
            )
            textViewRemarks.text = branchTransactionForm.remarks ?: "--"
            viewBranchTransactionItem.setOnClickListener {
                navigationBranchTransactionDetail(branchTransactionForm)
            }
            binding.linearLayoutBranchTransaction.addView(viewBranchTransactionItem)
        }
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

    private fun getBranchVisit() {
        viewModel.getBranchVisit(intent.getStringExtra(EXTRA_ID).notNullable())
    }

    private fun navigateBranchTransactionScreen() {
        val bundle = Bundle().apply {
            putBoolean(BranchTransactionActivity.EXTRA_DISABLE_ACTION, true)
            putString(
                BranchTransactionActivity.EXTRA_LIST,
                JsonHelper.toJson(viewModel.branchTransactionsFormLiveData.value)
            )
        }
        navigator.navigate(
            this,
            BranchTransactionActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun navigationBranchTransactionDetail(branchTransactionForm: BranchTransactionForm) {
        val bundle = Bundle().apply {
            putString(
                BranchTransactionDetailActivity.EXTRA_BRANCH_TRANSACTION_DETAIL,
                JsonHelper.toJson(branchTransactionForm)
            )
        }
        navigator.navigate(
            this,
            BranchTransactionDetailActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    companion object {
        const val EXTRA_ID = "id"
    }

    override val viewModelClassType: Class<BranchVisitDetailViewModel>
        get() = BranchVisitDetailViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityBranchVisitDetailBinding
        get() = ActivityBranchVisitDetailBinding::inflate
}
