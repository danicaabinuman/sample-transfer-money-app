package com.unionbankph.corporate.branch.presentation.confirmation

import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.convertColorResourceToHex
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.notEmpty
import com.unionbankph.corporate.app.common.extension.toHtmlSpan
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.branch.data.model.BranchVisitSubmitDto
import com.unionbankph.corporate.branch.presentation.model.BranchTransactionForm
import com.unionbankph.corporate.branch.presentation.model.BranchVisitConfirmationForm
import com.unionbankph.corporate.branch.presentation.summary.BranchVisitSummaryActivity
import com.unionbankph.corporate.common.presentation.constant.DateFormatEnum
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.ShowGeneralGetOrganizationName
import com.unionbankph.corporate.databinding.ActivityBranchVisitConfirmationBinding

class BranchVisitConfirmationActivity :
    BaseActivity<ActivityBranchVisitConfirmationBinding, BranchVisitConfirmationViewModel>() {

    private val branchTransactionsForm by lazyFast {
        JsonHelper.fromListJson<BranchTransactionForm>(intent.getStringExtra(EXTRA_TRANSACTION_LIST))
    }

    private val branchVisitConfirmationForm by lazyFast {
        JsonHelper.fromJson<BranchVisitConfirmationForm>(intent.getStringExtra(EXTRA_CONFIRMATION_FORM))
    }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initConfirmationDetails()
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initViewModel()
        initGeneralViewModel()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        binding.buttonEdit.setOnClickListener {
            onBackPressed()
        }
        binding.buttonSubmit.setOnClickListener {
            viewModel.submitBranchTransaction()
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
        viewModel.confirmationState.observe(this, Observer {
            when (it) {
                is ShowBranchVisitConfirmationLoading -> {
                    showProgressAlertDialog(BranchVisitConfirmationActivity::class.java.simpleName)
                }
                is ShowBranchVisitConfirmationDismissLoading -> {
                    dismissProgressAlertDialog()
                }
                is ShowBranchVisitConfirmationSubmitted -> {
                    navigateBranchVisitSummaryScreen(it.branchVisitSubmitDto)
                    eventBus.actionSyncEvent.emmit(
                        BaseEvent(ActionSyncEvent.ACTION_UPDATE_BRANCH_VISIT_LIST)
                    )
                }
                is ShowBranchVisitConfirmationError -> {
                    handleOnError(it.throwable)
                }
            }
        })
        viewModel.setBranchTransactionsForm(branchTransactionsForm)
        viewModel.setBranchVisitConfirmationForm(branchVisitConfirmationForm)
    }

    private fun initGeneralViewModel() {
        generalViewModel.state.observe(this, Observer {
            when (it) {
                is ShowGeneralGetOrganizationName -> {
                    setToolbarTitle(
                        binding.viewToolbar.textViewTitle,
                        binding.viewToolbar.textViewCorporationName,
                        formatString(R.string.title_new_branch_transaction),
                        it.orgName
                    )
                }
            }
        })
        generalViewModel.getOrgName()
    }

    private fun initConfirmationDetails() {
        binding.textViewTransactionDate.text = viewUtil.getDateFormatByDateString(
            branchVisitConfirmationForm.transactionDate,
            DateFormatEnum.DATE_FORMAT_ISO_DATE.value,
            DateFormatEnum.DATE_FORMAT_DATE.value
        )
        binding.textViewCheckBranchName.text = branchVisitConfirmationForm.branchName
        binding.textViewCheckBranchAddress.text = branchVisitConfirmationForm.branchAddress
        binding.textViewDepositTo.text = branchVisitConfirmationForm.depositTo

        binding.textViewTotalDepositsTitle.visibility(branchVisitConfirmationForm.isBatch)
        binding.textViewTotalDeposits.visibility(branchVisitConfirmationForm.isBatch)
        binding.viewBorderTotalDeposits.visibility(branchVisitConfirmationForm.isBatch)

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
        binding.textViewTotalDeposits.text = stringBuffer.toString().toHtmlSpan()
        binding.textViewAmountTitle.text = formatString(
            if (branchVisitConfirmationForm.isBatch)
                R.string.title_total_amount
            else
                R.string.title_amount
        )
        binding.textViewAmount.text = autoFormatUtil.formatWithTwoDecimalPlaces(
            branchVisitConfirmationForm.totalAmount.toString(),
            branchVisitConfirmationForm.currency
        )
        binding.textViewServiceFee.text = if (branchVisitConfirmationForm.serviceFee != null) {
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
        binding.textViewChannel.text = branchVisitConfirmationForm.channel
        binding.textViewRemarks.text = branchVisitConfirmationForm.remarks.notEmpty()
    }

    private fun navigateBranchVisitSummaryScreen(branchVisitSubmitDto: BranchVisitSubmitDto) {
        navigator.navigate(
            this,
            BranchVisitSummaryActivity::class.java,
            Bundle().apply {
                putString(
                    BranchVisitSummaryActivity.EXTRA_TRANSACTION_LIST,
                    intent.getStringExtra(EXTRA_TRANSACTION_LIST)
                )
                putString(
                    BranchVisitSummaryActivity.EXTRA_CONFIRMATION_FORM,
                    intent.getStringExtra(EXTRA_CONFIRMATION_FORM)
                )
                putString(
                    BranchVisitSummaryActivity.EXTRA_BRANCH_VISIT_SUBMIT_DTO,
                    JsonHelper.toJson(branchVisitSubmitDto)
                )
            },
            isClear = true,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    companion object {
        const val EXTRA_TRANSACTION_LIST = "transaction_list"
        const val EXTRA_CONFIRMATION_FORM = "confirmation_form"
    }

    override val layoutId: Int
        get() = R.layout.activity_branch_visit_confirmation

    override val viewModelClassType: Class<BranchVisitConfirmationViewModel>
        get() = BranchVisitConfirmationViewModel::class.java
}
