package com.unionbankph.corporate.approval.presentation.approval_batch_detail

import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.approval.presentation.approval_batch_list.BatchTransferViewModel
import com.unionbankph.corporate.approval.presentation.approval_cwt.BatchCWTActivity
import com.unionbankph.corporate.common.presentation.helper.ConstantHelper
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.fund_transfer.data.model.Batch
import kotlinx.android.synthetic.main.activity_batch_detail.*
import kotlinx.android.synthetic.main.widget_transparent_appbar.*


class BatchDetailActivity : BaseActivity<BatchTransferViewModel>(R.layout.activity_batch_detail) {

    private val batch by lazyFast {
        JsonHelper.fromJson<Batch>(intent.getStringExtra(EXTRA_BATCH))
    }

    private val supportCWT by lazyFast { intent.getBooleanExtra(EXTRA_SUPPORT_CWT, false) }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
        setToolbarTitle(tvToolbar, getString(R.string.title_transfer_details))
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initBatchDetailView()
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

    private fun initBatchDetailView() {
        batch.let {
            textViewStatus.setContextCompatTextColor(ConstantHelper.Color.getTextColor(it.status))
            textViewStatus.text = it.status?.description

            textViewTransferTo.text =
                if (it.beneficiaryName != null)
                    formatString(
                        R.string.params_two_format,
                        it.beneficiaryName,
                        viewUtil.getAccountNumberFormat(it.receiverAccountNumber)
                    ).toHtmlSpan()
                else
                    viewUtil.getAccountNumberFormat(it.receiverAccountNumber)

            textViewAmount.text =
                autoFormatUtil.formatWithTwoDecimalPlaces(batch.amount.toString(), batch.currency)

            if (0.00 >= it.serviceFee?.value?.toDouble() ?: 0.00 || it.serviceFee == null) {
                textViewServiceFee.text = getString(R.string.value_service_fee_free)
            } else {
                textViewServiceFee.text = formatString(
                    R.string.value_service,
                    autoFormatUtil.formatWithTwoDecimalPlaces(
                        it.serviceFee?.value,
                        it.serviceFee?.currency
                    )
                )
            }

            val hasBeneficiaryAddress = it.beneAddress != null
            viewBorderBeneficiaryAddress.visibility(hasBeneficiaryAddress)
            textViewBeneficiaryAddressTitle.visibility(hasBeneficiaryAddress)
            textViewBeneficiaryAddress.visibility(hasBeneficiaryAddress)
            textViewBeneficiaryAddress.text = it.beneAddress

            textViewChannel.text = ConstantHelper.Text.getChannelByChannelId(it.channelId?.toInt())

            val hasBankCode = it.bankCode != null
            viewBorderBankCode.visibility(hasBankCode)
            textViewBankCodeTitle.visibility(hasBankCode)
            textViewBankCode.visibility(hasBankCode)
            textViewBankCode.text = it.bankCode

            val hasBankName = it.bankName != null
            viewBorderBankName.visibility(hasBankName)
            textViewBankNameTitle.visibility(hasBankName)
            textViewBankName.visibility(hasBankName)
            textViewBankName.text = it.bankName

            val hasPurpose = it.purpose != null
            viewBorderPurpose.visibility(hasPurpose)
            textViewPurposeTitle.visibility(hasPurpose)
            textViewPurpose.visibility(hasPurpose)
            textViewPurpose.text = it.purpose

            val hasORReleaseBranch = it.orReleasingBranch != null
            viewBorderORReleaseBranch.visibility(hasORReleaseBranch)
            textViewORReleaseBranchTitle.visibility(hasORReleaseBranch)
            textViewORReleaseBranch.visibility(hasORReleaseBranch)
            textViewORReleaseBranch.text = it.orReleasingBranch

            val hasRemarks = it.remarks != null
            viewBorderRemarks.visibility(hasRemarks)
            textViewRemarksTitle.visibility(hasRemarks)
            textViewRemarks.visibility(hasRemarks)
            textViewRemarks.text = it.remarks

            val buttonViewCWT = viewButtonViewCWT.findViewById<Button>(R.id.button)
            val textViewViewCWT = viewButtonViewCWT.findViewById<TextView>(R.id.textViewShare)

            val buttonViewAdditionalInformation =
                viewButtonViewAdditionalInformation.findViewById<Button>(R.id.button)
            val textViewViewAdditionalInformation =
                viewButtonViewAdditionalInformation.findViewById<TextView>(R.id.textViewShare)

            textViewViewCWT.text = formatString(R.string.action_view_cwt)
            textViewViewAdditionalInformation.text =
                formatString(R.string.action_view_additional_information)

            viewButtonViewCWT.visibility(supportCWT)
            viewButtonViewAdditionalInformation.visibility(supportCWT)

            buttonViewCWT.setOnClickListener { _ ->
                val bundle = Bundle().apply {
                    putString(BatchCWTActivity.EXTRA_REFERENCE_ID, it.referenceId)
                    putString(BatchCWTActivity.EXTRA_TYPE, BATCH_TYPE_CWT)
                }
                navigateCWTScreen(bundle)
            }
            buttonViewAdditionalInformation.setOnClickListener { _ ->
                val bundle = Bundle().apply {
                    putString(BatchCWTActivity.EXTRA_REFERENCE_ID, it.referenceId)
                    putString(BatchCWTActivity.EXTRA_TYPE, BATCH_TYPE_INV)
                }
                navigateCWTScreen(bundle)
            }
        }

    }

    private fun navigateCWTScreen(bundle: Bundle) {
        navigator.navigate(
            this,
            BatchCWTActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_UP
        )
    }

    companion object {
        const val EXTRA_BATCH = "batch"
        const val EXTRA_SUPPORT_CWT = "support_cwt"
    }

}
