package com.unionbankph.corporate.bills_payment.presentation.bills_payment_summary

import android.Manifest
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.jakewharton.rxbinding2.view.RxView
import com.takusemba.spotlight.Spotlight
import com.tbruyelle.rxpermissions2.RxPermissions
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.platform.bus.event.AccountSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.SettingsSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.widget.qrgenerator.RxQrCode
import com.unionbankph.corporate.app.common.widget.tutorial.OnTutorialListener
import com.unionbankph.corporate.app.util.AutoFormatUtil
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.bills_payment.data.form.BillsPaymentForm
import com.unionbankph.corporate.bills_payment.data.model.BillsPaymentVerify
import com.unionbankph.corporate.bills_payment.data.model.Transaction
import com.unionbankph.corporate.bills_payment.presentation.organization_payment.OrganizationPaymentActivity
import com.unionbankph.corporate.common.data.model.ServiceFee
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.constant.DateFormatEnum
import com.unionbankph.corporate.common.presentation.constant.GravityEnum
import com.unionbankph.corporate.common.presentation.constant.OverlayAnimationEnum
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.ShowGeneralGetOrganizationName
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialError
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialHasTutorial
import com.unionbankph.corporate.common.presentation.viewmodel.TutorialViewModel
import com.unionbankph.corporate.corporate.presentation.channel.ChannelActivity
import com.unionbankph.corporate.databinding.ActivityBillsPaymentSummaryBinding
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import java.util.concurrent.TimeUnit

class BillsPaymentSummaryActivity :
    BaseActivity<ActivityBillsPaymentSummaryBinding, BillsPaymentSummaryViewModel>(),
    OnTutorialListener {

    private lateinit var billsPaymentForm: BillsPaymentForm

    private lateinit var billsPaymentVerify: BillsPaymentVerify

    private lateinit var selectedAccount: Account

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initTutorialViewModel()
        initGeneralViewModel()
        initViewModel()
    }

    private fun initTutorialViewModel() {
        tutorialViewModel = ViewModelProviders.of(
            this,
            viewModelFactory
        )[TutorialViewModel::class.java]
        tutorialViewModel.state.observe(this, Observer {
            when (it) {
                is ShowTutorialHasTutorial -> {
                    if (it.hasTutorial) {
                        startViewTutorial()
                    }
                }
                is ShowTutorialError -> {
                    handleOnError(it.throwable)
                }
            }
        })
        // tutorialViewModel.hasTutorial(TutorialScreenEnum.BILLS_PAYMENT_SUMMARY)
    }

    private fun initGeneralViewModel() {
        generalViewModel.state.observe(this, Observer {
            when (it) {
                is ShowGeneralGetOrganizationName -> {
                    setToolbarTitle(
                        binding.viewToolbar.textViewTitle,
                        binding.viewToolbar.textViewCorporationName,
                        formatString(R.string.title_payment_summary),
                        it.orgName
                    )
                }
            }
        })
        generalViewModel.getOrgName()
    }

    private fun initViewModel() {
        viewModel.summaryState.observe(this, Observer {
            when (it) {
                is ShowBillsPaymentSummaryError -> {
                    handleOnError(it.throwable)
                }
            }
        })
    }

    override fun onViewsBound() {
        super.onViewsBound()
        setupBinding()
    }

    private fun setupBinding() {
        viewModel.contextualClassFeatureToggle.subscribe {
            if (intent.getStringExtra(EXTRA_BILLS_PAYMENT) != null &&
                intent.getStringExtra(EXTRA_ACCOUNT) != null
            ) {
                billsPaymentForm = JsonHelper.fromJson(intent.getStringExtra(EXTRA_BILLS_PAYMENT))
                billsPaymentVerify =
                    JsonHelper.fromJson(intent.getStringExtra(EXTRA_BILLS_PAYMENT_DTO))
                selectedAccount = JsonHelper.fromJson(intent.getStringExtra(EXTRA_ACCOUNT))
                initBillsPaymentDetail(billsPaymentForm, billsPaymentVerify, selectedAccount, it)
            }
        }.addTo(disposables)
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        tutorialEngineUtil.setOnTutorialListener(this)
        RxView.clicks(binding.buttonViewOrganizationPayments)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                navigateBPDashboard()
                eventBus.actionSyncEvent.emmit(
                    BaseEvent(ActionSyncEvent.ACTION_UPDATE_TRANSACTION_LIST)
                )
            }.addTo(disposables)
        RxView.clicks(binding.buttonMakeAnotherPayment)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                val bundle = Bundle()
                bundle.putString(ChannelActivity.EXTRA_PAGE, ChannelActivity.PAGE_BILLS_PAYMENT)
                navigator.navigateClearUpStack(
                    this,
                    ChannelActivity::class.java,
                    bundle,
                    isClear = true,
                    isAnimated = true
                )
                eventBus.actionSyncEvent.emmit(
                    BaseEvent(ActionSyncEvent.ACTION_UPDATE_TRANSACTION_LIST)
                )
            }.addTo(disposables)
        RxView.clicks(binding.viewShareButton.buttonShare)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                initPermission()
            }.addTo(disposables)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_toolbar, menu)
        val helpMenu = menu.findItem(R.id.menu_help)
        helpMenu.isVisible = true
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.menu_help -> {
                isClickedHelpTutorial = true
                startViewTutorial()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onClickSkipButtonTutorial(spotlight: Spotlight) {
        isSkipTutorial = true
        tutorialViewModel.skipTutorial()
        spotlight.closeSpotlight()
    }

    override fun onClickOkButtonTutorial(spotlight: Spotlight) {
        spotlight.closeCurrentTarget()
    }

    override fun onStartedTutorial(view: View?, viewTarget: View) {
        // onStartedTutorial
        isSkipTutorial = false
    }

    override fun onEndedTutorial(view: View?, viewTarget: View) {
        if (isSkipTutorial) {
            binding.scrollView.post { binding.scrollView.smoothScrollTo(0, 0) }
        } else {
            val radius = resources.getDimension(R.dimen.button_radius)
            when (view) {
                binding.buttonViewOrganizationPayments -> {
                    viewUtil.setFocusOnView(binding.scrollView, binding.buttonMakeAnotherPayment)
                    tutorialEngineUtil.startTutorial(
                        this,
                        binding.buttonMakeAnotherPayment,
                        R.layout.frame_tutorial_lower_left,
                        radius,
                        false,
                        getString(R.string.msg_tutorial_ubp_summary_make_another),
                        GravityEnum.TOP,
                        OverlayAnimationEnum.ANIM_EXPLODE
                    )
                }
                else -> {
                    binding.scrollView.post { binding.scrollView.smoothScrollTo(0, 0) }
                    // tutorialViewModel.setTutorial(TutorialScreenEnum.BILLS_PAYMENT_SUMMARY, false)
                }
            }
        }
    }

    override fun onBackPressed() {
        eventBus.settingsSyncEvent.emmit(BaseEvent(SettingsSyncEvent.ACTION_REFRESH_SCROLL_TO_TOP))
        backToDashBoard()
    }

    private fun initBillsPaymentDetail(
        billsPaymentForm: BillsPaymentForm,
        billsPaymentVerify: BillsPaymentVerify,
        account: Account,
        contextualFeature: Boolean
    ) {
        val transaction = billsPaymentVerify.transactions[0]
        eventBus.accountSyncEvent.emmit(
            BaseEvent(
                AccountSyncEvent.ACTION_UPDATE_CURRENT_BALANCE,
                selectedAccount
            )
        )
        binding.imageViewHeader.post {
            binding.imageViewHeader.layoutParams.width = binding.imageViewHeader.measuredWidth
        }
        binding.textViewTransferFrom.text =
            Html.fromHtml(
                String.format(
                    getString(R.string.params_account_detail),
                    account.name,
                    viewUtil.getAccountNumberFormat(account.accountNumber),
                    viewUtil.getStringOrEmpty(account.productCodeDesc)
                )
            )
        val sb = StringBuilder()
        transaction.references
            ?.sortedWith(compareBy { it.index })
            ?.forEachIndexed { index, reference ->
                if (viewUtil.isValidDateFormat(ViewUtil.DATE_FORMAT_DATE_SLASH, reference.value) &&
                    reference.name.equals("Return Period", true)
                ) {
                    sb.append(
                        viewUtil.getDateFormatByDateString(
                            reference.value,
                            ViewUtil.DATE_FORMAT_DATE_SLASH,
                            ViewUtil.DATE_FORMAT_DATE
                        )
                    )
                } else {
                    sb.append(reference.value.notEmpty())
                }
                sb.append("<br>")
                sb.append(
                    String.format(
                        getString(R.string.format_change_color),
                        reference.name
                    )
                )
                if (index != transaction.references?.size?.minus(1)) {
                    sb.append("<br>")
                    sb.append("<br>")
                }
            }

        binding.textViewPaymentTo.text =
            Html.fromHtml(
                billsPaymentForm.billerName +
                        "<br>" +
                        "<br>" +
                        sb.toString()
            )

        binding.textViewAmount.text = AutoFormatUtil().formatWithTwoDecimalPlaces(
            billsPaymentForm.amount?.value.toString(), billsPaymentForm.amount?.currency
        )
        binding.textViewServiceFee.text = if (intent.getStringExtra(EXTRA_SERVICE_FEE) != null) {
            val serviceFee = JsonHelper.fromJson<ServiceFee>(
                intent.getStringExtra(EXTRA_SERVICE_FEE)
            )
            String.format(
                getString(R.string.value_service),
                autoFormatUtil.formatWithTwoDecimalPlaces(
                    serviceFee.value,
                    serviceFee.currency
                )
            )
        } else {
            getString(R.string.value_service_fee_free)
        }
        binding.textViewProposedTransferDate.text =
            if (billsPaymentForm.immediate!!)
                getString(R.string.title_immediately)
            else
                viewUtil.getDateFormatByDateString(
                    billsPaymentForm.paymentDate,
                    ViewUtil.DATE_FORMAT_ISO,
                    ViewUtil.DATE_FORMAT_DEFAULT
                )
        if (billsPaymentForm.immediate!!) {
            binding.view4.visibility = View.GONE
            binding.textViewStartDateTitle.visibility = View.GONE
            binding.textViewStartDate.visibility = View.GONE
            binding.view5.visibility = View.GONE
            binding.textViewFrequencyTitle.visibility = View.GONE
            binding.textViewFrequency.visibility = View.GONE
            binding.view6.visibility = View.GONE
            binding.textViewEndDateTitle.visibility = View.GONE
            binding.textViewEndDate.visibility = View.GONE
            binding.view7.visibility = View.GONE
            binding.textViewProposedTransferDateTitle.visibility = View.VISIBLE
            binding.textViewProposedTransferDate.visibility = View.VISIBLE
        } else {
            binding.textViewStartDate.text =
                viewUtil.getDateFormatByDateString(
                    billsPaymentForm.paymentDate,
                    ViewUtil.DATE_FORMAT_ISO,
                    ViewUtil.DATE_FORMAT_DEFAULT
                )
            binding.textViewEndDate.text =
                viewUtil.getDateFormatByDateString(
                    billsPaymentForm.recurrenceEndDate,
                    ViewUtil.DATE_FORMAT_ISO,
                    ViewUtil.DATE_FORMAT_DATE
                )
            binding.textViewFrequency.text = billsPaymentForm.frequency
            if (billsPaymentForm.frequency == getString(R.string.title_one_time)) {
                binding.textViewEndDate.visibility = View.GONE
                binding.textViewEndDateTitle.visibility = View.GONE
                binding.view7.visibility = View.GONE
                binding.textViewFrequencyTitle.visibility = View.GONE
                binding.textViewFrequency.visibility = View.GONE
                binding.view6.visibility = View.GONE
                binding.textViewStartDateTitle.visibility = View.GONE
                binding.textViewStartDate.visibility = View.GONE
                binding.view5.visibility = View.GONE
                binding.view4.visibility = View.GONE
            } else {
                binding.textViewProposedTransferDateTitle.visibility = View.GONE
                binding.textViewProposedTransferDate.visibility = View.GONE
                binding.view4.visibility = View.GONE
            }
        }
        binding.textViewRemarks.text = viewUtil.getStringOrEmpty(billsPaymentForm.remarks)
        if (billsPaymentForm.remarks == null || billsPaymentForm.remarks == "") {
            binding.textViewRemarksTitle.visibility = View.GONE
            binding.textViewRemarks.visibility = View.GONE
            binding.view7.visibility = View.GONE
        } else {
            binding.textViewRemarksTitle.visibility = View.VISIBLE
            binding.textViewRemarks.visibility = View.VISIBLE
            binding.view7.visibility = View.VISIBLE
        }
        binding.textViewCreatedBy.text = billsPaymentVerify.createdBy
        binding.textViewCreatedOn.text = viewUtil.getDateFormatByDateString(
            billsPaymentVerify.createdDate,
            DateFormatEnum.DATE_FORMAT_ISO_WITHOUT_T.value,
            DateFormatEnum.DATE_FORMAT_DEFAULT.value
        )
        binding.textViewDateDownloaded.text = viewUtil.getCurrentDateString()
        setTransactionStatus(contextualFeature, transaction)
    }

    private fun setTransactionStatus(
        contextualFeature: Boolean,
        transaction: Transaction
    ) {
        if (contextualFeature) {
            when (transaction.status?.type) {
                Constant.STATUS_FOR_APPROVAL -> {
                    initTransactionStatus(
                        R.drawable.bg_rectangle_yellow,
                        R.drawable.ic_fund_transfer_clock_yellow,
                        transaction.status?.title,
                        R.color.colorPendingStatus,
                        transaction.status?.message
                    )
                }
                Constant.STATUS_CONDITIONAL,
                Constant.STATUS_PROCESSING -> {
                    initTransactionStatus(
                        R.drawable.bg_rectangle_gray,
                        R.drawable.ic_fund_transfer_gear_gray,
                        transaction.status?.title,
                        R.color.colorInfo,
                        transaction.status?.message
                    )
                }
                Constant.STATUS_SCHEDULED -> {
                    initTransactionStatus(
                        R.drawable.bg_rectangle_gray,
                        R.drawable.ic_fund_transfer_calendar_gray,
                        transaction.status?.title,
                        R.color.colorInfo,
                        transaction.status?.message
                    )
                }
                Constant.STATUS_SUCCESS,
                Constant.STATUS_FOR_PROCESSING,
                Constant.STATUS_RELEASED,
                Constant.STATUS_APPROVED -> {
                    initTransactionStatus(
                        R.drawable.bg_rectangle_green,
                        R.drawable.ic_fund_transfer_check_green,
                        transaction.status?.title,
                        R.color.colorApprovedStatus,
                        transaction.status?.message
                    )
                }
                Constant.STATUS_BANCNET_ERROR,
                Constant.STATUS_FAILED,
                Constant.STATUS_REVERSED,
                Constant.STATUS_REVERSAL_FAILED -> {
                    initTransactionStatus(
                        R.drawable.bg_rectangle_red,
                        R.drawable.ic_fund_transfer_warning_red,
                        transaction.status?.title,
                        R.color.colorRejectedStatus,
                        transaction.status?.message
                    )
                }
                else -> {
                    initTransactionStatus(
                        R.drawable.bg_rectangle_gray,
                        R.drawable.ic_fund_transfer_check_gray,
                        transaction.status?.title,
                        R.color.colorInfo,
                        transaction.status?.message
                    )
                }
            }
        } else {
            when (intent.getStringExtra(EXTRA_STATUS)) {
                Constant.STATUS_FOR_APPROVAL,
                Constant.STATUS_CONDITIONAL -> {
                    initTransactionStatus(
                        R.drawable.bg_rectangle_yellow,
                        R.drawable.ic_fund_transfer_clock_yellow,
                        formatString(R.string.title_bills_payment_sent),
                        R.color.colorPendingStatus,
                        formatString(
                            R.string.msg_bills_payment_sent_summary,
                            intent.getStringExtra(EXTRA_APPROVAL_NAME)
                        )
                    )
                }
                Constant.STATUS_SCHEDULED -> {
                    initTransactionStatus(
                        R.drawable.bg_rectangle_gray,
                        R.drawable.ic_fund_transfer_calendar_gray,
                        formatString(R.string.title_bills_payment_scheduled),
                        R.color.colorInfo,
                        formatString(
                            R.string.msg_bills_payment_scheduled_summary,
                            intent.getStringExtra(EXTRA_REFERENCE_ID)
                        )
                    )
                }
                Constant.STATUS_SUCCESS,
                Constant.STATUS_RELEASED,
                Constant.STATUS_FOR_PROCESSING,
                Constant.STATUS_APPROVED -> {
                    initTransactionStatus(
                        R.drawable.bg_rectangle_green,
                        R.drawable.ic_fund_transfer_check_green,
                        formatString(R.string.title_bills_payment_success),
                        R.color.colorApprovedStatus,
                        formatString(
                            R.string.msg_bills_payment_success_summary,
                            intent.getStringExtra(EXTRA_REFERENCE_ID)
                        )
                    )
                }
                Constant.STATUS_REJECTED,
                Constant.STATUS_FAILED,
                Constant.STATUS_REVERSED,
                Constant.STATUS_REVERSAL_FAILED -> {
                    initTransactionStatus(
                        R.drawable.bg_rectangle_red,
                        R.drawable.ic_fund_transfer_warning_red,
                        formatString(R.string.title_bills_payment_error),
                        R.color.colorRejectedStatus,
                        formatString(
                            R.string.msg_bills_payment_error_summary,
                            intent.getStringExtra(EXTRA_ERROR_MESSAGE)
                        )
                    )
                }
                else -> {
                    initTransactionStatus(
                        R.drawable.bg_rectangle_gray,
                        R.drawable.ic_fund_transfer_check_gray,
                        formatString(R.string.title_fund_transfer_for_processing),
                        R.color.colorInfo,
                        formatString(R.string.msg_fund_transfer_default_status)
                    )
                }
            }
        }
    }

    private fun initTransactionStatus(
        background: Int,
        icon: Int,
        header: String?,
        headerColor: Int,
        headerMsg: String?
    ) {
        binding.viewHeader.setContextCompatBackground(background)
        binding.imageViewHeader.setImageResource(icon)
        binding.textViewHeader.text = header.notEmpty().toHtmlSpan()
        binding.textViewHeader.setContextCompatTextColor(headerColor)
        binding.textViewMsg.text = headerMsg.notEmpty().toHtmlSpan()
    }


    private fun initPermission() {
        RxPermissions(this)
            .request(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            .subscribe { granted ->
                if (granted) {
                    Handler().postDelayed(
                        {
                            showShareableContent()
                        }, resources.getInteger(R.integer.time_delay_thread).toLong()
                    )
                } else {
                    MaterialDialog(this).show {
                        lifecycleOwner(this@BillsPaymentSummaryActivity)
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

    private fun showShareableContent() {
        RxQrCode.generateQrCodeFile(
            this,
            intent.getStringExtra(EXTRA_QR_CONTENT),
            resources.getDimension(R.dimen.image_view_qr_code).toInt(),
            resources.getDimension(R.dimen.image_view_qr_code).toInt()
        )
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { showProgressAlertDialog(BillsPaymentSummaryActivity::class.java.simpleName) }
            .subscribe(
                {
                    binding.viewHeaderTransaction.imageViewQRCode.setImageBitmap(it.toBitmap())
                    showShareContent(true)
                    Handler().postDelayed(
                        {
                            val shareBitmap = viewUtil.getBitmapByView(binding.constraintLayoutShare)
                            showShareContent(false)
                            dismissProgressAlertDialog()
                            startShareMediaActivity(shareBitmap)
                        }, resources.getInteger(R.integer.time_delay_share_media).toLong()
                    )
                }, {
                    Timber.e(it, "showShareableContent")
                    dismissProgressAlertDialog()
                    handleOnError(it)
                }
            ).addTo(disposables)
    }

    private fun showShareContent(isShown: Boolean) {
        if (isShown) {
            binding.viewHeaderTransaction.root.visibility(true)
            binding.viewShareButton.root.visibility(false)
            binding.viewBorderCreatedBy.visibility(true)
            binding.textViewCreatedByTitle.visibility(true)
            binding.textViewCreatedBy.visibility(true)
            binding.textViewCreatedOnTitle.visibility(true)
            binding.textViewCreatedOn.visibility(true)
            binding.textViewDateDownloadedTitle.visibility(true)
            binding.textViewDateDownloaded.visibility(true)
            binding.buttonMakeAnotherPayment.visibility(false)
            binding.buttonViewOrganizationPayments.visibility(false)
        } else {
            binding.viewHeaderTransaction.root.visibility(false)
            binding.viewShareButton.root.visibility(true)
            binding.viewBorderCreatedBy.visibility(false)
            binding.textViewCreatedByTitle.visibility(false)
            binding.textViewCreatedBy.visibility(false)
            binding.textViewCreatedOnTitle.visibility(false)
            binding.textViewCreatedOn.visibility(false)
            binding.textViewDateDownloadedTitle.visibility(false)
            binding.textViewDateDownloaded.visibility(false)
            binding.buttonMakeAnotherPayment.visibility(true)
            binding.buttonViewOrganizationPayments.visibility(true)
        }
    }

    private fun navigateBPDashboard() {
        navigator.navigateClearUpStack(
            this,
            OrganizationPaymentActivity::class.java,
            null,
            isClear = true,
            isAnimated = true
        )
    }

    private fun startViewTutorial() {
        viewUtil.setFocusOnView(binding.scrollView, binding.buttonViewOrganizationPayments)
        val radius = resources.getDimension(R.dimen.button_radius)
        tutorialEngineUtil.startTutorial(
            this,
            binding.buttonViewOrganizationPayments,
            R.layout.frame_tutorial_lower_left,
            radius,
            false,
            getString(R.string.msg_tutorial_bills_payment_summary_view_organization),
            GravityEnum.TOP,
            OverlayAnimationEnum.ANIM_EXPLODE
        )
    }

    companion object {

        const val EXTRA_QR_CONTENT = "qr_content"
        const val EXTRA_BILLS_PAYMENT = "bills_payment"
        const val EXTRA_ACCOUNT = "account"
        const val EXTRA_ACCOUNT_TYPE = "account_type"
        const val EXTRA_STATUS = "status"
        const val EXTRA_REFERENCE_ID = "reference_id"
        const val EXTRA_BILLS_PAYMENT_DTO = "bills_payment_dto"
        const val EXTRA_APPROVAL_NAME = "approval_name"
        const val EXTRA_ERROR_MESSAGE = "error_message"
        const val EXTRA_SERVICE_FEE = "service_fee"
    }

    override val viewModelClassType: Class<BillsPaymentSummaryViewModel>
        get() = BillsPaymentSummaryViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityBillsPaymentSummaryBinding
        get() = ActivityBillsPaymentSummaryBinding::inflate
}
