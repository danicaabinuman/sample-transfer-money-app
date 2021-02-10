package com.unionbankph.corporate.fund_transfer.presentation.pddts

import android.Manifest
import android.os.Bundle
import android.os.Handler
import android.text.Html
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
import com.unionbankph.corporate.common.data.model.ContextualClassStatus
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
import com.unionbankph.corporate.fund_transfer.data.form.FundTransferPesoNetForm
import com.unionbankph.corporate.fund_transfer.data.model.FundTransferVerify
import com.unionbankph.corporate.fund_transfer.data.model.TransactionVerify
import com.unionbankph.corporate.fund_transfer.presentation.organization_transfer.OrganizationTransferActivity
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_fund_transfer_summary_pesonet.*
import kotlinx.android.synthetic.main.widget_button_share_outline.*
import kotlinx.android.synthetic.main.widget_header_transaction_summary.*
import kotlinx.android.synthetic.main.widget_transparent_org_appbar.*
import timber.log.Timber
import java.util.concurrent.TimeUnit

class PDDTSSummaryActivity :
    BaseActivity<PDDTSViewModel>(R.layout.activity_fund_transfer_summary_pesonet),
    OnTutorialListener {

    private val fundTransferVerify by lazyFast {
        JsonHelper.fromJson<FundTransferVerify>(intent.getStringExtra(EXTRA_FUND_TRANSFER_DTO))
    }

    private lateinit var referenceId: String

    private lateinit var status: ContextualClassStatus

    private lateinit var errorMessage: String

    private lateinit var fundTransferPesoNetForm: FundTransferPesoNetForm

    private lateinit var selectedAccount: Account

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
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
        // tutorialViewModel.hasTutorial(TutorialScreenEnum.PDDTS_SUMMARY)
    }

    private fun initGeneralViewModel() {
        generalViewModel.state.observe(this, Observer {
            when (it) {
                is ShowGeneralGetOrganizationName -> {
                    setToolbarTitle(
                        textViewTitle,
                        textViewCorporationName,
                        formatString(R.string.title_transfer_summary),
                        it.orgName
                    )
                }
            }
        })
        generalViewModel.getOrgName()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this, viewModelFactory)[PDDTSViewModel::class.java]
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowPDDTSError -> {
                    handleOnError(it.throwable)
                }
            }
        })
        viewModel.isEnabledFeature()
    }

    override fun onViewsBound() {
        super.onViewsBound()
        referenceId = fundTransferVerify.transactions[0].transactionReferenceId.notNullable()
        status = fundTransferVerify.transactions[0].status ?: ContextualClassStatus()
        errorMessage = fundTransferVerify.transactions[0].errorMessage.notNullable()
        setupBinding()
    }

    private fun setupBinding() {
        viewModel.contextualClassFeatureToggle.subscribe {
            if (intent.getStringExtra(EXTRA_FUND_TRANSFER) != null &&
                intent.getStringExtra(EXTRA_ACCOUNT) != null
            ) {
                fundTransferPesoNetForm =
                    JsonHelper.fromJson(intent.getStringExtra(EXTRA_FUND_TRANSFER))
                selectedAccount = JsonHelper.fromJson(intent.getStringExtra(EXTRA_ACCOUNT))
                setFundTransferDetails(fundTransferPesoNetForm, selectedAccount, it)
            }
        }.addTo(disposables)
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        tutorialEngineUtil.setOnTutorialListener(this)
        RxView.clicks(buttonViewOrganizationTransactions)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                navigateFTDashboard()
            }
            .addTo(disposables)
        RxView.clicks(buttonMakeAnotherTransfer)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                navigateChannels()
            }
            .addTo(disposables)
        RxView.clicks(buttonShare)
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
            scrollView.post { scrollView.smoothScrollTo(0, 0) }
        } else {
            val radius = resources.getDimension(R.dimen.button_radius)
            when (view) {
                buttonViewOrganizationTransactions -> {
                    viewUtil.setFocusOnView(scrollView, buttonMakeAnotherTransfer)
                    tutorialEngineUtil.startTutorial(
                        this,
                        buttonMakeAnotherTransfer,
                        R.layout.frame_tutorial_lower_left,
                        radius,
                        false,
                        getString(R.string.msg_tutorial_ubp_summary_make_another),
                        GravityEnum.TOP,
                        OverlayAnimationEnum.ANIM_EXPLODE
                    )
                }
                else -> {
                    scrollView.post { scrollView.smoothScrollTo(0, 0) }
                    // tutorialViewModel.setTutorial(TutorialScreenEnum.PDDTS_SUMMARY, false)
                }
            }
        }
    }

    override fun onBackPressed() {
        eventBus.settingsSyncEvent.emmit(BaseEvent(SettingsSyncEvent.ACTION_REFRESH_SCROLL_TO_TOP))
        backToDashBoard()
    }

    private fun setFundTransferDetails(
        fundTransferPesoNetForm: FundTransferPesoNetForm,
        account: Account,
        contextualFeature: Boolean
    ) {
        val transaction = fundTransferVerify.transactions[0]
        eventBus.accountSyncEvent.emmit(
            BaseEvent(
                AccountSyncEvent.ACTION_UPDATE_CURRENT_BALANCE,
                selectedAccount
            )
        )
        imageViewHeader.post {
            imageViewHeader.layoutParams.width = imageViewHeader.measuredWidth
        }
        textViewTransferFrom.text =
            Html.fromHtml(
                String.format(
                    getString(R.string.params_account_detail),
                    account.name,
                    viewUtil.getAccountNumberFormat(account.accountNumber),
                    viewUtil.getStringOrEmpty(account.productCodeDesc)
                )
            )

        textViewTransferTo.text = if (fundTransferPesoNetForm.beneficiaryMasterForm == null) {
            Html.fromHtml(
                String.format(
                    getString(R.string.params_two_format),
                    fundTransferPesoNetForm.beneficiaryName,
                    viewUtil.getAccountNumberFormat(fundTransferPesoNetForm.receiverAccountNumber)
                )
            )
        } else {
            Html.fromHtml(
                String.format(
                    getString(R.string.params_two_format),
                    fundTransferPesoNetForm.beneficiaryMasterForm?.beneficiaryName,
                    viewUtil.getAccountNumberFormat(
                        fundTransferPesoNetForm.beneficiaryMasterForm?.beneficiaryBankAccountNumber
                    )
                )
            )
        }
        textViewReceivingBank.text = if (fundTransferPesoNetForm.beneficiaryMasterForm == null) {
            fundTransferPesoNetForm.receivingBankName
        } else {
            fundTransferPesoNetForm.beneficiaryMasterForm?.beneficiaryBankName
        }

        textViewAmount.text =
            AutoFormatUtil().formatWithTwoDecimalPlaces(
                fundTransferPesoNetForm.amount.toString(),
                account.currency
            )

        if (intent.getStringExtra(EXTRA_CUSTOM_SERVICE_FEE) != null &&
            intent.getStringExtra(EXTRA_SERVICE_FEE) != null
        ) {
            val customServiceFee = JsonHelper.fromJson<ServiceFee>(
                intent.getStringExtra(EXTRA_CUSTOM_SERVICE_FEE)
            )
            if (0.00 >= customServiceFee.value?.toDouble() ?: 0.00) {
                textViewServiceFee.text = getString(R.string.value_service_fee_free)
            } else {
                textViewServiceFee.text = formatString(
                    R.string.value_service,
                    autoFormatUtil.formatWithTwoDecimalPlaces(
                        customServiceFee.value,
                        customServiceFee.currency
                    )
                )
            }
            textViewServiceDiscountFee.visibility(true)
            viewBorderServiceDiscountFee.visibility(true)
        } else {
            textViewServiceDiscountFee.visibility(false)
            viewBorderServiceDiscountFee.visibility(false)
        }
        if (intent.getStringExtra(EXTRA_SERVICE_FEE) != null) {
            val serviceFee = JsonHelper.fromJson<ServiceFee>(
                intent.getStringExtra(EXTRA_SERVICE_FEE)
            )
            if (intent.getStringExtra(EXTRA_CUSTOM_SERVICE_FEE) != null) {
                textViewServiceDiscountFee.text = formatString(
                    R.string.value_service,
                    autoFormatUtil.formatWithTwoDecimalPlaces(
                        serviceFee.value,
                        serviceFee.currency
                    )
                )
            } else {
                textViewServiceFee.text = formatString(
                    R.string.value_service,
                    autoFormatUtil.formatWithTwoDecimalPlaces(
                        serviceFee.value,
                        serviceFee.currency
                    )
                )
            }
        } else {
            textViewServiceFee.text = getString(R.string.value_service_fee_free)
        }

        textViewChannel.text = intent.getStringExtra(EXTRA_ACCOUNT_TYPE)
        textViewPurpose.text = fundTransferPesoNetForm.purposeDesc
        textViewRemarks.text = viewUtil.getStringOrEmpty(fundTransferPesoNetForm.remarks)
        if (fundTransferPesoNetForm.remarks == null || fundTransferPesoNetForm.remarks == "") {
            textViewRemarksTitle.visibility = View.GONE
            textViewRemarks.visibility = View.GONE
            view9.visibility = View.GONE
        }
        textViewProposedTransferDate.text =
            if (fundTransferPesoNetForm.immediate!!)
                getString(R.string.title_immediately)
            else
                viewUtil.getDateFormatByDateString(
                    fundTransferPesoNetForm.transferDate,
                    ViewUtil.DATE_FORMAT_ISO,
                    ViewUtil.DATE_FORMAT_DEFAULT
                )
        if (fundTransferPesoNetForm.immediate!!) {
            textViewStartDateTitle.visibility = View.GONE
            textViewStartDate.visibility = View.GONE
            view5.visibility = View.GONE
            textViewFrequencyTitle.visibility = View.GONE
            textViewFrequency.visibility = View.GONE
            view6.visibility = View.GONE
            textViewEndDateTitle.visibility = View.GONE
            textViewEndDate.visibility = View.GONE
            view7.visibility = View.GONE
            textViewProposedTransferDateTitle.visibility = View.VISIBLE
            textViewProposedTransferDate.visibility = View.VISIBLE
            view4.visibility = View.VISIBLE
        } else {
            textViewStartDate.text =
                viewUtil.getDateFormatByDateString(
                    fundTransferPesoNetForm.transferDate,
                    ViewUtil.DATE_FORMAT_ISO,
                    ViewUtil.DATE_FORMAT_DEFAULT
                )
            if (fundTransferPesoNetForm.recurrenceEndDate != null) {
                val endDate = viewUtil.getDateFormatByDateString(
                    fundTransferPesoNetForm.recurrenceEndDate,
                    ViewUtil.DATE_FORMAT_ISO,
                    ViewUtil.DATE_FORMAT_DATE
                )
                textViewEndDate.text =
                    ("${fundTransferPesoNetForm.occurrencesText}\n(Until $endDate)")
            } else {
                textViewEndDate.text = fundTransferPesoNetForm.occurrencesText
            }
            textViewFrequency.text = fundTransferPesoNetForm.frequency
            if (fundTransferPesoNetForm.frequency == getString(R.string.title_one_time)) {
                textViewEndDate.visibility = View.GONE
                textViewEndDateTitle.visibility = View.GONE
                view7.visibility = View.GONE
                textViewFrequencyTitle.visibility = View.GONE
                textViewFrequency.visibility = View.GONE
                view6.visibility = View.GONE
                textViewStartDateTitle.visibility = View.GONE
                textViewStartDate.visibility = View.GONE
                view5.visibility = View.GONE
            } else {
                textViewProposedTransferDateTitle.visibility = View.GONE
                textViewProposedTransferDate.visibility = View.GONE
                view4.visibility = View.GONE
            }
        }
        textViewCreatedBy.text = fundTransferVerify.createdBy
        textViewCreatedOn.text = viewUtil.getDateFormatByDateString(
            transaction.transferDate,
            DateFormatEnum.DATE_FORMAT_ISO_WITHOUT_T.value,
            DateFormatEnum.DATE_FORMAT_DEFAULT.value
        )
        textViewDateDownloaded.text = viewUtil.getCurrentDateString()

        setTransactionStatus(contextualFeature, transaction)
    }

    private fun setTransactionStatus(
        contextualFeature: Boolean,
        transaction: TransactionVerify
    ) {
        if (contextualFeature) {
            when (status.type) {
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
            when (status.type) {
                Constant.STATUS_FOR_APPROVAL -> {
                    initTransactionStatus(
                        R.drawable.bg_rectangle_yellow,
                        R.drawable.ic_fund_transfer_clock_yellow,
                        formatString(R.string.title_fund_transfer_sent),
                        R.color.colorPendingStatus,
                        formatString(
                            R.string.msg_fund_transfer_sent_summary,
                            fundTransferVerify.approvalHierarchy?.name
                        )
                    )
                }
                Constant.STATUS_CONDITIONAL -> {
                    initTransactionStatus(
                        R.drawable.bg_rectangle_gray,
                        R.drawable.ic_fund_transfer_gear_gray,
                        formatString(R.string.title_fund_transfer_for_processing),
                        R.color.colorInfo,
                        formatString(R.string.msg_fund_transfer_for_processing_summary)
                    )
                }
                Constant.STATUS_SCHEDULED -> {
                    initTransactionStatus(
                        R.drawable.bg_rectangle_gray,
                        R.drawable.ic_fund_transfer_calendar_gray,
                        formatString(R.string.title_fund_transfer_scheduled),
                        R.color.colorInfo,
                        formatString(
                            R.string.msg_fund_transfer_scheduled_summary,
                            referenceId
                        )
                    )
                }
                Constant.STATUS_PROCESSING -> {
                    initTransactionStatus(
                        R.drawable.bg_rectangle_gray,
                        R.drawable.ic_fund_transfer_gear_gray,
                        formatString(R.string.title_fund_transfer_processing),
                        R.color.colorInfo,
                        formatString(R.string.msg_fund_transfer_processing_summary)
                    )
                }
                Constant.STATUS_SUCCESS,
                Constant.STATUS_FOR_PROCESSING,
                Constant.STATUS_APPROVED -> {
                    initTransactionStatus(
                        R.drawable.bg_rectangle_green,
                        R.drawable.ic_fund_transfer_check_green,
                        formatString(R.string.title_fund_transfer_success),
                        R.color.colorApprovedStatus,
                        formatString(
                            R.string.msg_fund_transfer_success_ubp_summary,
                            referenceId
                        )
                    )
                }
                Constant.STATUS_RELEASED -> {
                    initTransactionStatus(
                        R.drawable.bg_rectangle_green,
                        R.drawable.ic_fund_transfer_check_green,
                        formatString(R.string.title_fund_transfer_released),
                        R.color.colorApprovedStatus,
                        formatString(R.string.msg_fund_transfer_released_summary)
                    )
                }
                Constant.STATUS_BANCNET_ERROR -> {
                    initTransactionStatus(
                        R.drawable.bg_rectangle_red,
                        R.drawable.ic_fund_transfer_warning_red,
                        formatString(R.string.title_fund_transfer_error),
                        R.color.colorRejectedStatus,
                        formatString(
                            R.string.msg_fund_transfer_failed_error,
                            formatString(R.string.msg_fund_transfer_bancnet_error)
                        )
                    )
                }
                Constant.STATUS_FAILED,
                Constant.STATUS_REVERSED,
                Constant.STATUS_REVERSAL_FAILED -> {
                    initTransactionStatus(
                        R.drawable.bg_rectangle_red,
                        R.drawable.ic_fund_transfer_warning_red,
                        formatString(R.string.title_fund_transfer_error),
                        R.color.colorRejectedStatus,
                        formatString(
                            R.string.msg_fund_transfer_failed_error,
                            errorMessage
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
        viewHeader.setContextCompatBackground(background)
        imageViewHeader.setImageResource(icon)
        textViewHeader.text = header.notEmpty().toHtmlSpan()
        textViewHeader.setContextCompatTextColor(headerColor)
        textViewMsg.text = headerMsg.notEmpty().toHtmlSpan()
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
                        lifecycleOwner(this@PDDTSSummaryActivity)
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
            fundTransferVerify.qrContent,
            resources.getDimension(R.dimen.image_view_qr_code).toInt(),
            resources.getDimension(R.dimen.image_view_qr_code).toInt()
        )
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { showProgressAlertDialog(PDDTSSummaryActivity::class.java.simpleName) }
            .subscribe(
                {
                    imageViewQRCode.setImageBitmap(it.toBitmap())
                    showShareContent(true)
                    Handler().postDelayed(
                        {
                            val shareBitmap = viewUtil.getBitmapByView(constraintLayoutShare)
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
            viewHeaderTransaction.visibility(true)
            viewShareButton.visibility(false)
            viewBorderCreatedBy.visibility(true)
            textViewCreatedByTitle.visibility(true)
            textViewCreatedBy.visibility(true)
            textViewCreatedOnTitle.visibility(true)
            textViewCreatedOn.visibility(true)
            textViewDateDownloadedTitle.visibility(true)
            textViewDateDownloaded.visibility(true)
            buttonMakeAnotherTransfer.visibility(false)
            buttonViewOrganizationTransactions.visibility(false)
        } else {
            viewHeaderTransaction.visibility(false)
            viewShareButton.visibility(true)
            viewBorderCreatedBy.visibility(false)
            textViewCreatedByTitle.visibility(false)
            textViewCreatedBy.visibility(false)
            textViewCreatedOnTitle.visibility(false)
            textViewCreatedOn.visibility(false)
            textViewDateDownloadedTitle.visibility(false)
            textViewDateDownloaded.visibility(false)
            buttonMakeAnotherTransfer.visibility(true)
            buttonViewOrganizationTransactions.visibility(true)
        }
    }

    private fun navigateFTDashboard() {
        navigator.navigateClearUpStack(
            this,
            OrganizationTransferActivity::class.java,
            null,
            isClear = true,
            isAnimated = true
        )
        eventBus.actionSyncEvent.emmit(
            BaseEvent(ActionSyncEvent.ACTION_UPDATE_TRANSACTION_LIST)
        )
    }

    private fun navigateChannels() {
        val bundle = Bundle().apply {
            putString(ChannelActivity.EXTRA_PAGE, ChannelActivity.PAGE_FUND_TRANSFER)
        }
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
    }

    private fun startViewTutorial() {
        viewUtil.setFocusOnView(scrollView, buttonViewOrganizationTransactions)
        val radius = resources.getDimension(R.dimen.button_radius)
        tutorialEngineUtil.startTutorial(
            this,
            buttonViewOrganizationTransactions,
            R.layout.frame_tutorial_lower_left,
            radius,
            false,
            getString(R.string.msg_tutorial_ubp_summary_view_organization),
            GravityEnum.TOP,
            OverlayAnimationEnum.ANIM_EXPLODE
        )
    }

    companion object {
        const val EXTRA_FUND_TRANSFER = "fund_transfer"
        const val EXTRA_FUND_TRANSFER_DTO = "fund_transfer_dto"
        const val EXTRA_ACCOUNT = "account"
        const val EXTRA_ACCOUNT_TYPE = "account_type"
        const val EXTRA_SERVICE_FEE = "service_fee"
        const val EXTRA_CUSTOM_SERVICE_FEE = "custom_service_fee"
    }
}
