package com.unionbankph.corporate.account.presentation.account_history_detail

import android.Manifest
import android.os.Bundle
import android.os.Handler
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
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_account_transaction_history_details.*
import kotlinx.android.synthetic.main.view_account_transaction_share.*
import kotlinx.android.synthetic.main.widget_header_transaction_summary.*
import kotlinx.android.synthetic.main.widget_transparent_appbar.*


class AccountTransactionHistoryDetailsActivity :
    BaseActivity<AccountTransactionHistoryDetailsViewModel>(R.layout.activity_account_transaction_history_details) {

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
        setToolbarTitle(tvToolbar, getString(R.string.title_transaction_details))
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel = ViewModelProviders.of(
            this,
            viewModelFactory
        )[AccountTransactionHistoryDetailsViewModel::class.java]
        viewModel.uiState.observe(this, EventObserver {
            when (it) {
                is UiState.Loading -> {
                    viewLoadingState.isVisible = true
                    scroll_view.isVisible = false
                }
                is UiState.Complete -> {
                    viewLoadingState.isVisible = false
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
        scroll_view.isVisible = true
        text_view_trans_ref_no.text = accountDetails.referenceNumber.notEmpty()
        text_view_trans_date.text =
            accountDetails.transactionDate.convertDateToDesireFormat(DateFormatEnum.DATE_FORMAT_DEFAULT)
        tv_account.text = formatString(
            R.string.params_account_detail,
            accountDetails.account?.name,
            accountDetails.account?.accountNumber.formatAccountNumber(),
            accountDetails.account?.productCodeDesc
        ).toHtmlSpan()
        tv_posted_date.text =
            accountDetails.postingDate.convertDateToDesireFormat(DateFormatEnum.DATE_FORMAT_DEFAULT)
        tv_description.text = accountDetails.description.notEmpty()
        tv_check_number.text = accountDetails.checkNumber.notEmpty()
        tv_amount.text =
            accountDetails.amount?.value.toString().formatAmount(accountDetails.amount?.currency)
        tv_ending_balance.text =
            viewModel.record.value?.endingBalance.formatAmount(viewModel.record.value?.balanceCurrency)
        iv_transfer_type.setImageResource(
            ConstantHelper.Drawable.getAccountTransactionType(
                accountDetails.transactionClass
            )
        )
        ll_fields.removeAllViews()
        accountDetails.billsPayment?.also { billsPayment ->
            tv_biller_name.text = billsPayment.billerName
            tv_bills_payment_header.isVisible = true
            cv_bills_payment.isVisible = true
            billsPayment.references?.forEachIndexed { index, reference ->
                val viewFields = layoutInflater.inflate(R.layout.item_textview_biller, null)
                val textViewTitle = viewFields.findViewById<TextView>(R.id.textViewTitle)
                val textView = viewFields.findViewById<TextView>(R.id.textView)
                textViewTitle.text = reference.referenceName
                textView.text = reference.referenceValue
                if (reference.referenceValue.isValidDateFormat(DateFormatEnum.DATE_FORMAT_DATE_SLASH)) {
                    textView.text = reference.referenceValue.convertDateToDesireFormat(
                        DateFormatEnum.DATE_FORMAT_DATE
                    )
                } else {
                    textView.text = reference.referenceValue.notEmpty()
                }
                ll_fields.addView(viewFields)
            }
        }
        tv_check_number_title.isVisible = accountDetails.checkNumber != null
        tv_check_number.isVisible = accountDetails.checkNumber != null
        border_check_number.isVisible = accountDetails.checkNumber != null
        btn_view_transaction.isVisible = accountDetails.portalTransaction != null &&
                "Debit".equals(accountDetails.transactionClass?.description, true)

        setupShareDetails(accountDetails)
        btn_view_transaction.setOnClickListener {
            viewModel.onClickedViewTransaction()
        }
    }

    private fun setupShareDetails(accountDetails: AccountTransactionHistoryDetails) {
        textViewQRCodeDesc.isVisible = false
        imageViewQRCode.isVisible = false
        tv_share_reference_number.text = accountDetails.referenceNumber.notEmpty()
        tv_share_account.text = formatString(
            R.string.params_account_detail,
            accountDetails.account?.name,
            accountDetails.account?.accountNumber.formatAccountNumber(),
            accountDetails.account?.productCodeDesc
        ).toHtmlSpan()
        tv_share_posted_date.text =
            accountDetails.postingDate.convertDateToDesireFormat(DateFormatEnum.DATE_FORMAT_DEFAULT)
        tv_share_transaction_date.text =
            accountDetails.transactionDate.convertDateToDesireFormat(DateFormatEnum.DATE_FORMAT_DEFAULT)
        tv_share_check_number_title.isVisible = accountDetails.checkNumber != null
        tv_share_check_number.isVisible = accountDetails.checkNumber != null
        tv_share_check_number.text = accountDetails.checkNumber.notEmpty()
        tv_share_description.text = accountDetails.description.notEmpty()
        tv_share_amount.text =
            accountDetails.amount?.value.toString().formatAmount(accountDetails.amount?.currency)
        tv_share_ending_balance.text =
            viewModel.record.value?.endingBalance.formatAmount(viewModel.record.value?.balanceCurrency)
        iv_share_transfer_type.setImageResource(
            ConstantHelper.Drawable.getAccountTransactionType(
                accountDetails.transactionClass
            )
        )
        accountDetails.billsPayment?.also { billsPayment ->
            tv_share_remittance_details.isVisible = true
            tv_share_fields.isVisible = true
            border_remittance_details.isVisible = true
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

            tv_share_fields.text = (
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
        tv_share_date_downloaded.text = viewUtil.getCurrentDateString()
    }

    private fun initViews() {
        val buttonShare = view_share_button.findViewById<Button>(R.id.buttonShare)
        buttonShare.setOnClickListener {
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
                    sv_share.isVisible = true
                    Handler().postDelayed(
                        {
                            val shareBitmap = viewUtil.getBitmapByView(viewShareDetails)
                            sv_share.isVisible = false
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

}
