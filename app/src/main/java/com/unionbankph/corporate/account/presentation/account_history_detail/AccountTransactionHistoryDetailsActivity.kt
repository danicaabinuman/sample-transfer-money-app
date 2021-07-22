package com.unionbankph.corporate.account.presentation.account_history_detail

import android.Manifest
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.tbruyelle.rxpermissions2.RxPermissions
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.data.model.AccountTransactionHistoryDetails
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.convertDateToDesireFormat
import com.unionbankph.corporate.app.common.extension.formatAccountNumber
import com.unionbankph.corporate.app.common.extension.formatAmount
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.isValidDateFormat
import com.unionbankph.corporate.app.common.extension.notEmpty
import com.unionbankph.corporate.app.common.extension.startShareMediaActivity
import com.unionbankph.corporate.app.common.extension.toHtmlSpan
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.approval.presentation.approval_detail.ApprovalDetailActivity
import com.unionbankph.corporate.common.presentation.constant.DateFormatEnum
import com.unionbankph.corporate.common.presentation.helper.ConstantHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.databinding.ActivityAccountTransactionHistoryDetailsBinding
import io.reactivex.rxkotlin.addTo


class AccountTransactionHistoryDetailsActivity :
    BaseActivity<ActivityAccountTransactionHistoryDetailsBinding ,AccountTransactionHistoryDetailsViewModel>() {

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
        setToolbarTitle(binding.viewToolbar.tvToolbar, getString(R.string.title_transaction_details))
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()

        viewModel.uiState.observe(this, EventObserver {
            when (it) {
                is UiState.Loading -> {
                    binding.viewLoadingState.progressBar.isVisible = true
                    binding.scrollView.isVisible = false
                }
                is UiState.Complete -> {
                    binding.viewLoadingState.progressBar.isVisible = false
                }
                is UiState.Error -> {
                    handleOnError(it.throwable)
                }
            }
        })
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initViews()
        setupInputs()
        setupOutputs()
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

    private fun setupInputs() {
        viewModel.initBundleData(
            intent.getStringExtra(EXTRA_RECORD),
            intent.getStringExtra(EXTRA_ID)
        )
    }

    private fun setupOutputs() {
        viewModel.accountTransactionHistoryDetails.observe(this, Observer {
            setupViews(it)
        })
        viewModel.navigateViewTransaction.observe(this, EventObserver {
            navigateViewTransaction(it)
        })
    }

    private fun setupViews(accountDetails: AccountTransactionHistoryDetails) {
        binding.scrollView.isVisible = true
        binding.textViewTransRefNo.text = accountDetails.referenceNumber.notEmpty()
        binding.textViewTransDate.text =
            accountDetails.transactionDate.convertDateToDesireFormat(DateFormatEnum.DATE_FORMAT_DEFAULT)
        binding.tvAccount.text = formatString(
            R.string.params_account_detail,
            accountDetails.account?.name,
            accountDetails.account?.accountNumber.formatAccountNumber(),
            accountDetails.account?.productCodeDesc
        ).toHtmlSpan()
        binding.tvPostedDate.text =
            accountDetails.postingDate.convertDateToDesireFormat(DateFormatEnum.DATE_FORMAT_DEFAULT)
        binding.tvDescription.text = accountDetails.description.notEmpty()
        binding.tvCheckNumber.text = accountDetails.checkNumber.notEmpty()
        binding.tvAmount.text =
            accountDetails.amount?.value.toString().formatAmount(accountDetails.amount?.currency)
        binding.tvEndingBalance.text =
            viewModel.record.value?.endingBalance.formatAmount(viewModel.record.value?.balanceCurrency)
        binding.ivTransferType.setImageResource(
            ConstantHelper.Drawable.getAccountTransactionType(
                accountDetails.transactionClass
            )
        )
        binding.llFields.removeAllViews()
        accountDetails.billsPayment?.also { billsPayment ->
            binding.tvBillerName.text = billsPayment.billerName
            binding.tvBillsPaymentHeader.isVisible = true
            binding.cvBillsPayment.isVisible = true
            billsPayment.references?.forEachIndexed { index, reference ->
                val viewFields = layoutInflater.inflate(R.layout.item_textview_biller, null)
                val textViewTitle = viewFields.findViewById<TextView>(R.id.textViewTitle)
                val textView = viewFields.findViewById<TextView>(R.id.tvReferenceNo)
                textViewTitle.text = reference.referenceName
                textView.text = reference.referenceValue
                if (reference.referenceValue.isValidDateFormat(DateFormatEnum.DATE_FORMAT_DATE_SLASH)) {
                    textView.text = reference.referenceValue.convertDateToDesireFormat(
                        DateFormatEnum.DATE_FORMAT_DATE
                    )
                } else {
                    textView.text = reference.referenceValue.notEmpty()
                }
                binding.llFields.addView(viewFields)
            }
        }
        binding.tvCheckNumberTitle.isVisible = accountDetails.checkNumber != null
        binding.tvCheckNumber.isVisible = accountDetails.checkNumber != null
        binding.borderCheckNumber.isVisible = accountDetails.checkNumber != null
        binding.btnViewTransaction.isVisible = accountDetails.portalTransaction != null &&
                "Debit".equals(accountDetails.transactionClass?.description, true)

        setupShareDetails(accountDetails)
        binding.btnViewTransaction.setOnClickListener {
            viewModel.onClickedViewTransaction()
        }
    }

    private fun setupShareDetails(accountDetails: AccountTransactionHistoryDetails) {
        binding.viewShareDetails.viewHeaderTransaction.textViewQRCodeDesc.isVisible = false
        binding.viewShareDetails.viewHeaderTransaction.imageViewQRCode.isVisible = false
        binding.viewShareDetails.tvShareReferenceNumber.text = accountDetails.referenceNumber.notEmpty()
        binding.viewShareDetails.tvShareAccount.text = formatString(
            R.string.params_account_detail,
            accountDetails.account?.name,
            accountDetails.account?.accountNumber.formatAccountNumber(),
            accountDetails.account?.productCodeDesc
        ).toHtmlSpan()
        binding.viewShareDetails.tvSharePostedDate.text =
            accountDetails.postingDate.convertDateToDesireFormat(DateFormatEnum.DATE_FORMAT_DEFAULT)
        binding.viewShareDetails.tvShareTransactionDate.text =
            accountDetails.transactionDate.convertDateToDesireFormat(DateFormatEnum.DATE_FORMAT_DEFAULT)
        binding.viewShareDetails.tvShareCheckNumberTitle.isVisible = accountDetails.checkNumber != null
        binding.viewShareDetails.tvShareCheckNumber.isVisible = accountDetails.checkNumber != null
        binding.viewShareDetails.tvShareCheckNumber.text = accountDetails.checkNumber.notEmpty()
        binding.viewShareDetails.tvShareDescription.text = accountDetails.description.notEmpty()
        binding.viewShareDetails.tvShareAmount.text =
            accountDetails.amount?.value.toString().formatAmount(accountDetails.amount?.currency)
        binding.viewShareDetails.tvShareEndingBalance.text =
            viewModel.record.value?.endingBalance.formatAmount(viewModel.record.value?.balanceCurrency)
        binding.viewShareDetails.ivShareTransferType.setImageResource(
            ConstantHelper.Drawable.getAccountTransactionType(
                accountDetails.transactionClass
            )
        )
        accountDetails.billsPayment?.also { billsPayment ->
            binding.viewShareDetails.tvShareRemittanceDetails.isVisible = true
            binding.viewShareDetails.tvShareFields.isVisible = true
            binding.viewShareDetails.borderRemittanceDetails.isVisible = true
            val stringBuilder = StringBuilder()
            billsPayment.references?.forEachIndexed { index, reference ->
                if (reference.referenceValue.isValidDateFormat(DateFormatEnum.DATE_FORMAT_DATE_SLASH)) {
                    stringBuilder.append(
                        reference.referenceValue.convertDateToDesireFormat(DateFormatEnum.DATE_FORMAT_DATE)
                    )
                } else {
                    stringBuilder.append(reference.referenceValue.notEmpty())
                }

                stringBuilder.append("<br>")
                stringBuilder.append(
                    String.format(
                        getString(R.string.format_change_color),
                        reference.referenceName
                    )
                )
                if (index != billsPayment.references?.size?.minus(1)) {
                    stringBuilder.append("<br>")
                    stringBuilder.append("<br>")
                }
            }

            binding.viewShareDetails.tvShareFields.text = (
                    billsPayment.billerName +
                            "<br>" +
                            formatString(
                                R.string.format_change_color,
                                formatString(R.string.title_biller_name)
                            ) +
                            "<br>" +
                            "<br>" +
                            stringBuilder.toString()
                    ).toHtmlSpan()
        }
        binding.viewShareDetails.tvShareDateDownloaded.text = viewUtil.getCurrentDateString()
    }

    private fun initViews() {
        binding.viewShareButton.buttonShare.setOnClickListener {
            initPermission()
        }
    }

    private fun initPermission() {
        RxPermissions(this)
            .request(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            .subscribe { granted ->
                if (granted) {
                    showProgressAlertDialog(this::class.java.simpleName)
                    binding.svShare.isVisible = true
                    Handler().postDelayed(
                        {
                            val shareBitmap = viewUtil.getBitmapByView(
                                binding.viewShareDetails.viewShareAccountTransaction
                            )
                            binding.svShare.isVisible = false
                            dismissProgressAlertDialog()
                            startShareMediaActivity(shareBitmap)
                        }, resources.getInteger(R.integer.time_delay_share_media).toLong()
                    )
                } else {
                    MaterialDialog(this).show {
                        lifecycleOwner(this@AccountTransactionHistoryDetailsActivity)
                        cancelOnTouchOutside(false)
                        message(R.string.desc_service_permission)
                        positiveButton(
                            res = R.string.action_ok,
                            click = {
                                it.dismiss()
                                initPermission()
                            }
                        )
                        negativeButton(
                            res = R.string.action_cancel,
                            click = {
                                it.dismiss()
                                initPermission()
                            }
                        )
                    }
                }
            }.addTo(disposables)
    }

    private fun navigateViewTransaction(transaction: String) {
        val bundle = Bundle().apply {
            putString(ApprovalDetailActivity.EXTRA_TRANSACTION_DETAIL, transaction)
        }
        navigator.navigate(
            this,
            ApprovalDetailActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_UP
        )
    }

    companion object {
        const val EXTRA_RECORD = "record"
        const val EXTRA_ID = "id"
    }

    override val viewModelClassType: Class<AccountTransactionHistoryDetailsViewModel>
        get() = AccountTransactionHistoryDetailsViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityAccountTransactionHistoryDetailsBinding
        get() = ActivityAccountTransactionHistoryDetailsBinding::inflate

}
