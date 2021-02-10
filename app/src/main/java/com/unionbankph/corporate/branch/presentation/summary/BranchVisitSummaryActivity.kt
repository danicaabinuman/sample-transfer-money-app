package com.unionbankph.corporate.branch.presentation.summary

import android.os.Bundle
import android.text.Spanned
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.convertColorResourceToHex
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.notEmpty
import com.unionbankph.corporate.app.common.extension.setContextCompatBackground
import com.unionbankph.corporate.app.common.extension.setContextCompatTextColor
import com.unionbankph.corporate.app.common.extension.toHtmlSpan
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.branch.data.model.BranchVisitSubmitDto
import com.unionbankph.corporate.branch.presentation.channel.BranchVisitChannelActivity
import com.unionbankph.corporate.branch.presentation.list.BranchVisitActivity
import com.unionbankph.corporate.branch.presentation.model.BranchVisitConfirmationForm
import com.unionbankph.corporate.common.presentation.constant.DateFormatEnum
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.ShowGeneralGetOrganizationName
import kotlinx.android.synthetic.main.activity_branch_visit_summary.*
import kotlinx.android.synthetic.main.widget_transparent_org_appbar.*

class BranchVisitSummaryActivity :
    BaseActivity<BranchVisitSummaryViewModel>(R.layout.activity_branch_visit_summary) {

    private val branchVisitConfirmationForm by lazyFast {
        JsonHelper.fromJson<BranchVisitConfirmationForm>(
            intent.getStringExtra(EXTRA_CONFIRMATION_FORM)
        )
    }

    private val branchVisitSubmitDto by lazyFast {
        JsonHelper.fromJson<BranchVisitSubmitDto>(
            intent.getStringExtra(EXTRA_BRANCH_VISIT_SUBMIT_DTO)
        )
    }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initSummaryDetails()
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initViewModel()
        initGeneralViewModel()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        buttonMakeAnotherBranchTransaction.setOnClickListener {
            navigator.navigateClearUpStack(
                this,
                BranchVisitChannelActivity::class.java,
                null,
                isClear = true,
                isAnimated = true
            )
        }
        buttonViewBranchTransaction.setOnClickListener {
            navigator.navigateClearUpStack(
                this,
                BranchVisitActivity::class.java,
                null,
                isClear = true,
                isAnimated = true
            )
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

    private fun initViewModel() {
        viewModel =
            ViewModelProviders.of(
                this,
                viewModelFactory
            )[BranchVisitSummaryViewModel::class.java]
        viewModel.summaryState.observe(this, Observer {
            when (it) {
                is ShowBranchVisitSummaryLoading -> {
                    showProgressAlertDialog(BranchVisitSummaryActivity::class.java.simpleName)
                }
                is ShowBranchVisitSummaryDismissLoading -> {
                    dismissProgressAlertDialog()
                }
                is ShowBranchVisitSummaryError -> {
                    handleOnError(it.throwable)
                }
            }
        })
    }

    private fun initGeneralViewModel() {
        generalViewModel.state.observe(this, Observer {
            when (it) {
                is ShowGeneralGetOrganizationName -> {
                    setToolbarTitle(
                        textViewTitle,
                        textViewCorporationName,
                        formatString(R.string.title_cash_or_check_deposit),
                        it.orgName
                    )
                }
            }
        })
        generalViewModel.getOrgName()
    }

    private fun initSummaryDetails() {
        initTransactionStatus(
            R.drawable.bg_rectangle_green,
            R.drawable.ic_fund_transfer_check_green,
            R.string.title_branch_visit_success,
            R.color.colorApprovedStatus,
            formatString(
                R.string.msg_branch_visit_success_summary,
                branchVisitConfirmationForm.branchName,
                branchVisitSubmitDto.referenceId
            ).toHtmlSpan()
        )
        textViewTransactionDate.text = viewUtil.getDateFormatByDateString(
            branchVisitConfirmationForm.transactionDate,
            DateFormatEnum.DATE_FORMAT_ISO_DATE.value,
            DateFormatEnum.DATE_FORMAT_DATE.value
        )
        textViewCheckBranchName.text = branchVisitConfirmationForm.branchName
        textViewCheckBranchAddress.text = branchVisitConfirmationForm.branchAddress
        textViewDepositTo.text = branchVisitConfirmationForm.depositTo

        textViewTotalDepositsTitle.visibility(branchVisitConfirmationForm.isBatch)
        textViewTotalDeposits.visibility(branchVisitConfirmationForm.isBatch)
        viewBorderTotalDeposits.visibility(branchVisitConfirmationForm.isBatch)

        val cashDepositTitle = formatString(
            if (branchVisitConfirmationForm.cashDepositSize > 1)
                R.string.title_cash_deposits
            else
                R.string.title_cash_deposit
        )
        val checkDepositOnUsTitle = formatString(
            if (branchVisitConfirmationForm.checkDepositOnSize > 1)
                R.string.title_on_us_check_deposits
            else
                R.string.title_on_us_check_deposit
        )
        val checkDepositOffUsTitle = formatString(
            if (branchVisitConfirmationForm.checkDepositOffSize > 1)
                R.string.title_off_us_check_deposits
            else
                R.string.title_off_us_check_deposit
        )
        val totalDepositHeaderText =
            "${branchVisitConfirmationForm.numberOfTransactions} ${formatString(R.string.title_transactions)}<br><br>"
        val cashDepositText = if (branchVisitConfirmationForm.cashDepositSize > 0) {
            "${branchVisitConfirmationForm.cashDepositSize} $cashDepositTitle<br>" +
                    "${formatString(
                        R.string.param_color,
                        convertColorResourceToHex(R.color.colorSubText),
                        autoFormatUtil.formatWithTwoDecimalPlaces(
                            branchVisitConfirmationForm.totalAmountCashDeposit.toString(),
                            branchVisitConfirmationForm.currency
                        )
                    )}<br><br>"
        } else ""
        val checkDepositOnUsText = if (branchVisitConfirmationForm.checkDepositOnSize > 0) {
            "${branchVisitConfirmationForm.checkDepositOnSize} $checkDepositOnUsTitle<br>" +
                    "${formatString(
                        R.string.param_color,
                        convertColorResourceToHex(R.color.colorSubText),
                        autoFormatUtil.formatWithTwoDecimalPlaces(
                            branchVisitConfirmationForm.totalAmountCheckDepositOn.toString(),
                            branchVisitConfirmationForm.currency
                        )
                    )}<br><br>"
        } else ""
        val checkDepositOffUsText = if (branchVisitConfirmationForm.checkDepositOffSize > 0) {
            "${branchVisitConfirmationForm.checkDepositOffSize} $checkDepositOffUsTitle<br>" +
                    "${formatString(
                        R.string.param_color,
                        convertColorResourceToHex(R.color.colorSubText),
                        autoFormatUtil.formatWithTwoDecimalPlaces(
                            branchVisitConfirmationForm.totalAmountCheckDepositOff.toString(),
                            branchVisitConfirmationForm.currency
                        )
                    )}<br><br>"
        } else ""
        val stringBuffer = StringBuffer()
        stringBuffer.append(totalDepositHeaderText)
        stringBuffer.append(cashDepositText)
        stringBuffer.append(checkDepositOnUsText)
        stringBuffer.append(checkDepositOffUsText)
        textViewTotalDeposits.text = stringBuffer.toString().toHtmlSpan()
        textViewAmountTitle.text = formatString(
            if (branchVisitConfirmationForm.isBatch)
                R.string.title_total_amount
            else
                R.string.title_amount
        )
        textViewAmount.text = autoFormatUtil.formatWithTwoDecimalPlaces(
            branchVisitConfirmationForm.totalAmount.toString(),
            branchVisitConfirmationForm.currency
        )
        textViewServiceFee.text = if (branchVisitConfirmationForm.serviceFee != null) {
            formatString(
                R.string.value_service,
                autoFormatUtil.formatWithTwoDecimalPlaces(
                    branchVisitConfirmationForm.serviceFee?.value,
                    branchVisitConfirmationForm.serviceFee?.currency
                )
            )
        } else {
            formatString(R.string.value_service_fee_free)
        }
        textViewChannel.text = branchVisitConfirmationForm.channel
        textViewRemarks.text = branchVisitConfirmationForm.remarks.notEmpty()
    }

    private fun initTransactionStatus(
        background: Int,
        icon: Int,
        header: Int,
        headerColor: Int,
        headerMsg: Spanned
    ) {
        viewHeader.setContextCompatBackground(background)
        imageViewHeader.setImageResource(icon)
        textViewHeader.text = formatString(header)
        textViewHeader.setContextCompatTextColor(headerColor)
        textViewMsg.text = headerMsg
    }

    companion object {
        const val EXTRA_TRANSACTION_LIST = "transaction_list"
        const val EXTRA_CONFIRMATION_FORM = "confirmation_form"
        const val EXTRA_BRANCH_VISIT_SUBMIT_DTO = "branch_visit_submit_dto"
    }
}
