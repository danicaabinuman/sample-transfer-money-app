package com.unionbankph.corporate.approval.presentation.approval_detail

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.text.Html
import android.text.InputType
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.material.textfield.TextInputEditText
import com.jakewharton.rxbinding2.view.RxView
import com.takusemba.spotlight.Spotlight
import com.tbruyelle.rxpermissions2.RxPermissions
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.dialog.ConfirmationBottomSheet
import com.unionbankph.corporate.app.common.widget.qrgenerator.RxQrCode
import com.unionbankph.corporate.app.common.widget.tutorial.OnTutorialListener
import com.unionbankph.corporate.app.common.widget.validator.validation.RxCombineValidator
import com.unionbankph.corporate.app.service.fcm.AutobahnFirebaseMessagingService
import com.unionbankph.corporate.app.util.AutoFormatUtil
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.approval.data.model.ApprovalHierarchyDto
import com.unionbankph.corporate.approval.data.model.Transaction
import com.unionbankph.corporate.approval.presentation.approval_activity_log.ActivityLogActivity
import com.unionbankph.corporate.approval.presentation.approval_batch_list.BatchTransferActivity
import com.unionbankph.corporate.common.data.model.PushNotificationPayload
import com.unionbankph.corporate.common.presentation.callback.OnConfirmationPageCallBack
import com.unionbankph.corporate.common.presentation.constant.ChannelBankEnum
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.constant.DateFormatEnum
import com.unionbankph.corporate.common.presentation.constant.GravityEnum
import com.unionbankph.corporate.common.presentation.constant.OverlayAnimationEnum
import com.unionbankph.corporate.common.presentation.helper.ConstantHelper
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialError
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialHasTutorial
import com.unionbankph.corporate.common.presentation.viewmodel.TutorialViewModel
import com.unionbankph.corporate.databinding.ActivityApprovalDetailsBinding
import com.unionbankph.corporate.general.presentation.result.ResultLandingPageActivity
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.annotation.concurrent.ThreadSafe


class ApprovalDetailActivity :
    BaseActivity<ActivityApprovalDetailsBinding, ApprovalDetailViewModel>(),
    OnTutorialListener {

    private lateinit var approvalDetail: Transaction

    private lateinit var approvalHierarchyManager: ApprovalHierarchyManager

    private var rejectionConfirmationBottomSheet: ConfirmationBottomSheet? = null

    private var approvalConfirmationBottomSheet: ConfirmationBottomSheet? = null

    private var isClickedShare: Boolean = false

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
    }

    override fun onViewsBound() {
        super.onViewsBound()
        init()
        initBinding()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        initPushNotification()
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initTutorialViewModel()
        initViewModel()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        tutorialEngineUtil.setOnTutorialListener(this)
        initEventBus()
        initDataBus()
        initClickListener()
    }

    override fun onBackPressed() {
        onBackPressed(false)
        overridePendingTransition(R.anim.anim_no_change, R.anim.anim_slide_down)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        val helpMenu = menu.findItem(R.id.menu_help)
        viewModel.isShowHelpIcon.value?.let {
            helpMenu.isVisible = it
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.menu_help -> {
                onClickedHelp()
                return true
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
        //onStartedTutorial
        isSkipTutorial = false
    }

    override fun onEndedTutorial(view: View?, viewTarget: View) {
        if (isSkipTutorial) {
            approvalHierarchyManager.tutorialApprovalHierarchyDto = null
            approvalHierarchyManager.resetApprovalHierarchy()
            getApprovalHierarchy()
        } else {
            if (view != null) {
                if (view.id == binding.viewHierarchy.id) {
                    tutorialEngineUtil.startTutorial(
                        this,
                        approvalHierarchyManager.rootView,
                        R.layout.frame_tutorial_upper_left,
                        resources.getDimension(R.dimen.card_radius),
                        false,
                        getString(R.string.msg_tutorial_approval_hierarchy_root),
                        GravityEnum.BOTTOM,
                        OverlayAnimationEnum.ANIM_SLIDE_DOWN
                    )
                } else if (view.id == approvalHierarchyManager.rootView.id) {
                    //tutorialViewModel.setTutorial(TutorialScreenEnum.APPROVAL_DETAILS, false)
                    approvalHierarchyManager.tutorialApprovalHierarchyDto = null
                    approvalHierarchyManager.resetApprovalHierarchy()
                    getApprovalHierarchy()
                }
            }
        }
    }

    private fun init() {
        initPushNotification()
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
                        startTutorial()
                    } else {
                        getApprovalHierarchy()
                    }
                }
                is ShowTutorialError -> {
                    handleOnError(it.throwable)
                }
            }
        })
    }

    private fun initBinding() {
        viewModel.isShowHelpIcon
            .subscribe {
                invalidateOptionsMenu()
            }.addTo(disposables)
    }

    private fun initViewModel() {
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowApprovalDetailInitialLoading -> {
                    showInitialLoading()
                }
                is ShowApprovalDetailInitialDismissLoading -> {
                    dismissInitialLoading()
                }
                is ShowApprovalDetailLoading -> {
                    showApprovalDetailLoading()
                }
                is ShowApprovalDetailDismissLoading -> {
                    dismissApprovalDetailLoading()
                }
                is ShowApprovalRequestLoading -> {
                    showProgressAlertDialog(ApprovalDetailActivity::class.java.simpleName)
                }
                is ShowApprovalRequestDismissLoading -> {
                    dismissProgressAlertDialog()
                }
                is ShowApprovalDetailGetUserDetails -> {
                    approvalHierarchyManager = ApprovalHierarchyManager(
                        this,
                        viewUtil,
                        it.userDetails
                    )
                }
                is ShowApprovalFundTransferDetail -> {
                    updateApprovalDetails(it.item)
                }
                is ShowApprovalBillsPaymentDetail -> {
                    updateApprovalDetails(it.item)
                }
                is ShowApprovalCheckWriterDetail -> {
                    updateApprovalDetails(it.item)
                }
                is ShowApprovalDetailDeleteScheduledTransfer -> {
                    deleteScheduledTransfer()
                }
                is ShowApprovalDetailCancelTransaction -> {
                    showCancelApprovalSuccessDialog(
                        formatString(R.string.msg_transaction_successfully_cancelled)
                    )
                }
                is ShowApprovalDetailErrorSubmit -> {
                    handleOnError(it.throwable)
                }
                is ShowApprovalDetailError -> {
                    binding.nestedScrollView.visibility = View.GONE
                    handleOnError(it.throwable)
                }
            }
        })
        viewModel.hierarchyLiveData.observe(this, Observer {
            drawApprovalHierarchy(it)
        })
    }

    private fun updateApprovalDetails(transaction: Transaction) {
        if (binding.nestedScrollView.visibility == View.GONE) {
            binding.nestedScrollView.visibility = View.VISIBLE
        }
        binding.nestedScrollView.smoothScrollTo(0, 0)
        approvalDetail = transaction
        setInitialView()
        approvalHierarchyManager.resetApprovalHierarchy()
        getApprovalHierarchy()
    }

    private fun showApprovalDetailLoading() {
        binding.viewLoadingState.root.visibility = View.VISIBLE
        binding.constraintLayout.visibility = View.GONE
        if (binding.cardViewApprovalDetail.visibility == View.VISIBLE)
            binding.cardViewApprovalDetail.visibility = View.INVISIBLE
        binding.zoomLayout.setVisible(false)
        binding.zoomLayout.layoutParams.height =
            resources.getDimension(
                R.dimen.constraint_approval_hierarchy_small
            ).toInt()
    }

    private fun dismissApprovalDetailLoading() {
        if (binding.viewLoadingState.root.visibility == View.VISIBLE) {
            binding.viewLoadingState.root.visibility = View.GONE
        }
        binding.constraintLayout.visibility = View.VISIBLE
    }

    private fun dismissInitialLoading() {
        if (binding.viewLoadingStateInitial.root.visibility == View.VISIBLE) {
            binding.viewLoadingStateInitial.root.visibility = View.GONE
        }
    }

    private fun showInitialLoading() {
        if (binding.nestedScrollView.visibility == View.VISIBLE) {
            binding.nestedScrollView.visibility = View.GONE
        }
        binding.viewLoadingStateInitial.root.visibility = View.VISIBLE
    }

    private fun showCancelApprovalSuccessDialog(message: String) {
        MaterialDialog(this).show {
            lifecycleOwner(this@ApprovalDetailActivity)
            title(R.string.title_success)
            message(text = message)
            positiveButton(
                res = R.string.action_ok,
                click = {
                    it.dismiss()
                    eventBus.actionSyncEvent.emmit(
                        BaseEvent(ActionSyncEvent.ACTION_UPDATE_TRANSACTION_LIST)
                    )
                    getDetailsBasedOnChannel()
                }
            )
        }
    }

    private fun startTutorial() {
        val parseApprovalHierarchy =
            viewUtil.loadJSONFromAsset(this, "approval_hierarchy")
        approvalHierarchyManager.tutorialApprovalHierarchyDto =
            JsonHelper.fromJson(parseApprovalHierarchy)
        binding.textViewState.visibility = View.GONE
        drawApprovalHierarchy(approvalHierarchyManager.tutorialApprovalHierarchyDto)
        tutorialEngineUtil.startTutorial(
            this,
            binding.viewHierarchy,
            R.layout.frame_tutorial_upper_left,
            0f,
            false,
            getString(R.string.msg_tutorial_approval_hierarchy),
            GravityEnum.BOTTOM,
            OverlayAnimationEnum.ANIM_SLIDE_DOWN
        )
    }

    private fun initClickListener() {
        RxView.clicks(binding.viewTransactionDetails.buttonApprove)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                showApprovalConfirmationBottomSheet()
            }.addTo(disposables)
        RxView.clicks(binding.viewTransactionDetails.buttonReject)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                showReasonForRejectionBottomSheet(title = R.string.title_reject_transaction)
            }.addTo(disposables)
        RxView.clicks(binding.viewShareButton.buttonShare)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                initPermission()
            }.addTo(disposables)
        RxView.clicks(binding.buttonViewLogs)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                navigateActivityLogsScreen()
            }.addTo(disposables)

        RxView.clicks(binding.buttonViewAllTransaction)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                navigateBatchTransfer()
            }.addTo(disposables)
        RxView.clicks(binding.buttonDelete)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                onClickedCancelTransaction()
            }.addTo(disposables)
    }

    private fun onClickedCancelTransaction() {
        if (approvalDetail.maker == true &&
            approvalDetail.transactionStatus?.type == Constant.STATUS_FOR_APPROVAL
        ) {
            showReasonForRejectionBottomSheet(title = R.string.title_cancel_transaction)
        } else {
            showReasonForRejectionBottomSheet(title = R.string.title_delete_scheduled_transaction)
        }
    }

    private fun navigateActivityLogsScreen() {
        val bundle = Bundle()
        bundle.putString(
            ActivityLogActivity.EXTRA_MODEL,
            JsonHelper.toJson(approvalDetail)
        )
        bundle.putString(
            ActivityLogActivity.EXTRA_PAGE,
            ActivityLogActivity.PAGE_APPROVAL_DETAIL
        )
        navigator.navigate(
            this,
            ActivityLogActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_UP
        )
    }

    private fun showApprovalConfirmationBottomSheet() {
        val name = getTransactionNameTitle().notEmpty()
        val desc = formatString(
            when {
                approvalDetail.batchType == Constant.TYPE_BATCH ->
                    R.string.param_desc_approve_batch_transaction
                approvalDetail.channel.equals(ChannelBankEnum.BILLS_PAYMENT.value, true) ->
                    R.string.param_desc_payment_approve_transaction
                approvalDetail.channel.equals(ChannelBankEnum.CHECK_WRITER.value, true) ->
                    R.string.param_desc_check_approve_transaction
                approvalDetail.channel.equals(ChannelBankEnum.CASH_WITHDRAWAL.name, true) ->
                    R.string.param_desc_cash_withdrawal_approve_transaction
                else ->
                    R.string.param_desc_transfer_approve_transaction
            },
            name
        )
        approvalConfirmationBottomSheet = ConfirmationBottomSheet.newInstance(
            R.drawable.ic_check_approval,
            getString(R.string.title_approve_transaction),
            desc,
            getString(R.string.action_approve_default),
            getString(R.string.action_cancel),
            null,
            null,
            R.drawable.circle_green
        )
        approvalConfirmationBottomSheet?.setOnConfirmationPageCallBack(
            object : OnConfirmationPageCallBack {

                override fun onClickPositiveButtonDialog(data: String?, tag: String?) {
                    approvalConfirmationBottomSheet?.dismiss()
                    onApproveTransaction()
                }

                override fun onClickNegativeButtonDialog(data: String?, tag: String?) {
                    approvalConfirmationBottomSheet?.dismiss()
                }

            })
        approvalConfirmationBottomSheet?.isCancelable = true
        approvalConfirmationBottomSheet?.show(
            supportFragmentManager,
            this::class.java.simpleName
        )
    }

    private fun onApproveTransaction() {
        if (approvalDetail.channel == ChannelBankEnum.CHECK_WRITER.value) {
            val checkWriterBatchIds = mutableListOf<String>()
            checkWriterBatchIds.add(approvalDetail.id.notNullable())
            viewModel.approval(
                approvalType = REQUEST_APPROVE,
                batchIds = mutableListOf(),
                checkWriterBatchIds = checkWriterBatchIds
            )
        } else {
            val batchIds = mutableListOf<String>()
            batchIds.add(approvalDetail.id.notNullable())
            viewModel.approval(
                approvalType = REQUEST_APPROVE,
                batchIds = batchIds,
                checkWriterBatchIds = mutableListOf()
            )
        }
    }

    private fun initEventBus() {
        eventBus.actionSyncEvent.flowable.subscribe {
            if (it.eventType == ActionSyncEvent.ACTION_UPDATE_SCHEDULED_TRANSFER_LIST) {
                getDetailsBasedOnChannel()
            }
        }.addTo(disposables)
    }

    private fun initDataBus() {
        dataBus.approvalBatchIdDataBus.flowable.subscribe {
            val badgeCountSharedApprovalsPref = sharedPreferenceUtil.badgeCountApprovalsSharedPref()
            badgeCountSharedApprovalsPref.set(
                badgeCountSharedApprovalsPref.get().minus(it.batchIds.size)
            )
            generalViewModel.updateShortCutBadgeCount()
            MaterialDialog(this).show {
                lifecycleOwner(this@ApprovalDetailActivity)
                title(R.string.title_success)
                message(
                    text = formatString(
                        if (it.approvalType == REQUEST_APPROVE)
                            R.string.msg_success_approval
                        else
                            R.string.msg_rejected_approval
                    )
                )
                positiveButton(
                    res = R.string.action_ok,
                    click = {
                        it.dismiss()
                        getDetailsBasedOnChannel()
                    }
                )
            }
        }.addTo(disposables)
    }

    private fun onClickedHelp() {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) return
        mLastClickTime = SystemClock.elapsedRealtime()
        if (binding.viewLoadingStateInitial.root.visibility != View.VISIBLE &&
            binding.viewLoadingState.root.visibility != View.VISIBLE
        ) {
            binding.nestedScrollView.fullScroll(NestedScrollView.FOCUS_UP)
            Handler().postDelayed(
                {
                    startTutorial()
                },
                resources.getInteger(R.integer.time_delay_smooth_scroll).toLong()
            )
        }
    }

    private fun initViewDetails(isShownButtons: Boolean) {
        binding.viewHeader.textViewTitle.text = getString(R.string.title_approval_hierarchy)
        binding.viewTransactionDetails.buttonApprove.visibility(isShownButtons)
        binding.viewTransactionDetails.buttonReject.visibility(isShownButtons)
        binding.viewTransactionDetails.viewBorderStatus.visibility(isShownButtons)
        if ((approvalDetail.immediate == false &&
                    approvalDetail.transactionStatus?.type == Constant.STATUS_SCHEDULED) ||
            (approvalDetail.maker == true &&
                    approvalDetail.transactionStatus?.type == Constant.STATUS_FOR_APPROVAL)
        ) {
            binding.buttonDelete.visibility = View.VISIBLE
            val constraintSet = ConstraintSet()
            constraintSet.clone(binding.constraintLayoutContent)
            constraintSet.connect(
                binding.buttonViewLogs.id,
                ConstraintSet.BOTTOM,
                binding.buttonDelete.id,
                ConstraintSet.TOP
            )
            constraintSet.applyTo(binding.constraintLayoutContent)
        } else {
            binding.buttonDelete.visibility = View.GONE
        }
        binding.viewTransactionDetails.textViewStatus.text =
            if (approvalDetail.transactionStatus?.type == Constant.STATUS_FOR_PROCESSING ||
                approvalDetail.transactionStatus?.type == Constant.STATUS_SUCCESSFUL ||
                approvalDetail.transactionStatus?.type == Constant.STATUS_SUCCESS ||
                approvalDetail.transactionStatus?.type == Constant.STATUS_REJECTED ||
                approvalDetail.transactionStatus?.type == Constant.STATUS_FAILED ||
                approvalDetail.transactionStatus?.type == Constant.STATUS_CANCELLED
            ) {
                approvalDetail.transactionStatus?.let {
                    ("<b>${it.detailedDescription}</b><br>${it.remarks.notEmpty()}").toHtmlSpan()
                }
            } else {
                "<b>${approvalDetail.transactionStatus?.description.notEmpty()}</b>".toHtmlSpan()
            }
        binding.viewTransactionDetails.textViewStatus.setTextColor(
            ContextCompat.getColor(
                this,
                ConstantHelper.Color.getTextColor(approvalDetail.transactionStatus)
            )
        )
        binding.viewTransactionDetails.textViewRemarks.text = if (approvalDetail.batchType == Constant.TYPE_BATCH) {
            if (approvalDetail.remarks == "" || approvalDetail.remarks == null)
                approvalDetail.fileName
            else
                approvalDetail.remarks
        } else {
            ConstantHelper.Text.getRemarks(this, approvalDetail)
        }
        binding.viewTransactionDetails.textViewCreatedBy.text = viewUtil.getStringOrEmpty(approvalDetail.createdBy)
        binding.viewTransactionDetails.textViewCreatedDate.text = viewUtil.getStringOrEmpty(
            viewUtil.getDateFormatByDateString(
                approvalDetail.createdDate,
                ViewUtil.DATE_FORMAT_ISO_WITHOUT_T,
                ViewUtil.DATE_FORMAT_DEFAULT
            )
        )

        binding.viewTransactionDetails.textViewSourceAccount.text =
            Html.fromHtml(
                String.format(
                    getString(R.string.params_account_detail),
                    approvalDetail.sourceAccountName,
                    viewUtil.getAccountNumberFormat(approvalDetail.sourceAccountNumber),
                    viewUtil.getStringOrEmpty(approvalDetail.sourceAccountType)
                )
            )

        if (approvalDetail.channel.equals(ChannelBankEnum.CHECK_WRITER.value, true)) {
            binding.viewTransactionDetails.textViewHeaderTransferTo.text = formatString(R.string.title_payee)
            binding.viewTransactionDetails.textViewHeaderPaymentTo.visibility(false)
            binding.viewTransactionDetails.cardViewPaymentTo.visibility(false)
            binding.viewTransactionDetails.textViewHeaderBankDetails.visibility(false)
            binding.viewTransactionDetails.cardViewBankDetails.visibility(false)
            binding.viewTransactionDetails.viewBorderPurpose.visibility(false)
            binding.viewTransactionDetails.textViewPurposeTitle.visibility(false)
            binding.viewTransactionDetails.textViewPurpose.visibility(false)
            binding.viewTransactionDetails.textViewBeneficiaryCodeTitle.visibility(false)
            binding.viewTransactionDetails.textViewBeneficiaryCode.visibility(false)
            binding.viewTransactionDetails.viewBorderBeneficiaryName.visibility(false)
            binding.viewTransactionDetails.viewBorderBeneficiaryAddress.visibility(false)
            binding.viewTransactionDetails.textViewBeneficiaryAddressTitle.visibility(false)
            binding.viewTransactionDetails.textViewBeneficiaryAddress.visibility(false)
            binding.viewTransactionDetails.viewBorderAccountNumber.visibility(false)
            binding.viewShareButton.root.visibility(false)
            binding.buttonViewAllTransaction.visibility(false)
            binding.buttonDelete.visibility(false)
            binding.viewTransactionDetails.viewBorderProposedTransactionDate.visibility(false)
            binding.viewTransactionDetails.textViewProposedTransactionDateTitle.visibility(false)
            binding.viewTransactionDetails.textViewProposedTransactionDate.visibility(false)
            binding.viewTransactionDetails.viewBorderCurrency.visibility(true)
            binding.viewTransactionDetails.textViewCurrencyTitle.visibility(true)
            binding.viewTransactionDetails.textViewCurrency.visibility(true)
            binding.viewTransactionDetails.viewBorderCheckNumber.visibility(true)
            binding.viewTransactionDetails.textViewCheckNumberTitle.visibility(true)
            binding.viewTransactionDetails.textViewCheckNumber.visibility(true)
            binding.viewTransactionDetails.viewBorderCheckDate.visibility(true)
            binding.viewTransactionDetails.textViewCheckDateTitle.visibility(true)
            binding.viewTransactionDetails.textViewCheckDate.visibility(true)
            binding.viewTransactionDetails.viewBorderCWTAmount.visibility(false)
            binding.viewTransactionDetails.textViewCWTAmountTitle.visibility(false)
            binding.viewTransactionDetails.textViewCWTAmount.visibility(false)
            binding.viewTransactionDetails.viewBorderPrintingType.visibility(true)
            binding.viewTransactionDetails.textViewPrintingTypeTitle.visibility(true)
            binding.viewTransactionDetails.textViewPrintingType.visibility(true)
            binding.viewTransactionDetails.viewBorderReferenceNumber.visibility(approvalDetail.transactionReferenceNumber != null)
            binding.viewTransactionDetails.textViewReferenceNumberTitle.visibility(approvalDetail.transactionReferenceNumber != null)
            binding.viewTransactionDetails.textViewReferenceNumber.visibility(approvalDetail.transactionReferenceNumber != null)
            if (approvalDetail.batchType == Constant.TYPE_BATCH) {
                binding.viewTransactionDetails.textViewBeneficiaryNameTitle.visibility(false)
                binding.viewTransactionDetails.textViewBeneficiaryName.visibility(false)
                binding.viewTransactionDetails.textViewBeneficiaryAccountNumberTitle.visibility(true)
                binding.viewTransactionDetails.textViewBeneficiaryAccountNumber.visibility(true)
                binding.viewTransactionDetails.textViewBeneficiaryAccountNumberTitle.text =
                    formatString(R.string.title_number_of_checks)
                binding.viewTransactionDetails.textViewCheckNumberTitle.text = formatString(R.string.title_file_name)
                binding.viewTransactionDetails.textViewCheckNumber.text = approvalDetail.fileName
            } else {
                binding.viewTransactionDetails.textViewBeneficiaryNameTitle.visibility(true)
                binding.viewTransactionDetails.textViewBeneficiaryName.visibility(true)
                binding.viewTransactionDetails.textViewBeneficiaryAccountNumberTitle.visibility(false)
                binding.viewTransactionDetails.textViewBeneficiaryAccountNumber.visibility(false)
                binding.viewTransactionDetails.textViewCheckNumber.text = approvalDetail.checkNumber.notEmpty()
            }
            binding.viewTransactionDetails.textViewBeneficiaryNameTitle.text = formatString(R.string.title_payee_name)
            binding.viewTransactionDetails.textViewBeneficiaryName.text = approvalDetail.payeeName
            binding.viewTransactionDetails.textViewCurrency.text = approvalDetail.currency
            binding.viewTransactionDetails.textViewCheckDate.text =
                approvalDetail.checkDate.convertDateToDesireFormat(DateFormatEnum.DATE_FORMAT_DATE)
            binding.viewTransactionDetails.textViewPrintingType.text = approvalDetail.printingType
        } else if (approvalDetail.channel.equals(ChannelBankEnum.CASH_WITHDRAWAL.name, true)) {
            binding.viewTransactionDetails.viewCashWithdrawalDetails.root.visibility(true)
            binding.viewTransactionDetails.textViewHeaderPaymentTo.visibility(false)
            binding.viewTransactionDetails.cardViewPaymentTo.visibility(false)
            binding.viewTransactionDetails.textViewHeaderBankDetails.visibility(false)
            binding.viewTransactionDetails.cardViewBankDetails.visibility(false)
            binding.viewTransactionDetails.textViewHeaderTransferTo.visibility(false)
            binding.viewTransactionDetails.cardViewTransferTo.visibility(false)
            binding.viewShareButton.root.isVisible = false
            binding.buttonViewLogs.isVisible = false
            binding.viewTransactionDetails.viewCashWithdrawalDetails.root.isVisible = true
            binding.viewTransactionDetails.textViewHeaderTransactionDetail.isVisible = false
            binding.viewTransactionDetails.cardViewTransactionDetails.isVisible = false
            binding.viewTransactionDetails.textViewHeaderTransferFrom.text = formatString(R.string.title_withdrawal_from)
            binding.viewTransactionDetails.viewCashWithdrawalDetails.tvCashTransactionDate.text =
                approvalDetail.branchTransactionDate.convertDateToDesireFormat(DateFormatEnum.DATE_FORMAT_DATE)
            binding.viewTransactionDetails.viewCashWithdrawalDetails.tvCashBranchName.text = approvalDetail.branchName
            binding.viewTransactionDetails.viewCashWithdrawalDetails.tvCashBranchAddress.text = approvalDetail.branchAddress
            binding.viewTransactionDetails.viewCashWithdrawalDetails.tvCashAmount.text = approvalDetail.totalAmount.formatAmount(approvalDetail.currency)
            initServiceFee(
                binding.viewTransactionDetails.viewCashWithdrawalDetails.tvCashServiceFee,
                binding.viewTransactionDetails.viewCashWithdrawalDetails.tvCashDiscountServiceFee,
                binding.viewTransactionDetails.viewCashWithdrawalDetails.viewCashDiscountServiceFee
            )
            binding.viewTransactionDetails.viewCashWithdrawalDetails.tvCashRepresentativeName.text = approvalDetail.representativeName
            binding.viewTransactionDetails.viewCashWithdrawalDetails.tvCashChannel.text = ChannelBankEnum.CASH_WITHDRAWAL.value
            binding.viewTransactionDetails.viewCashWithdrawalDetails.tvCashReferenceNumber.text = approvalDetail.transactionReferenceId.notEmpty()
        } else if (approvalDetail.channel.equals(ChannelBankEnum.BILLS_PAYMENT.value, true)) {
            binding.viewTransactionDetails.textViewHeaderTransferFrom.text = formatString(R.string.title_payment_from)
            binding.viewTransactionDetails.textViewProposedTransactionDateTitle.text =
                formatString(R.string.title_proposed_payment_date)
            binding.viewTransactionDetails.textViewHeaderTransferTo.visibility(false)
            binding.viewTransactionDetails.cardViewTransferTo.visibility(false)
            binding.viewTransactionDetails.textViewHeaderBankDetails.visibility(false)
            binding.viewTransactionDetails.cardViewBankDetails.visibility(false)
            binding.viewTransactionDetails.viewBorderPurpose.visibility(false)
            binding.viewTransactionDetails.textViewPurposeTitle.visibility(false)
            binding.viewTransactionDetails.textViewPurpose.visibility(false)
            binding.viewTransactionDetails.textViewBiller.text = approvalDetail.billerName.notEmpty()
            binding.viewTransactionDetails.linearLayoutBillerFields.removeAllViews()
            approvalDetail.fields?.sortedWith(compareBy { it.index })?.forEach {
                val viewFields = layoutInflater.inflate(R.layout.item_textview_biller, null)
                val textViewTitle = viewFields.findViewById<TextView>(R.id.textViewTitle)
                val textView = viewFields.findViewById<TextView>(R.id.tvReferenceNo)
                textViewTitle.text = it.name
                textView.text = it.value
                if (viewUtil.isValidDateFormat(ViewUtil.DATE_FORMAT_DATE_SLASH, it.value) &&
                    it.name.equals("Return Period", true)
                ) {
                    textView.text = viewUtil.getDateFormatByDateString(
                        it.value,
                        ViewUtil.DATE_FORMAT_DATE_SLASH,
                        ViewUtil.DATE_FORMAT_DATE
                    )
                } else {
                    textView.text = it.value.notEmpty()
                }
                binding.viewTransactionDetails.linearLayoutBillerFields?.addView(viewFields)
            }
        } else if (approvalDetail.channel.equals(ChannelBankEnum.UBP_TO_UBP.value, true) ||
            approvalDetail.channel.equals(ChannelBankEnum.PESONET.value, true) ||
            approvalDetail.channel.equals(ChannelBankEnum.PDDTS.value, true) ||
            approvalDetail.channel.equals(ChannelBankEnum.INSTAPAY.value, true) ||
            approvalDetail.channel.equals(ChannelBankEnum.SWIFT.value, true)
        ) {
            if (approvalDetail.channel.equals(ChannelBankEnum.UBP_TO_UBP.value, true)) {
                binding.viewTransactionDetails.textViewHeaderBankDetails.visibility(false)
                binding.viewTransactionDetails.cardViewBankDetails.visibility(false)
                binding.viewTransactionDetails.viewBorderPurpose.visibility(false)
                binding.viewTransactionDetails.textViewPurposeTitle.visibility(false)
                binding.viewTransactionDetails.textViewPurpose.visibility(false)
                binding.viewTransactionDetails.textViewBeneficiaryCodeTitle.visibility(false)
                binding.viewTransactionDetails.textViewBeneficiaryCode.visibility(false)
                binding.viewTransactionDetails.viewBorderBeneficiaryName.visibility(false)
                binding.viewTransactionDetails.textViewBeneficiaryNameTitle.visibility(false)
                binding.viewTransactionDetails.textViewBeneficiaryName.visibility(false)
                binding.viewTransactionDetails.viewBorderBeneficiaryAddress.visibility(false)
                binding.viewTransactionDetails.textViewBeneficiaryAddressTitle.visibility(false)
                binding.viewTransactionDetails.textViewBeneficiaryAddress.visibility(false)
                binding.viewTransactionDetails.viewBorderAccountNumber.visibility(false)
            } else if (approvalDetail.channel.equals(ChannelBankEnum.SWIFT.value, true)) {
                binding.viewTransactionDetails.viewBorderReceivingBankName.visibility(true)
                binding.viewTransactionDetails.textViewSwiftCodeTitle.visibility(true)
                binding.viewTransactionDetails.textViewSwiftCode.visibility(true)
                binding.viewTransactionDetails.viewBorderReceivingBankAddress.visibility(true)
                binding.viewTransactionDetails.textViewReceivingBankAddressTitle.visibility(true)
                binding.viewTransactionDetails.textViewReceivingBankAddress.visibility(true)
            }
            binding.viewTransactionDetails.textViewHeaderPaymentTo.visibility(false)
            binding.viewTransactionDetails.cardViewPaymentTo.visibility(false)
            binding.viewTransactionDetails.textViewHeaderTransferFrom.text = formatString(R.string.title_transfer_from)
            binding.viewTransactionDetails.textViewProposedTransactionDateTitle.text =
                formatString(R.string.title_transaction_date)
            binding.viewTransactionDetails.textViewBeneficiaryCode.text = viewUtil.getStringOrEmpty(approvalDetail.beneficiaryCode)
            binding.viewTransactionDetails.textViewBeneficiaryName.text = viewUtil.getStringOrEmpty(approvalDetail.beneficiaryName)
            binding.viewTransactionDetails.textViewBeneficiaryAddress.text =
                viewUtil.getStringOrEmpty(approvalDetail.beneficiaryAddress)
            binding.viewTransactionDetails.textViewBeneficiaryAccountNumber.text =
                viewUtil.getAccountNumberFormat(approvalDetail.destinationAccountNumber)
            binding.viewTransactionDetails.textViewSwiftCode.text = viewUtil.getStringOrEmpty(approvalDetail.swiftCode)
            binding.viewTransactionDetails.textViewReceivingBankName.text = viewUtil.getStringOrEmpty(approvalDetail.receivingBank)
            binding.viewTransactionDetails.textViewReceivingBankAddress.text =
                viewUtil.getStringOrEmpty(approvalDetail.bankAddress)
            binding.viewTransactionDetails.textViewPurpose.text = viewUtil.getStringOrEmpty(approvalDetail.purpose)
        }

        binding.viewTransactionDetails.textViewAmount.text = viewUtil.getStringOrEmpty(
            autoFormatUtil.formatWithTwoDecimalPlaces(
                approvalDetail.totalAmount,
                approvalDetail.currency
            )
        )
        binding.viewTransactionDetails.textViewServiceFeeTitle.text = if (approvalDetail.batchType == Constant.TYPE_BATCH) {
            getString(R.string.title_total_service_fee)
        } else {
            getString(R.string.title_service_fee)
        }
        initServiceFee(
            binding.viewTransactionDetails.textViewServiceFee,
            binding.viewTransactionDetails.textViewServiceDiscountFee,
            binding.viewTransactionDetails.viewBorderServiceDiscountFee
        )
        binding.viewTransactionDetails.textViewProposedTransactionDate.text =
            if (approvalDetail.immediate == true)
                getString(R.string.title_immediately)
            else
                viewUtil.findDateFormatByDateString(
                    if (approvalDetail.immediate == true)
                        approvalDetail.proposedTransactionDate ?: approvalDetail.startDate
                    else
                        approvalDetail.startDate ?: approvalDetail.proposedTransactionDate,
                    ViewUtil.DATE_FORMAT_DEFAULT
                )
        if (approvalDetail.immediate == false) {
            if (approvalDetail.endDate == null || approvalDetail.numberOfOccurrences.notNullable() == 1) {
                binding.viewTransactionDetails.viewBorderEndDate.visibility(false)
                binding.viewTransactionDetails.textViewEndDateTitle.visibility(false)
                binding.viewTransactionDetails.textViewEndDate.visibility(false)
            } else {
                binding.viewTransactionDetails.textViewProposedTransactionDateTitle.text =
                    getString(R.string.title_transaction_date)
            }
            binding.viewTransactionDetails.textViewFrequency.text = viewUtil.getStringOrEmpty(approvalDetail.frequency)
            if (approvalDetail.numberOfOccurrences.notNullable() > 1) {
                binding.viewTransactionDetails.textViewProposedTransactionDateTitle.text = formatString(R.string.title_start_date)
                binding.viewTransactionDetails.textViewEndDate.text =
                    ("After ${approvalDetail.numberOfOccurrences} occurrences" +
                            "\n" +
                            "(Until " +
                            "${
                            approvalDetail.endDate.convertDateToDesireFormat(
                                DateFormatEnum.DATE_FORMAT_DATE
                            )
                            })")
            }
        } else {
            binding.viewTransactionDetails.viewBorderFrequency.visibility(false)
            binding.viewTransactionDetails.textViewFrequencyTitle.visibility(false)
            binding.viewTransactionDetails.textViewFrequency.visibility(false)
            binding.viewTransactionDetails.viewBorderEndDate.visibility(false)
            binding.viewTransactionDetails.textViewEndDateTitle.visibility(false)
            binding.viewTransactionDetails.textViewEndDate.visibility(false)
        }
        binding.viewTransactionDetails.textViewChannel.text = approvalDetail.channel
        if (approvalDetail.batchType == Constant.TYPE_BATCH) {
            binding.viewTransactionDetails.textViewHeaderBankDetails.visibility(false)
            binding.viewTransactionDetails.cardViewBankDetails.visibility(false)
            binding.viewTransactionDetails.textViewBeneficiaryCodeTitle.visibility(false)
            binding.viewTransactionDetails.textViewBeneficiaryCode.visibility(false)
            binding.viewTransactionDetails.textViewBeneficiaryNameTitle.visibility(false)
            binding.viewTransactionDetails.textViewBeneficiaryName.visibility(false)
            binding.viewTransactionDetails.textViewBeneficiaryAddressTitle.visibility(false)
            binding.viewTransactionDetails.textViewBeneficiaryAddress.visibility(false)
            binding.viewTransactionDetails.viewBorderBeneficiaryName.visibility(false)
            binding.viewTransactionDetails.viewBorderBeneficiaryAddress.visibility(false)
            binding.viewTransactionDetails.viewBorderAccountNumber.visibility(false)
            binding.viewTransactionDetails.viewBorderPurpose.visibility(false)
            binding.viewTransactionDetails.textViewPurposeTitle.visibility(false)
            binding.viewTransactionDetails.textViewPurpose.visibility(false)
            binding.viewTransactionDetails.textViewBeneficiaryAccountNumberTitle.text =
                getString(R.string.title_number_of_transfers)
            binding.viewTransactionDetails.textViewBeneficiaryAccountNumber.setTextColor(
                ContextCompat.getColor(this, getAccentColor())
            )
            binding.viewTransactionDetails.textViewBeneficiaryAccountNumber.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_batch_orange,
                0,
                0,
                0
            )
            val constraintSet = ConstraintSet()
            constraintSet.clone(binding.viewTransactionDetails.constraintLayoutTransferTo)
            constraintSet.clear(R.id.textViewBeneficiaryAccountNumber, ConstraintSet.START)
            constraintSet.applyTo(binding.viewTransactionDetails.constraintLayoutTransferTo)
            binding.zoomLayout.setVisible(true)
            binding.viewTransactionDetails.textViewBeneficiaryAccountNumber.text =
                viewUtil.getStringOrEmpty(approvalDetail.numberOfTransactions)
            if (!approvalDetail.channel.equals(ChannelBankEnum.CHECK_WRITER.value, true)) {
                binding.buttonViewAllTransaction.visibility(true)
                binding.viewTransactionDetails.constraintLayoutTransferTo.setOnClickListener {
                    navigateBatchTransfer()
                }
            }
        } else {
            binding.viewTransactionDetails.constraintLayoutTransferTo.setOnClickListener(null)
        }
        if (approvalDetail.transactionReferenceId != null) {
            if (approvalDetail.transactionStatus?.description == Constant.TRANSFER_SUCCESSFUL ||
                approvalDetail.transactionStatus?.description == Constant.PAYMENT_SUCCESSFUL ||
                approvalDetail.transactionStatus?.description == Constant.FULLY_APPROVED ||
                approvalDetail.transactionStatus?.type == Constant.STATUS_FOR_PROCESSING ||
                approvalDetail.transactionStatus?.type == Constant.STATUS_SUCCESS
            ) {
                binding.viewTransactionDetails.textViewReferenceNumber.text = approvalDetail.transactionReferenceId
            } else {
                binding.viewTransactionDetails.viewBorderReferenceNumber.visibility(false)
                binding.viewTransactionDetails.textViewReferenceNumberTitle.visibility(false)
                binding.viewTransactionDetails.textViewReferenceNumber.visibility(false)
            }
        } else {
            binding.viewTransactionDetails.viewBorderReferenceNumber.visibility(false)
            binding.viewTransactionDetails.textViewReferenceNumberTitle.visibility(false)
            binding.viewTransactionDetails.textViewReferenceNumber.visibility(false)
        }
    }

    private fun initServiceFee(
        tvServiceFee: TextView,
        tvServiceDiscountFee: TextView,
        viewServiceDiscountFee: View
    ) {
        if (approvalDetail.customServiceFee != null &&
            approvalDetail.serviceFee != null
        ) {
            if (0.00 >= approvalDetail.customServiceFee?.value?.toDouble() ?: 0.00) {
                tvServiceFee.text = getString(R.string.value_service_fee_free)
            } else {
                tvServiceFee.text = formatString(
                    R.string.value_service,
                    autoFormatUtil.formatWithTwoDecimalPlaces(
                        approvalDetail.customServiceFee?.value,
                        approvalDetail.customServiceFee?.currency
                    )
                )
            }
            tvServiceDiscountFee.visibility(true)
            viewServiceDiscountFee.visibility(true)
        } else {
            tvServiceDiscountFee.visibility(false)
            viewServiceDiscountFee.visibility(false)
        }
        if (approvalDetail.serviceFee != null && approvalDetail.serviceFee?.value != "0") {
            if (approvalDetail.customServiceFee != null) {
                tvServiceDiscountFee.text = formatString(
                    R.string.value_service,
                    autoFormatUtil.formatWithTwoDecimalPlaces(
                        approvalDetail.serviceFee?.value,
                        approvalDetail.serviceFee?.currency
                    )
                )
            } else {
                tvServiceFee.text = formatString(
                    R.string.value_service,
                    autoFormatUtil.formatWithTwoDecimalPlaces(
                        approvalDetail.serviceFee?.value,
                        approvalDetail.serviceFee?.currency
                    )
                )
            }
        } else {
            tvServiceFee.text = getString(R.string.value_service_fee_free)
        }
    }

    private fun initViewShareDetails(transaction: Transaction) {
        binding.viewShareDetails.textViewShareTransferFrom.text = formatString(
            R.string.params_account_detail,
            transaction.sourceAccountName,
            viewUtil.getAccountNumberFormat(transaction.sourceAccountNumber),
            transaction.sourceAccountType.notEmpty()
        ).toHtmlSpan()

        if (transaction.batchType == Constant.TYPE_BATCH) {
            binding.viewShareDetails.textViewShareAmountTitle.text = formatString(R.string.title_total_amount)
            binding.viewShareDetails.textViewShareServiceFeeTitle.text = formatString(R.string.title_total_service_fee)
            binding.viewShareDetails.textViewShareTransferToTitle.text = formatString(R.string.title_number_of_transfers)
        }

        when (transaction.channel) {
            ChannelBankEnum.UBP_TO_UBP.value -> {
                binding.viewShareDetails.textViewShareReceivingBankTitle.visibility(false)
                binding.viewShareDetails.textViewShareReceivingBank.visibility(false)
                binding.viewShareDetails.viewShareBorderPurpose.visibility(false)
                binding.viewShareDetails.textViewSharePurposeTitle.visibility(false)
                binding.viewShareDetails.textViewSharePurpose.visibility(false)
                binding.viewShareDetails.textViewShareTransferTo.text = if (transaction.batchType == Constant.TYPE_BATCH) {
                    transaction.numberOfTransactions.toString().notEmpty()
                } else {
                    viewUtil.getAccountNumberFormat(transaction.destinationAccountNumber)
                }
            }
            ChannelBankEnum.BILLS_PAYMENT.value -> {
                binding.viewShareDetails.textViewShareReceivingBankTitle.visibility(false)
                binding.viewShareDetails.textViewShareReceivingBank.visibility(false)
                binding.viewShareDetails.viewShareBorderPurpose.visibility(false)
                binding.viewShareDetails.textViewSharePurposeTitle.visibility(false)
                binding.viewShareDetails.textViewSharePurpose.visibility(false)
                binding.viewShareDetails.textViewShareProposedTransferDateTitle.text =
                    formatString(R.string.title_proposed_payment_date)
                val stringBuilder = StringBuilder()
                transaction.fields
                    ?.sortedWith(compareBy { it.index })
                    ?.forEachIndexed { index, reference ->
                        if (viewUtil.isValidDateFormat(
                                ViewUtil.DATE_FORMAT_DATE_SLASH,
                                reference.value
                            )
                            && reference.name.equals("Return Period", true)
                        ) {
                            stringBuilder.append(
                                viewUtil.getDateFormatByDateString(
                                    reference.value,
                                    ViewUtil.DATE_FORMAT_DATE_SLASH,
                                    ViewUtil.DATE_FORMAT_DATE
                                )
                            )
                        } else {
                            stringBuilder.append(reference.value)
                        }
                        stringBuilder.append("<br>")
                        stringBuilder.append(
                            String.format(
                                getString(R.string.format_change_color),
                                reference.name
                            )
                        )
                        if (index != transaction.fields?.size?.minus(1)) {
                            stringBuilder.append("<br>")
                            stringBuilder.append("<br>")
                        }
                    }

                binding.viewShareDetails.textViewShareTransferTo.text =
                    Html.fromHtml(
                        transaction.billerName +
                                "<br>" +
                                "<br>" +
                                stringBuilder.toString()
                    )
            }
            else -> {
                binding.viewShareDetails.textViewShareTransferTo.text = if (transaction.batchType == Constant.TYPE_BATCH) {
                    transaction.numberOfTransactions.toString().notEmpty()
                } else {
                    formatString(
                        R.string.params_two_format,
                        transaction.beneficiaryName,
                        viewUtil.getAccountNumberFormat(transaction.destinationAccountNumber)
                    ).toHtmlSpan()
                }
                if (transaction.channel == ChannelBankEnum.SWIFT.value) {
                    binding.viewShareDetails.textViewShareReceivingBank.text = formatString(
                        R.string.params_account_detail,
                        transaction.swiftCode,
                        transaction.receivingBank,
                        transaction.bankAddress
                    ).toHtmlSpan()
                } else {
                    binding.viewShareDetails.textViewShareReceivingBank.text = transaction.receivingBank.notEmpty()
                }
            }
        }
        if (approvalDetail.batchType == Constant.TYPE_BATCH) {
            binding.viewShareDetails.textViewShareReceivingBankTitle.visibility(false)
            binding.viewShareDetails.textViewShareReceivingBank.visibility(false)
            binding.viewShareDetails.viewShareBorderPurpose.visibility(false)
            binding.viewShareDetails.textViewSharePurposeTitle.visibility(false)
            binding.viewShareDetails.textViewSharePurpose.visibility(false)
        }
        binding.viewShareDetails.textViewShareAmount.text =
            AutoFormatUtil().formatWithTwoDecimalPlaces(
                transaction.totalAmount,
                transaction.currency
            )

        if (transaction.customServiceFee != null &&
            transaction.serviceFee != null
        ) {
            if (0.00 >= transaction.customServiceFee?.value?.toDouble() ?: 0.00) {
                binding.viewShareDetails.textViewShareServiceFee.text = getString(R.string.value_service_fee_free)
            } else {
                binding.viewShareDetails.textViewShareServiceFee.text = formatString(
                    R.string.value_service,
                    autoFormatUtil.formatWithTwoDecimalPlaces(
                        transaction.customServiceFee?.value,
                        transaction.customServiceFee?.currency
                    )
                )
            }
            binding.viewShareDetails.textViewShareServiceDiscountFee.visibility(true)
            binding.viewShareDetails.viewBorderShareServiceDiscountFee.visibility(true)
        } else {
            binding.viewShareDetails.textViewShareServiceDiscountFee.visibility(false)
            binding.viewShareDetails.viewBorderShareServiceDiscountFee.visibility(false)
        }
        if (transaction.serviceFee != null) {
            if (transaction.customServiceFee != null) {
                binding.viewShareDetails.textViewShareServiceDiscountFee.text = formatString(
                    R.string.value_service,
                    autoFormatUtil.formatWithTwoDecimalPlaces(
                        transaction.serviceFee?.value,
                        transaction.serviceFee?.currency
                    )
                )
            } else {
                binding.viewShareDetails.textViewShareServiceFee.text = formatString(
                    R.string.value_service,
                    autoFormatUtil.formatWithTwoDecimalPlaces(
                        transaction.serviceFee?.value,
                        transaction.serviceFee?.currency
                    )
                )
            }
        } else {
            binding.viewShareDetails.textViewShareServiceFee.text = getString(R.string.value_service_fee_free)
        }

        binding.viewShareDetails.textViewShareChannel.text = transaction.channel.notEmpty()
        binding.viewShareDetails.textViewSharePurpose.text = transaction.purpose.notEmpty()
        binding.viewShareDetails.textViewShareRemarks.text = if (approvalDetail.batchType == Constant.TYPE_BATCH) {
            if (approvalDetail.remarks == "" || approvalDetail.remarks == null)
                approvalDetail.fileName
            else
                approvalDetail.remarks
        } else {
            approvalDetail.remarks.notEmpty()
        }
        binding.viewShareDetails.textViewShareProposedTransferDate.text =
            if (transaction.immediate == true)
                getString(R.string.title_immediately)
            else
                viewUtil.getDateFormatByDateString(
                    transaction.startDate,
                    ViewUtil.DATE_FORMAT_ISO_WITHOUT_T,
                    ViewUtil.DATE_FORMAT_DEFAULT
                )
        if (transaction.immediate == true) {
            binding.viewShareDetails.textViewShareStartDateTitle.visibility = View.GONE
            binding.viewShareDetails.textViewShareStartDate.visibility = View.GONE
            binding.viewShareDetails.viewBorderShareStartDate.visibility = View.GONE
            binding.viewShareDetails.textViewShareFrequencyTitle.visibility = View.GONE
            binding.viewShareDetails.textViewShareFrequency.visibility = View.GONE
            binding.viewShareDetails.viewBorderShareFrequency.visibility = View.GONE
            binding.viewShareDetails.textViewShareEndDateTitle.visibility = View.GONE
            binding.viewShareDetails.textViewShareEndDate.visibility = View.GONE
            binding.viewShareDetails.viewBorderShareEndDate.visibility = View.GONE
            binding.viewShareDetails.textViewShareProposedTransferDateTitle.visibility = View.VISIBLE
            binding.viewShareDetails.textViewShareProposedTransferDate.visibility = View.VISIBLE
            binding.viewShareDetails.viewBorderShareProposedTransferDate.visibility = View.VISIBLE
        } else {
            binding.viewShareDetails.textViewShareStartDate.text =
                viewUtil.getDateFormatByDateString(
                    transaction.startDate,
                    ViewUtil.DATE_FORMAT_ISO_WITHOUT_T,
                    ViewUtil.DATE_FORMAT_DEFAULT
                )
            if (transaction.endDate != null) {
                if (approvalDetail.numberOfOccurrences.notNullable() > 1) {
                    binding.viewShareDetails.textViewShareEndDate.text =
                        ("After ${approvalDetail.numberOfOccurrences} occurrences" +
                                "\n" +
                                "(Until " +
                                "${
                                approvalDetail.endDate.convertDateToDesireFormat(
                                    DateFormatEnum.DATE_FORMAT_DATE
                                )
                                })")
                }
            }
            binding.viewShareDetails.textViewShareFrequency.text = transaction.frequency
            if (transaction.frequency == getString(R.string.title_one_time)) {
                binding.viewShareDetails.textViewShareEndDate.visibility = View.GONE
                binding.viewShareDetails.textViewShareEndDateTitle.visibility = View.GONE
                binding.viewShareDetails.viewBorderShareEndDate.visibility = View.GONE
                binding.viewShareDetails.textViewShareStartDateTitle.visibility = View.GONE
                binding.viewShareDetails.textViewShareStartDate.visibility = View.GONE
                binding.viewShareDetails.viewBorderShareStartDate.visibility = View.GONE
            } else {
                binding.viewShareDetails.textViewShareProposedTransferDateTitle.visibility = View.GONE
                binding.viewShareDetails.textViewShareProposedTransferDate.visibility = View.GONE
                binding.viewShareDetails.viewBorderShareProposedTransferDate.visibility = View.GONE
            }
        }

        binding.viewShareDetails.textViewShareCreatedBy.text = transaction.createdBy
        binding.viewShareDetails.textViewShareCreatedOn.text = viewUtil.getDateFormatByDateString(
            transaction.createdDate,
            DateFormatEnum.DATE_FORMAT_ISO_WITHOUT_T.value,
            DateFormatEnum.DATE_FORMAT_DEFAULT.value
        )
        binding.viewShareDetails.textViewShareDateDownloaded.text = viewUtil.getCurrentDateString()

        initShareDetailStatus(transaction)
    }

    private fun initShareDetailStatus(transaction: Transaction) {
        if (viewModel.contextualClassFeatureToggle.value.notNullable()
            && approvalDetail.batchType != Constant.TYPE_BATCH
        ) {
            when (transaction.transactionStatus?.type) {
                Constant.STATUS_FOR_APPROVAL -> {
                    setTransactionHeaderDetails(
                        background = R.drawable.bg_rectangle_yellow,
                        logo = R.drawable.ic_fund_transfer_clock_yellow,
                        headerTextColor = R.color.colorPendingStatus,
                        headerTitle = transaction.transactionStatus?.title,
                        headerContent = transaction.transactionStatus?.message
                    )
                }
                Constant.STATUS_CONDITIONAL,
                Constant.STATUS_PROCESSING -> {
                    setTransactionHeaderDetails(
                        background = R.drawable.bg_rectangle_gray,
                        logo = R.drawable.ic_fund_transfer_gear_gray,
                        headerTextColor = R.color.colorInfo,
                        headerTitle = transaction.transactionStatus?.title,
                        headerContent = transaction.transactionStatus?.message
                    )
                }
                Constant.STATUS_SCHEDULED -> {
                    if (transaction.transactionStatus?.description.equals(
                            "Schedule Cancelled",
                            true
                        )
                    ) {
                        setTransactionHeaderDetails(
                            background = R.drawable.bg_rectangle_red,
                            logo = R.drawable.ic_fund_transfer_warning_red,
                            headerTextColor = R.color.colorRejectedStatus,
                            headerTitle = transaction.transactionStatus?.title,
                            headerContent = transaction.transactionStatus?.message
                        )
                    } else {
                        setTransactionHeaderDetails(
                            background = R.drawable.bg_rectangle_gray,
                            logo = R.drawable.ic_fund_transfer_calendar_gray,
                            headerTextColor = R.color.colorInfo,
                            headerTitle = transaction.transactionStatus?.title,
                            headerContent = transaction.transactionStatus?.message
                        )
                    }
                }
                Constant.STATUS_RELEASED,
                Constant.STATUS_SUCCESS,
                Constant.STATUS_PARTIALLY_RELEASED,
                Constant.STATUS_APPROVED,
                Constant.STATUS_FOR_PROCESSING -> {
                    if (transaction.transactionStatus?.description.equals("Debited", true)) {
                        setTransactionHeaderDetails(
                            background = R.drawable.bg_rectangle_gray,
                            logo = R.drawable.ic_fund_transfer_gear_gray,
                            headerTextColor = R.color.colorInfo,
                            headerTitle = transaction.transactionStatus?.title,
                            headerContent = transaction.transactionStatus?.message
                        )
                    } else {
                        setTransactionHeaderDetails(
                            background = R.drawable.bg_rectangle_green,
                            logo = R.drawable.ic_fund_transfer_check_green,
                            headerTextColor = R.color.colorApprovedStatus,
                            headerTitle = transaction.transactionStatus?.title,
                            headerContent = transaction.transactionStatus?.message
                        )
                    }
                }
                Constant.STATUS_CANCELLED,
                Constant.STATUS_BANCNET_ERROR,
                Constant.STATUS_REJECTED,
                Constant.STATUS_EXPIRED,
                Constant.STATUS_FAILED,
                Constant.STATUS_REVERSED,
                Constant.STATUS_REVERSAL_FAILED -> {
                    setTransactionHeaderDetails(
                        background = R.drawable.bg_rectangle_red,
                        logo = R.drawable.ic_fund_transfer_warning_red,
                        headerTextColor = R.color.colorRejectedStatus,
                        headerTitle = transaction.transactionStatus?.title,
                        headerContent = transaction.transactionStatus?.message
                    )
                }
                else -> {
                    setTransactionHeaderDetails(
                        background = R.drawable.bg_rectangle_gray,
                        logo = R.drawable.ic_fund_transfer_check_gray,
                        headerTextColor = R.color.colorInfo,
                        headerTitle = transaction.transactionStatus?.title,
                        headerContent = transaction.transactionStatus?.message
                    )
                }
            }
        } else {
            when (transaction.transactionStatus?.type) {
                Constant.STATUS_FOR_APPROVAL -> {
                    setTransactionHeaderDetails(
                        background = R.drawable.bg_rectangle_yellow,
                        logo = R.drawable.ic_fund_transfer_clock_yellow,
                        headerTextColor = R.color.colorPendingStatus,
                        headerTitle = formatString(R.string.title_fund_transfer_sent),
                        headerContent = formatString(
                            R.string.msg_fund_transfer_sent_summary,
                            approvalHierarchyManager.approvalHierarchyName
                        )
                    )
                }
                Constant.STATUS_CONDITIONAL -> {
                    setTransactionHeaderDetails(
                        background = R.drawable.bg_rectangle_gray,
                        logo = R.drawable.ic_fund_transfer_gear_gray,
                        headerTextColor = R.color.colorInfo,
                        headerTitle = formatString(R.string.title_fund_transfer_for_processing),
                        headerContent = formatString(R.string.msg_fund_transfer_for_processing_summary)
                    )
                }
                Constant.STATUS_PROCESSING -> {
                    setTransactionHeaderDetails(
                        background = R.drawable.bg_rectangle_gray,
                        logo = R.drawable.ic_fund_transfer_gear_gray,
                        headerTextColor = R.color.colorInfo,
                        headerTitle = formatString(R.string.title_fund_transfer_processing),
                        headerContent = formatString(R.string.msg_fund_transfer_processing_summary)
                    )
                }
                Constant.STATUS_SCHEDULED -> {
                    if (transaction.transactionStatus?.description.equals(
                            "Schedule Cancelled",
                            true
                        )
                    ) {
                        setTransactionHeaderDetails(
                            background = R.drawable.bg_rectangle_red,
                            logo = R.drawable.ic_fund_transfer_warning_red,
                            headerTextColor = R.color.colorRejectedStatus,
                            headerTitle = formatString(
                                R.string.title_fund_transfer_cancelled_by_error,
                                transaction.cancelledBy
                            ),
                            headerContent = formatString(
                                R.string.msg_fund_transfer_cancelled_by_error,
                                transaction.reasonForCancellation
                            )
                        )
                    } else {
                        setTransactionHeaderDetails(
                            background = R.drawable.bg_rectangle_gray,
                            logo = R.drawable.ic_fund_transfer_calendar_gray,
                            headerTextColor = R.color.colorInfo,
                            headerTitle = formatString(R.string.title_fund_transfer_scheduled),
                            headerContent = formatString(
                                R.string.msg_fund_transfer_scheduled_summary,
                                transaction.transactionReferenceId
                            )
                        )
                    }
                }
                Constant.STATUS_CANCELLED -> {
                    setTransactionHeaderDetails(
                        background = R.drawable.bg_rectangle_red,
                        logo = R.drawable.ic_fund_transfer_warning_red,
                        headerTextColor = R.color.colorRejectedStatus,
                        headerTitle = formatString(
                            R.string.title_fund_transfer_cancelled_by_error,
                            transaction.cancelledBy
                        ),
                        headerContent = formatString(
                            R.string.msg_fund_transfer_cancelled_by_error,
                            transaction.reasonForCancellation
                        )
                    )
                }
                Constant.STATUS_RELEASED -> {
                    if (transaction.transactionStatus?.description.equals(
                            "Partially Released",
                            true
                        )
                    ) {
                        setTransactionHeaderDetails(
                            background = R.drawable.bg_rectangle_green,
                            logo = R.drawable.ic_fund_transfer_check_green,
                            headerTextColor = R.color.colorApprovedStatus,
                            headerTitle = formatString(R.string.title_fund_transfer_partially_released),
                            headerContent = formatString(
                                R.string.msg_fund_transfer_partially_released_summary,
                                transaction.transactionReferenceId
                            )
                        )
                    } else {
                        setTransactionHeaderDetails(
                            background = R.drawable.bg_rectangle_green,
                            logo = R.drawable.ic_fund_transfer_check_green,
                            headerTextColor = R.color.colorApprovedStatus,
                            headerTitle = formatString(R.string.title_fund_transfer_released),
                            headerContent = formatString(R.string.msg_fund_transfer_released_summary)
                        )
                    }
                }
                Constant.STATUS_SUCCESS -> {
                    if (transaction.transactionStatus?.description.equals(
                            "Transfer Partially Successful",
                            true
                        )
                    ) {
                        setTransactionHeaderDetails(
                            background = R.drawable.bg_rectangle_green,
                            logo = R.drawable.ic_fund_transfer_check_green,
                            headerTextColor = R.color.colorApprovedStatus,
                            headerTitle = formatString(R.string.title_fund_transfer_partially_successful),
                            headerContent = formatString(
                                R.string.msg_fund_transfer_partially_successful_summary,
                                transaction.transactionReferenceId
                            )
                        )
                    } else {
                        if (transaction.channel.equals(ChannelBankEnum.BILLS_PAYMENT.value, true)) {
                            setTransactionHeaderDetails(
                                background = R.drawable.bg_rectangle_green,
                                logo = R.drawable.ic_fund_transfer_check_green,
                                headerTextColor = R.color.colorApprovedStatus,
                                headerTitle = formatString(R.string.title_fund_transfer_success),
                                headerContent = formatString(
                                    R.string.msg_bills_payment_success_summary,
                                    transaction.transactionReferenceId
                                )
                            )
                        } else {
                            setTransactionHeaderDetails(
                                background = R.drawable.bg_rectangle_green,
                                logo = R.drawable.ic_fund_transfer_check_green,
                                headerTextColor = R.color.colorApprovedStatus,
                                headerTitle = formatString(R.string.title_fund_transfer_success),
                                headerContent = formatString(
                                    R.string.msg_fund_transfer_success_summary,
                                    transaction.transactionReferenceId
                                )
                            )
                        }
                    }
                }
                Constant.STATUS_PARTIALLY_RELEASED -> {
                    setTransactionHeaderDetails(
                        background = R.drawable.bg_rectangle_green,
                        logo = R.drawable.ic_fund_transfer_check_green,
                        headerTextColor = R.color.colorApprovedStatus,
                        headerTitle = formatString(R.string.title_fund_transfer_partially_released),
                        headerContent = formatString(
                            R.string.msg_fund_transfer_partially_released_summary,
                            transaction.transactionReferenceId
                        )
                    )
                }
                Constant.STATUS_FOR_PROCESSING -> {
                    if (transaction.transactionStatus?.description.equals("Debited", true)) {
                        setTransactionHeaderDetails(
                            background = R.drawable.bg_rectangle_gray,
                            logo = R.drawable.ic_fund_transfer_gear_gray,
                            headerTextColor = R.color.colorInfo,
                            headerTitle = formatString(R.string.title_fund_transfer_debited),
                            headerContent = formatString(R.string.msg_fund_transfer_debited_summary)
                        )
                    } else {
                        setTransactionHeaderDetails(
                            background = R.drawable.bg_rectangle_green,
                            logo = R.drawable.ic_fund_transfer_check_green,
                            headerTextColor = R.color.colorApprovedStatus,
                            headerTitle = formatString(R.string.title_fund_transfer_success),
                            headerContent = formatString(
                                R.string.msg_fund_transfer_success_summary,
                                transaction.transactionReferenceId
                            )
                        )
                    }
                }
                Constant.STATUS_APPROVED -> {
                    setTransactionHeaderDetails(
                        background = R.drawable.bg_rectangle_green,
                        logo = R.drawable.ic_fund_transfer_check_green,
                        headerTextColor = R.color.colorApprovedStatus,
                        headerTitle = formatString(R.string.title_fund_transfer_success),
                        headerContent = formatString(
                            R.string.msg_fund_transfer_success_summary,
                            transaction.transactionReferenceId
                        )
                    )
                }
                Constant.STATUS_BANCNET_ERROR -> {
                    setTransactionHeaderDetails(
                        background = R.drawable.bg_rectangle_red,
                        logo = R.drawable.ic_fund_transfer_warning_red,
                        headerTextColor = R.color.colorRejectedStatus,
                        headerTitle = formatString(R.string.title_fund_transfer_error),
                        headerContent = formatString(
                            R.string.msg_fund_transfer_failed_error,
                            formatString(R.string.msg_fund_transfer_bancnet_error)
                        )
                    )
                }
                Constant.STATUS_REJECTED -> {
                    setTransactionHeaderDetails(
                        background = R.drawable.bg_rectangle_red,
                        logo = R.drawable.ic_fund_transfer_warning_red,
                        headerTextColor = R.color.colorRejectedStatus,
                        headerTitle = formatString(
                            R.string.title_fund_transfer_rejected,
                            transaction.rejectedBy.notNullable()
                        ),
                        headerContent = formatString(
                            R.string.msg_fund_transfer_rejected_error,
                            transaction.reasonForRejection
                        )
                    )
                }
                Constant.STATUS_EXPIRED -> {
                    setTransactionHeaderDetails(
                        background = R.drawable.bg_rectangle_red,
                        logo = R.drawable.ic_fund_transfer_warning_red,
                        headerTextColor = R.color.colorRejectedStatus,
                        headerTitle = formatString(R.string.title_fund_transfer_expired),
                        headerContent = formatString(R.string.msg_fund_transfer_expired_error)
                    )
                }
                Constant.STATUS_FAILED,
                Constant.STATUS_REVERSED,
                Constant.STATUS_REVERSAL_FAILED -> {
                    setTransactionHeaderDetails(
                        background = R.drawable.bg_rectangle_red,
                        logo = R.drawable.ic_fund_transfer_warning_red,
                        headerTextColor = R.color.colorRejectedStatus,
                        headerTitle = formatString(R.string.title_fund_transfer_error),
                        headerContent = formatString(
                            R.string.msg_fund_transfer_failed_error,
                            transaction.errorMessage
                        )
                    )
                }
                else -> {
                    setTransactionHeaderDetails(
                        background = R.drawable.bg_rectangle_gray,
                        logo = R.drawable.ic_fund_transfer_check_gray,
                        headerTextColor = R.color.colorInfo,
                        headerTitle = formatString(R.string.title_fund_transfer_for_processing),
                        headerContent = formatString(R.string.msg_fund_transfer_default_status)
                    )
                }
            }
        }
    }

    private fun setTransactionHeaderDetails(
        background: Int,
        logo: Int,
        headerTextColor: Int,
        headerTitle: String?,
        headerContent: String?
    ) {
        binding.viewShareDetails.linearLayoutHeaderStatus.setContextCompatBackground(background)
        binding.viewShareDetails.imageViewHeader.setImageResource(logo)
        binding.viewShareDetails.textViewHeader.text = headerTitle.notEmpty().toHtmlSpan()
        binding.viewShareDetails.textViewHeader.setContextCompatTextColor(headerTextColor)
        binding.viewShareDetails.textViewMsg.text = headerContent.notEmpty().toHtmlSpan()
    }

    private fun drawApprovalHierarchy(approvalHierarchyDto: ApprovalHierarchyDto?) {
        if (approvalHierarchyDto?.approvalHierarchy != null) {
            approvalHierarchyManager.startDrawApprovalHierarchy(approvalHierarchyDto)
        } else {
            approvalHierarchyManager.approvalHierarchyName = ""
            binding.viewApprovalStatus.root.visibility = View.GONE
            binding.textViewState.visibility = View.VISIBLE
            binding.zoomLayout.setVisible(false)
            binding.zoomLayout.layoutParams.height = resources.getDimension(
                R.dimen.constraint_approval_hierarchy_small
            ).toInt()
        }
        initViewShareDetails(approvalDetail)
        if (isClickedShare) {
            showShareableContent()
        }
    }

    private fun getApprovalHierarchy() {
        if (approvalDetail.approvalProcessId != null) {
            binding.viewApprovalStatus.root.visibility = View.VISIBLE
            viewModel.getApprovalHierarchy(
                approvalDetail.approvalProcessId.notNullable(),
                approvalDetail.transactionStatus?.type
            )
        } else {
            binding.viewApprovalStatus.root.visibility = View.GONE
            binding.textViewState.visibility = View.VISIBLE
            binding.zoomLayout.layoutParams.height = resources.getDimension(
                R.dimen.constraint_approval_hierarchy_small
            ).toInt()
            binding.zoomLayout.setVisible(false)
            binding.viewHeader.textViewTitle.text = getString(R.string.title_no_approval_hierarchy)
            approvalHierarchyManager.approvalHierarchyName = ""
            initViewShareDetails(approvalDetail)
        }
    }

    private fun initApprovalDetails() {
        approvalDetail = JsonHelper.fromJson(intent.getStringExtra(EXTRA_TRANSACTION_DETAIL))
        when (approvalDetail.channel) {
            ChannelBankEnum.BILLS_PAYMENT.value -> {
                setToolbarTitle(binding.viewToolbar.tvToolbar, getString(R.string.title_payment_details))
            }
            ChannelBankEnum.CHECK_WRITER.value -> {
                setToolbarTitle(binding.viewToolbar.tvToolbar, getString(R.string.title_check_details))
            }
            ChannelBankEnum.CASH_WITHDRAWAL.name -> {
                setToolbarTitle(binding.viewToolbar.tvToolbar, getString(R.string.title_cash_withdrawal_details))
            }
            else -> {
                setToolbarTitle(binding.viewToolbar.tvToolbar, getString(R.string.title_transfer_details))
            }
        }
        getDetailsBasedOnChannel()
    }

    private fun getDetailsBasedOnChannel() {
        when (approvalDetail.channel) {
            ChannelBankEnum.BILLS_PAYMENT.value -> {
                viewModel.getBillsPaymentDetail(
                    approvalDetail.id.notNullable(),
                    ShowApprovalDetailInitialLoading,
                    ShowApprovalDetailInitialDismissLoading
                )
            }
            ChannelBankEnum.CHECK_WRITER.value -> {
                viewModel.getCheckWriterDetails(
                    approvalDetail.id.notNullable(),
                    ShowApprovalDetailInitialLoading,
                    ShowApprovalDetailInitialDismissLoading
                )
            }
            ChannelBankEnum.CASH_WITHDRAWAL.name -> {
                viewModel.getCashWithdrawalDetails(
                    approvalDetail.id.notNullable(),
                    ShowApprovalDetailInitialLoading,
                    ShowApprovalDetailInitialDismissLoading,
                    approvalDetail.createdBy
                )
            }
            else -> {
                viewModel.getFundTransferDetail(
                    approvalDetail.id.notNullable(),
                    ShowApprovalDetailInitialLoading,
                    ShowApprovalDetailInitialDismissLoading
                )
            }
        }
    }

    private fun initPushNotification() {
        binding.zoomLayout.visibility = View.INVISIBLE
        viewModel.getUserDetails()
        if (intent.getStringExtra(AutobahnFirebaseMessagingService.EXTRA_DATA) != null) {
            val pushNotificationPayload = JsonHelper.fromJson<PushNotificationPayload>(
                intent.getStringExtra(AutobahnFirebaseMessagingService.EXTRA_DATA)
            )
            if (pushNotificationPayload.channel?.equals("bills-payments", true) == true) {
                setToolbarTitle(binding.viewToolbar.tvToolbar, getString(R.string.title_payment_details))
            } else {
                setToolbarTitle(binding.viewToolbar.tvToolbar, getString(R.string.title_transfer_details))
            }
            if (pushNotificationPayload.channel?.equals("bills-payments", true) == true) {
                viewModel.getBillsPaymentDetail(
                    pushNotificationPayload.id.notNullable(),
                    ShowApprovalDetailInitialLoading,
                    ShowApprovalDetailInitialDismissLoading
                )
            } else {
                viewModel.getFundTransferDetail(
                    pushNotificationPayload.id.notNullable(),
                    ShowApprovalDetailInitialLoading,
                    ShowApprovalDetailInitialDismissLoading
                )
            }
        } else {
            initApprovalDetails()
        }
    }

    private fun setInitialView() {
        approvalDetail.let {
            if (it.approved == null) {
                it.approved = false
            }
            if (!intent.getBooleanExtra(SHOW_APRROVAL_ACTION, false)) {
                initViewDetails(false)
            } else {
                when (it.channel) {
                    ChannelBankEnum.CASH_WITHDRAWAL.name -> {
                        val isShownButtons = intent.getBooleanExtra(SHOW_APRROVAL_ACTION, false)
                                && !viewModel.isFromApproveOrReject.value.notNullable()
                        initViewDetails(isShownButtons)
                    }
                    ChannelBankEnum.CHECK_WRITER.value -> {
                        val isShownButtons =
                            it.transactionStatus?.description == Constant.PENDING_APPROVAL
                                    && it.approved == false
                                    && !viewModel.isFromApproveOrReject.value.notNullable()
                        initViewDetails(isShownButtons)
                    }
                    else -> {
                        val isShownButtons =
                            it.transactionStatus?.type == Constant.STATUS_FOR_APPROVAL && it.approved == false
                        initViewDetails(isShownButtons)
                    }
                }
            }
        }
    }

    private fun navigateBatchTransfer() {
        val bundle = Bundle().apply {
            putString(BatchTransferActivity.EXTRA_ID, approvalDetail.id)
            putBoolean(
                BatchTransferActivity.EXTRA_SUPPORT_CWT,
                approvalDetail.transactionType == "BANK_TO_BANK_V2"
            )
        }
        navigator.navigate(
            this,
            BatchTransferActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_UP
        )
    }

    private fun showReasonForRejectionBottomSheet(title: Int) {
        val name = getTransactionNameTitle().notEmpty()
        val isReject = title == R.string.title_reject_transaction
        val desc = formatString(
            when {
                approvalDetail.batchType == Constant.TYPE_BATCH ->
                    R.string.param_desc_reject_batch_transaction
                approvalDetail.channel.equals(ChannelBankEnum.BILLS_PAYMENT.value, true) ->
                    R.string.param_desc_payment_reject_transaction
                approvalDetail.channel.equals(ChannelBankEnum.CHECK_WRITER.value, true) ->
                    R.string.param_desc_check_reject_transaction
                approvalDetail.channel.equals(ChannelBankEnum.CASH_WITHDRAWAL.name, true) ->
                    R.string.param_desc_cash_withdrawal_reject_transaction
                else ->
                    R.string.param_desc_transfer_reject_transaction
            },
            formatString(if (isReject) R.string.title_reject else R.string.title_cancel),
            name
        )
        rejectionConfirmationBottomSheet = ConfirmationBottomSheet.newInstance(
            R.drawable.ic_close_approval,
            getString(R.string.title_confirmation),
            desc,
            getString(if (isReject) R.string.action_reject else R.string.action_confirm),
            getString(R.string.action_cancel),
            null,
            null,
            R.drawable.circle_red
        )

        lateinit var textInputEditTextReasonForRejection: TextInputEditText

        rejectionConfirmationBottomSheet?.setOnConfirmationPageCallBack(
            object :
                OnConfirmationPageCallBack {
                override fun onDataSetDialog(
                    tag: String?,
                    linearLayout: LinearLayout,
                    buttonPositive: Button,
                    buttonNegative: Button,
                    data: String?,
                    dynamicData: String?
                ) {
                    val view =
                        layoutInflater.inflate(R.layout.dialog_reason_for_rejection, null)
                    textInputEditTextReasonForRejection =
                        view.findViewById(R.id.textInputEditTextReasonForRejection)
                    textInputEditTextReasonForRejection.hint =
                        formatString(
                            if (isReject)
                                R.string.hint_reason_for_rejection
                            else
                                R.string.hint_reason_for_cancellation
                        )
                    buttonPositive.text =
                        formatString(if (isReject) R.string.action_reject else R.string.action_confirm)
                    buttonPositive.setContextCompatBackground(R.drawable.bg_gradient_red)
                    viewModel.isValidReasonForRejection.onNext(false)
                    textInputEditTextReasonForRejection.imeOptions = EditorInfo.IME_ACTION_DONE
                    textInputEditTextReasonForRejection.setRawInputType(InputType.TYPE_CLASS_TEXT)
                    validateForm(textInputEditTextReasonForRejection)
                    linearLayout.addView(view)
                }

                override fun onClickPositiveButtonDialog(data: String?, tag: String?) {
                    if (!viewModel.isValidReasonForRejection.value.notNullable()) {
                        textInputEditTextReasonForRejection.refresh()
                    } else {
                        rejectionConfirmationBottomSheet?.dismiss()
                        if (isReject) {
                            if (approvalDetail.channel == ChannelBankEnum.CHECK_WRITER.value) {
                                viewModel.approval(
                                    REQUEST_REJECT,
                                    textInputEditTextReasonForRejection.text.toString(),
                                    mutableListOf(),
                                    mutableListOf(approvalDetail.id.toString())
                                )
                            } else {
                                viewModel.approval(
                                    REQUEST_REJECT,
                                    textInputEditTextReasonForRejection.text.toString(),
                                    mutableListOf(approvalDetail.id.toString()),
                                    mutableListOf()
                                )
                            }
                        } else {
                            if (title == R.string.title_delete_scheduled_transaction) {
                                viewModel.deleteScheduledTransfer(
                                    approvalDetail.id?.toLong() ?: 0,
                                    textInputEditTextReasonForRejection.text.toString()
                                )
                            } else {
                                viewModel.cancelPendingApprovalTransaction(
                                    approvalDetail.id.toString(),
                                    textInputEditTextReasonForRejection.text.toString(),
                                    approvalDetail.channel
                                )
                            }
                        }
                    }
                }

                override fun onClickNegativeButtonDialog(data: String?, tag: String?) {
                    rejectionConfirmationBottomSheet?.dismiss()
                }
            })
        rejectionConfirmationBottomSheet?.isCancelable = true
        rejectionConfirmationBottomSheet?.show(
            supportFragmentManager,
            this::class.java.simpleName
        )
    }

    private fun getTransactionNameTitle(): String? {
        approvalDetail.let {
            return if (it.batchType == Constant.TYPE_BATCH) {
                it.fileName ?: it.remarks
            } else {
                if (it.channel.equals(ChannelBankEnum.BILLS_PAYMENT.value, true)) {
                    it.billerName
                } else if (it.channel.equals(ChannelBankEnum.CHECK_WRITER.value, true)) {
                    it.payeeName
                } else if (it.channel.equals(ChannelBankEnum.CASH_WITHDRAWAL.name, true)) {
                    it.sourceAccountNumber.formatAccountNumber()
                } else {
                    if (it.beneficiaryName != null) {
                        it.beneficiaryName
                    } else {
                        viewUtil.getAccountNumberFormat(it.destinationAccountNumber)
                    }
                }
            }
        }
    }

    private fun validateForm(reasonForRejection: EditText) {
        val reasonForRejectionObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_reason_for_rejection),
            editText = reasonForRejection,
            customErrorMessage = formatString(R.string.error_this_field)
        )
        initSetCounterFlowError(reasonForRejectionObservable)
        RxCombineValidator(reasonForRejectionObservable)
            .asObservable()
            .distinctUntilChanged()
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe {
                viewModel.isValidReasonForRejection.onNext(it)
            }.addTo(disposables)
    }

    private fun deleteScheduledTransfer() {
        val bundle = Bundle()
        bundle.putString(
            ResultLandingPageActivity.EXTRA_PAGE,
            ResultLandingPageActivity.PAGE_SCHEDULED_TRANSFER_DETAIL
        )
        bundle.putString(
            ResultLandingPageActivity.EXTRA_TITLE,
            getString(R.string.title_transaction_deleted)
        )
        bundle.putString(
            ResultLandingPageActivity.EXTRA_DESC,
            formatString(
                R.string.msg_successfully_delete_scheduled_transfer,
                if (approvalDetail.batchType == Constant.TYPE_BATCH) {
                    getTransactionNameTitle()
                } else {
                    formatString(
                        R.string.param_desc_scheduled_transactions_single,
                        getTransactionNameTitle()
                    )
                }
            )
        )
        bundle.putString(
            ResultLandingPageActivity.EXTRA_BUTTON,
            getString(R.string.action_close)
        )
        navigator.navigate(
            this,
            ResultLandingPageActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun initPermission() {
        RxPermissions(this)
            .request(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            .subscribe { granted ->
                if (granted) {
                    isClickedShare = true
                    Handler().postDelayed(
                        {
                            if (approvalHierarchyManager.approvalHierarchyName != null) {
                                showShareableContent()
                            } else {
                                showProgressAlertDialog(ApprovalDetailActivity::class.java.simpleName)
                            }
                        }, resources.getInteger(R.integer.time_delay_thread).toLong()
                    )
                } else {
                    MaterialDialog(this).show {
                        lifecycleOwner(this@ApprovalDetailActivity)
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
            approvalDetail.qrContent.notEmpty(),
            resources.getDimension(R.dimen.image_view_qr_code).toInt(),
            resources.getDimension(R.dimen.image_view_qr_code).toInt()
        )
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe {
                if (getProgressAlertDialog() == null || getProgressAlertDialog()?.isVisible == false)
                    showProgressAlertDialog(ApprovalDetailActivity::class.java.simpleName)
            }
            .subscribe(
                {
                    binding.viewShareDetails.viewHeaderTransaction.imageViewQRCode.setImageBitmap(it.toBitmap())
                    binding.scrollView.visibility(true)
                    Handler().postDelayed(
                        {
                            val shareBitmap = viewUtil.getBitmapByView(binding.viewShareDetails.root)
                            binding.scrollView.visibility(false)
                            dismissProgressAlertDialog()
                            startShareMediaActivity(shareBitmap)
                            if (isClickedShare) isClickedShare = false
                        }, resources.getInteger(R.integer.time_delay_share_media).toLong()
                    )
                }, {
                    Timber.e(it, "showShareableContent")
                    dismissProgressAlertDialog()
                    handleOnError(it)
                }
            ).addTo(disposables)
    }

    @ThreadSafe
    companion object {
        const val EXTRA_TRANSACTION_DETAIL = "transaction_detail"
        const val EXTRA_CANCEL_TRANSACTION = "cancel_transaction"

        const val TYPE_GROUP = "GROUP"
        const val TYPE_OPERATOR = "OPERATOR"

        const val REQUEST_APPROVE = "APPROVE"
        const val REQUEST_REJECT = "REJECT"

        const val SHOW_APRROVAL_ACTION = "approval_actions"
    }

    override val viewModelClassType: Class<ApprovalDetailViewModel>
        get() = ApprovalDetailViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityApprovalDetailsBinding
        get() = ActivityApprovalDetailsBinding::inflate

}
