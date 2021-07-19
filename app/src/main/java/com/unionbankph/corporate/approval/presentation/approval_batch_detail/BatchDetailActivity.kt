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
import com.unionbankph.corporate.databinding.ActivityBatchDetailBinding
import com.unionbankph.corporate.fund_transfer.data.model.Batch


class BatchDetailActivity :
    BaseActivity<ActivityBatchDetailBinding, BatchTransferViewModel>() {

    private val batch by lazyFast {
        JsonHelper.fromJson<Batch>(intent.getStringExtra(EXTRA_BATCH))
    }

    private val supportCWT by lazyFast { intent.getBooleanExtra(EXTRA_SUPPORT_CWT, false) }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
        setToolbarTitle(binding.viewToolbar.tvToolbar, getString(R.string.title_transfer_details))
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
            binding.textViewStatus.setContextCompatTextColor(ConstantHelper.Color.getTextColor(it.status))
            binding.textViewStatus.text = it.status?.description

            binding.textViewTransferTo.text =
                if (it.beneficiaryName != null)
                    formatString(
                        R.string.params_two_format,
                        it.beneficiaryName,
                        viewUtil.getAccountNumberFormat(it.receiverAccountNumber)
                    ).toHtmlSpan()
                else
                    viewUtil.getAccountNumberFormat(it.receiverAccountNumber)

            binding.textViewAmount.text =
                autoFormatUtil.formatWithTwoDecimalPlaces(batch.amount.toString(), batch.currency)

            if (0.00 >= it.serviceFee?.value?.toDouble() ?: 0.00 || it.serviceFee == null) {
                binding.textViewServiceFee.text = getString(R.string.value_service_fee_free)
            } else {
                binding.textViewServiceFee.text = formatString(
                    R.string.value_service,
                    autoFormatUtil.formatWithTwoDecimalPlaces(
                        it.serviceFee?.value,
                        it.serviceFee?.currency
                    )
                )
            }

            val hasBeneficiaryAddress = it.beneAddress != null
            binding.viewBorderBeneficiaryAddress.visibility(hasBeneficiaryAddress)
            binding.textViewBeneficiaryAddressTitle.visibility(hasBeneficiaryAddress)
            binding.textViewBeneficiaryAddress.visibility(hasBeneficiaryAddress)
            binding.textViewBeneficiaryAddress.text = it.beneAddress

            binding.textViewChannel.text = ConstantHelper.Text.getChannelByChannelId(it.channelId?.toInt())

            val hasBankCode = it.bankCode != null
            binding.viewBorderBankCode.visibility(hasBankCode)
            binding.textViewBankCodeTitle.visibility(hasBankCode)
            binding.textViewBankCode.visibility(hasBankCode)
            binding.textViewBankCode.text = it.bankCode

            val hasBankName = it.bankName != null
            binding.viewBorderBankName.visibility(hasBankName)
            binding.textViewBankNameTitle.visibility(hasBankName)
            binding.textViewBankName.visibility(hasBankName)
            binding.textViewBankName.text = it.bankName

            val hasPurpose = it.purpose != null
            binding.viewBorderPurpose.visibility(hasPurpose)
            binding.textViewPurposeTitle.visibility(hasPurpose)
            binding.textViewPurpose.visibility(hasPurpose)
            binding.textViewPurpose.text = it.purpose

            val hasORReleaseBranch = it.orReleasingBranch != null
            binding.viewBorderORReleaseBranch.visibility(hasORReleaseBranch)
            binding.textViewORReleaseBranchTitle.visibility(hasORReleaseBranch)
            binding.textViewORReleaseBranch.visibility(hasORReleaseBranch)
            binding.textViewORReleaseBranch.text = it.orReleasingBranch

            val hasRemarks = it.remarks != null
            binding.viewBorderRemarks.visibility(hasRemarks)
            binding.textViewRemarksTitle.visibility(hasRemarks)
            binding.textViewRemarks.visibility(hasRemarks)
            binding.textViewRemarks.text = it.remarks

            val buttonViewCWT = binding.viewButtonViewCWT.button
            val textViewViewCWT = binding.viewButtonViewCWT.textViewShare

            val buttonViewAdditionalInformation =
                binding.viewButtonViewAdditionalInformation.button
            val textViewViewAdditionalInformation =
                binding.viewButtonViewAdditionalInformation.textViewShare

            textViewViewCWT.text = formatString(R.string.action_view_cwt)
            textViewViewAdditionalInformation.text =
                formatString(R.string.action_view_additional_information)

            binding.viewButtonViewCWT.root.visibility(supportCWT)
            binding.viewButtonViewAdditionalInformation.root.visibility(supportCWT)

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

    override val layoutId: Int
        get() = R.layout.activity_batch_detail

    override val viewModelClassType: Class<BatchTransferViewModel>
        get() = BatchTransferViewModel::class.java

}
