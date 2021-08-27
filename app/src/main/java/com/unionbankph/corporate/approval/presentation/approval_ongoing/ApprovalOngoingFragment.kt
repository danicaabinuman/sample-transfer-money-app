package com.unionbankph.corporate.approval.presentation.approval_ongoing

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.takusemba.spotlight.Spotlight
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.SettingsSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.dialog.ConfirmationBottomSheet
import com.unionbankph.corporate.app.common.widget.recyclerview.PaginationScrollListener
import com.unionbankph.corporate.app.common.widget.tutorial.OnTutorialListener
import com.unionbankph.corporate.app.common.widget.validator.validation.RxCombineValidator
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.approval.data.model.Transaction
import com.unionbankph.corporate.approval.presentation.ApprovalFragment
import com.unionbankph.corporate.approval.presentation.approval_detail.ApprovalDetailActivity
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.common.presentation.callback.OnConfirmationPageCallBack
import com.unionbankph.corporate.common.presentation.constant.ChannelBankEnum
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.constant.GravityEnum
import com.unionbankph.corporate.common.presentation.constant.OverlayAnimationEnum
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialError
import com.unionbankph.corporate.common.presentation.viewmodel.TutorialViewModel
import com.unionbankph.corporate.databinding.FragmentApprovalOngoingBinding
import io.reactivex.Observable
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import java.util.concurrent.TimeUnit

class ApprovalOngoingFragment :
    BaseFragment<FragmentApprovalOngoingBinding, ApprovalOngoingViewModel>(),
    ApprovalOngoingController.AdapterCallbacks,
    EpoxyAdapterCallback<Transaction>,
    OnTutorialListener {

    private val pageable by lazyFast { Pageable() }

    private val dashBoardActivity by lazyFast { (activity as DashboardActivity) }

    private var tutorialResults: MutableList<Transaction> = mutableListOf()

    private var selectedIds: MutableList<String> = mutableListOf()

    private var checkWriterSelectedIds: MutableList<String> = mutableListOf()

    private var isSelection: Boolean = false

    private lateinit var textInputEditTextReasonForRejection: TextInputEditText

    private var snackBarProgressBar: Snackbar? = null

    private var rejectionConfirmationBottomSheet: ConfirmationBottomSheet? = null

    private var approvalConfirmationBottomSheet: ConfirmationBottomSheet? = null

    private val controller by lazyFast {
        ApprovalOngoingController(
            applicationContext,
            viewUtil,
            autoFormatUtil
        )
    }

    private val tutorialController by lazyFast {
        ApprovalOngoingController(
            applicationContext,
            viewUtil,
            autoFormatUtil
        )
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initViewModel()
        initTutorialViewModel()
    }

    private fun initViewModel() {
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowApprovalOngoingLoading -> {
                    showLoading(
                        binding.viewLoadingState.viewLoadingLayout,
                        getSwipeRefreshLayout(),
                        getRecyclerView(),
                        binding.textViewState
                    )
                    if (binding.viewLoadingState.viewLoadingLayout.visibility == View.VISIBLE) {
                        updateController(mutableListOf())
                    }
                }
                is ShowApprovalOngoingDismissLoading -> {
                    dismissLoading(
                        binding.viewLoadingState.viewLoadingLayout,
                        getSwipeRefreshLayout(),
                        getRecyclerView()
                    )
                }
                is ShowApprovalOngoingEndlessLoading -> {
                    showEndlessProgressBar()
                }
                is ShowApprovalOngoingEndlessDismissLoading -> {
                    dismissEndlessProgressBar()
                }
                is ShowApprovalOngoingRequestLoading -> {
                    showProgressAlertDialog(
                        ApprovalOngoingFragment::class.java.simpleName
                    )
                }
                is ShowApprovalOngoingRequestDismissLoading -> {
                    dismissProgressAlertDialog()
                }
            }
            if (dashBoardActivity.viewPager().currentItem == 2
                && (parentFragment as ApprovalFragment).viewPager().currentItem == 0
            ) {
                when (it) {
                    is ShowApprovalOngoingError -> {
                        handleOnError(it.throwable)
                    }
                }
            }
        })
        viewModel.transactions.observe(this, Observer {
            it?.let {
                updatedSelectedItem(it)
                if (pageable.isInitialLoad) {
                    showEmptyState(it)
                    scrollToTop()
                    updateApprovalBadge()
                }
                updateController(it)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (hasInitialLoad) {
            hasInitialLoad = false
        }
        dashBoardActivity.allowMultipleSelectionApprovals(getTransactions().isNotEmpty())
    }

    private fun initTutorialViewModel() {
        tutorialViewModel = ViewModelProviders.of(
            this,
            viewModelFactory
        )[TutorialViewModel::class.java]
        tutorialViewModel.state.observe(this, Observer {
            when (it) {
                is ShowTutorialError -> {
                    handleOnError(it.throwable)
                }
            }
        })
    }

    private fun startTutorialTestData() {
        val parseApprovals = viewUtil.loadJSONFromAsset(getAppCompatActivity(), "approvals")
        tutorialResults = JsonHelper.fromListJson(parseApprovals)
        if (isTableView()) {
            binding.viewTable.linearLayoutRow.visibility(true)
        }
        getRecyclerView().visibility(false)
        updateTutorialController()
        viewUtil.animateRecyclerView(getRecyclerViewTutorial(), true)
    }

    private fun startViewTutorial() {
        val firstItem = if (isTableView()) {
            getRecyclerViewTutorial().findViewHolderForAdapterPosition(0)
                ?.itemView
        } else {
            getRecyclerViewTutorial().findViewHolderForAdapterPosition(0)
                ?.itemView?.findViewById<androidx.cardview.widget.CardView>(R.id.cardViewContent)
        }
        tutorialEngineUtil.startTutorial(
            getAppCompatActivity(),
            firstItem!!,
            R.layout.frame_tutorial_upper_left,
            if (isTableView()) 0f else resources.getDimension(R.dimen.card_radius),
            false,
            getString(R.string.msg_tutorial_approval_click),
            GravityEnum.BOTTOM,
            OverlayAnimationEnum.ANIM_EXPLODE
        )
    }

    override fun onViewsBound() {
        super.onViewsBound()
        tutorialEngineUtil.setOnTutorialListener(this)
        initRecyclerView()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initEventBus()
        initDataBus()
        initRxSearchEventListener()
        getSwipeRefreshLayout().apply {
            setColorSchemeResources(getAccentColor())
            setOnRefreshListener {
                fetchApprovalOngoing(true)
            }
        }
        RxView.clicks(dashBoardActivity.binding.viewApprovalsNavigation.textViewApprove)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                showApprovalConfirmationBottomSheet(
                    false,
                    formatString(R.string.title_approve_transaction),
                    formatString(
                        R.string.param_desc_approve_selected_transaction,
                        selectedIds.size.plus(checkWriterSelectedIds.size)
                    )
                )
            }.addTo(disposables)
        RxView.clicks(binding.viewSearchLayout.imageViewClearText)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                binding.viewSearchLayout.editTextSearch.requestFocus()
                binding.viewSearchLayout.editTextSearch.text?.clear()
            }.addTo(disposables)
        RxView.clicks(dashBoardActivity.binding.viewApprovalsNavigation.textViewReject)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                clickRejectButton()
            }.addTo(disposables)
        RxView.clicks(dashBoardActivity.binding.viewApprovalsNavigation.textViewSelectAll)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                clickSelectAllButton()
            }.addTo(disposables)
        RxView.clicks(dashBoardActivity.textViewEditApprovals())
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                if (isSelection) {
                    clearSelection()
                } else {
                    setMultiSelection(true)
                    updateViews()
                    updateController()
                }
            }.addTo(disposables)
    }

    private fun showApprovalConfirmationBottomSheet(
        isFromSelection: Boolean,
        title: String,
        desc: String,
        transaction: Transaction? = null
    ) {
        approvalConfirmationBottomSheet = ConfirmationBottomSheet.newInstance(
            R.drawable.ic_check_approval,
            title,
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
                    if (isFromSelection) {
                        transaction?.let {
                            if (it.channel == ChannelBankEnum.CHECK_WRITER.value) {
                                val checkWriterBatchIds = mutableListOf<String>()
                                checkWriterBatchIds.add(it.id.notNullable())
                                viewModel.approval(
                                    approvalType = REQUEST_APPROVE,
                                    batchIds = mutableListOf(),
                                    checkWriterBatchIds = checkWriterBatchIds
                                )
                            } else {
                                val batchIds = mutableListOf<String>()
                                batchIds.add(it.id.notNullable())
                                viewModel.approval(
                                    approvalType = REQUEST_APPROVE,
                                    batchIds = batchIds,
                                    checkWriterBatchIds = mutableListOf()
                                )
                            }
                        }

                    } else {
                        if (selectedIds.size.plus(checkWriterSelectedIds.size) > 0) {
                            viewModel.approval(
                                approvalType = REQUEST_APPROVE,
                                batchIds = selectedIds,
                                checkWriterBatchIds = checkWriterSelectedIds
                            )
                        }
                    }
                }

                override fun onClickNegativeButtonDialog(data: String?, tag: String?) {
                    approvalConfirmationBottomSheet?.dismiss()
                }

            })
        approvalConfirmationBottomSheet?.isCancelable = true
        approvalConfirmationBottomSheet?.show(
            childFragmentManager,
            this::class.java.simpleName
        )
    }

    override fun onClickItem(id: String, result: String, position: Int) {
        val transaction = getTransactions()[position]
        if (isSelection) {
            transaction.hasSelected =
                !transaction.hasSelected
            if (transaction.hasSelected) {
                addItem(transaction.channel, transaction)
            } else {
                removeItem(transaction.channel, transaction)
            }
            updateViews()
            updateController()
        } else {
            val bundle = Bundle().apply {
                putString(ApprovalDetailActivity.EXTRA_TRANSACTION_DETAIL, result)
                putBoolean(ApprovalDetailActivity.SHOW_APRROVAL_ACTION, true)
            }
            navigator.navigate(
                getAppCompatActivity(),
                ApprovalDetailActivity::class.java,
                bundle,
                isClear = false,
                isAnimated = true,
                transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_UP
            )
        }
    }

    override fun onTapToRetry() {
        fetchApprovalOngoing(isInitialLoading = false, isTapToRetry = true)
    }

    override fun onLongClickItem(id: String, position: Int) {
        if (!isSelection) {
            val transaction = getTransactions()[position]
            setMultiSelection(true)
            transaction.hasSelected =
                !transaction.hasSelected
            if (transaction.hasSelected) {
                addItem(transaction.channel, transaction)
            } else {
                removeItem(transaction.channel, transaction)
            }
            updateViews()
            updateController()
        }
    }

    override fun onClickItemApprove(id: String, position: Int) {
        val transaction = getTransactions()[position]
        val name = getTransactionNameTitle(transaction).notEmpty()
        val desc = formatString(
            when {
                transaction.batchType == Constant.TYPE_BATCH ->
                    R.string.param_desc_approve_batch_transaction
                transaction.channel.equals(ChannelBankEnum.BILLS_PAYMENT.value, true) ->
                    R.string.param_desc_payment_approve_transaction
                transaction.channel.equals(ChannelBankEnum.CHECK_WRITER.value, true) ->
                    R.string.param_desc_check_approve_transaction
                transaction.channel.equals(ChannelBankEnum.CASH_WITHDRAWAL.name, true) ->
                    R.string.param_desc_cash_withdrawal_approve_transaction
                else ->
                    R.string.param_desc_transfer_approve_transaction
            },
            name
        )
        showApprovalConfirmationBottomSheet(
            true,
            formatString(R.string.title_approve_transaction),
            desc,
            transaction
        )
    }

    override fun onClickItemReject(id: String, position: Int) {
        val transaction = getTransactions()[position]
        val name = getTransactionNameTitle(transaction).notEmpty()
        if (transaction.channel == ChannelBankEnum.CHECK_WRITER.value) {
            showReasonForRejectionBottomSheet(
                mutableListOf(),
                mutableListOf(id),
                name,
                transaction.batchType,
                transaction.channel.notNullable()
            )
        } else {
            showReasonForRejectionBottomSheet(
                mutableListOf(id),
                mutableListOf(),
                name,
                transaction.batchType,
                transaction.channel.notNullable()
            )
        }
    }

    private fun getTransactionNameTitle(transaction: Transaction): String? {
        transaction.let {
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
            clearTutorial()
        } else {
            if (viewTarget.findViewById<TextView>(R.id.textViewTutorialMessage).text ==
                getString(R.string.msg_tutorial_approval_click)
            ) {
                val firstItem = if (isTableView()) {
                    getRecyclerViewTutorial().findViewHolderForAdapterPosition(0)
                        ?.itemView
                } else {
                    getRecyclerViewTutorial().findViewHolderForAdapterPosition(0)
                        ?.itemView?.findViewById<androidx.cardview.widget.CardView>(R.id.cardViewContent)
                }
                tutorialEngineUtil.startTutorial(
                    getAppCompatActivity(),
                    firstItem!!,
                    R.layout.frame_tutorial_upper_left,
                    if (isTableView()) 0f else resources.getDimension(R.dimen.card_radius),
                    false,
                    getString(R.string.msg_tutorial_approval_longclick),
                    GravityEnum.BOTTOM,
                    OverlayAnimationEnum.ANIM_EXPLODE
                )
            } else if (viewTarget.findViewById<TextView>(R.id.textViewTutorialMessage).text ==
                getString(R.string.msg_tutorial_approval_longclick)
            ) {
                clearTutorial()
                eventBus.settingsSyncEvent.emmit(
                    BaseEvent(SettingsSyncEvent.ACTION_ENABLE_NAVIGATION_BOTTOM)
                )
            }
        }
    }

    private fun resetUIList() {
        clearRecyclerView()
        initSwipeRefreshLayout()
        initRecyclerView()
        showEmptyState(getTransactions())
        scrollToTop()
        updateController()
    }

    private fun initSwipeRefreshLayout() {
        getSwipeRefreshLayout().apply {
            setColorSchemeResources(getAccentColor())
            setOnRefreshListener {
                fetchApprovalOngoing(true)
            }
        }
    }

    private fun clearRecyclerView() {
        if (isTableView()) {
            binding.swipeRefreshLayoutTable.visibility(true)
            binding.swipeRefreshLayoutApprovalOngoing.visibility(false)
            binding.recyclerViewApprovalOngoing.clear()
            binding.recyclerViewApprovalOngoing.clearPreloaders()
            binding.recyclerViewApprovalOngoing.adapter = null
            binding.recyclerViewApprovalOngoing.layoutManager = null
            binding.recyclerViewTutorialApprovalOngoing.clear()
            binding.recyclerViewTutorialApprovalOngoing.clearPreloaders()
            binding.recyclerViewTutorialApprovalOngoing.adapter = null
            binding.recyclerViewTutorialApprovalOngoing.layoutManager = null
        } else {
            binding.swipeRefreshLayoutTable.visibility(false)
            binding.swipeRefreshLayoutApprovalOngoing.visibility(true)
            binding.viewTable.recyclerViewTable.clear()
            binding.viewTable.recyclerViewTable.clearPreloaders()
            binding.viewTable.recyclerViewTable.adapter = null
            binding.viewTable.recyclerViewTable.layoutManager = null
            binding.viewTable.recyclerViewTableTutorial.clear()
            binding.viewTable.recyclerViewTableTutorial.clearPreloaders()
            binding.viewTable.recyclerViewTableTutorial.adapter = null
            binding.viewTable.recyclerViewTableTutorial.layoutManager = null
        }
    }

    private fun updateController(data: MutableList<Transaction> = getTransactions()) {
        controller.setData(data, isSelection, pageable, isTableView())
    }

    private fun updateTutorialController() {
        tutorialController.setData(tutorialResults, false, pageable, isTableView())
    }

    private fun showEndlessProgressBar() {
        getRecyclerView().post {
            pageable.isLoadingPagination = true
            if (isTableView()) {
                if (snackBarProgressBar == null) {
                    snackBarProgressBar = viewUtil.showCustomSnackBar(
                        binding.coordinatorLayoutSnackBar,
                        R.layout.widget_snackbar_progressbar,
                        Snackbar.LENGTH_INDEFINITE
                    )
                }
                snackBarProgressBar?.show()
            } else {
                updateController()
            }
        }
    }

    private fun dismissEndlessProgressBar() {
        getRecyclerView().post {
            pageable.isLoadingPagination = false
            if (isTableView()) {
                snackBarProgressBar?.dismiss()
            } else {
                updateController()
            }
        }
    }

    private fun showEmptyState(data: MutableList<Transaction>) {
        if (isTableView()) {
            binding.viewTable.linearLayoutRow.visibility(data.size > 0)
        }
        if (data.size > 0) {
            if ((parentFragment as ApprovalFragment).viewPager().currentItem == 0) {
                dashBoardActivity.allowMultipleSelectionApprovals(true)
            }
            if (binding.textViewState.visibility == View.VISIBLE) binding.textViewState?.visibility = View.GONE
        } else {
            binding.textViewState.visibility = View.VISIBLE
        }
    }

    private fun setMultiSelection(isShown: Boolean) {
        isSelection = isShown
        dashBoardActivity.textViewEditApprovals().text =
            formatString(if (isShown) R.string.action_done else R.string.action_edit)
        getSwipeRefreshLayout().isEnabled = !isShown && !isSelection
        dashBoardActivity.viewApprovalsNavigation().visibility(isShown)
        dashBoardActivity.bottomNavigationBTR().setVisible(!isShown)
    }

    private fun updateViews() {
        val isValid = selectedIds.size.plus(checkWriterSelectedIds.size) > 0
        dashBoardActivity.binding.viewApprovalsNavigation.textViewApprove.isEnabled = isValid
        dashBoardActivity.binding.viewApprovalsNavigation.textViewReject.isEnabled = isValid
        dashBoardActivity.binding.viewApprovalsNavigation.textViewApprove.alpha = if (isValid) 1f else 0.5f
        dashBoardActivity.binding.viewApprovalsNavigation.textViewReject.alpha = if (isValid) 1f else 0.5f
        dashBoardActivity.binding.viewApprovalsNavigation.textViewApprove.text =
            formatString(
                R.string.param_action_approve,
                selectedIds.size.plus(checkWriterSelectedIds.size).toString()
            )
        dashBoardActivity.binding.viewApprovalsNavigation.textViewReject.text =
            formatString(
                R.string.param_action_reject,
                selectedIds.size.plus(checkWriterSelectedIds.size).toString()
            )
        updateButtonSelectedAll()
    }

    fun clearSelection() {
        setMultiSelection(false)
        clearAllSelection()
    }

    private fun clickSelectAllButton() {
        if (!isSelectedAll()) {
            selectAll()
        } else {
            clearAllSelection()
        }
    }

    private fun clickRejectButton() {
        showReasonForRejectionBottomSheet(
            selectedIds,
            checkWriterSelectedIds,
            getString(R.string.msg_reject),
            Constant.TYPE_BATCH
        )
    }

    private fun initDataBus() {
        dataBus.approvalBatchIdDataBus.flowable.subscribe {
            removeBatchIdsFromList(it.batchIds)
            removeCheckWriterIdsFromList(it.transactionReferenceNumbers)
            clearAllSelection()
            setMultiSelection(false)
            resetSearchLayout()
            showEmptyState(getTransactions())
        }.addTo(disposables)
    }

    private fun updateApprovalBadge() {
        if (getTransactions().size > 0) {
            dashBoardActivity.setBottomNavigationBadgeColor(
                R.color.colorGrayBadge, 2
            )
            scrollToTop()
        }
    }

    private fun initEventBus() {
        eventBus.settingsSyncEvent.flowable.subscribe {
            when (it.eventType) {
                SettingsSyncEvent.ACTION_SCROLL_TO_TOP ->
                    if (dashBoardActivity.viewPager().currentItem == 2) {
                        scrollToTop()
                    }
                SettingsSyncEvent.ACTION_TUTORIAL_APPROVAL_TAP -> {
                    startViewTutorial()
                }
                SettingsSyncEvent.ACTION_TUTORIAL_APPROVAL_SHOW_DATA -> {
                    startTutorialTestData()
                }
                SettingsSyncEvent.ACTION_TUTORIAL_APPROVAL_CLEAR_DATA -> {
                    clearTutorial()
                }
                SettingsSyncEvent.ACTION_RESET_DEMO -> fetchApprovalOngoing(true)
            }
        }.addTo(disposables)
        eventBus.actionSyncEvent.flowable.subscribe {
            when (it.eventType) {
                ActionSyncEvent.ACTION_UPDATE_TRANSACTION_LIST -> {
                    pageable.filter = null
                    getSwipeRefreshLayout().isRefreshing = true
                    fetchApprovalOngoing(true)
                }
                ActionSyncEvent.ACTION_LOAD_APPROVAL_ONGOING_LIST -> {
                    fetchApprovalOngoing(true)
                }
                ActionSyncEvent.ACTION_CANCEL_MULTIPLE_SELECTION_APPROVAL -> {
                    clearSelection()
                }
                ActionSyncEvent.ACTION_RESET_LIST_UI -> {
                    resetUIList()
                }
            }
        }.addTo(disposables)
    }

    private fun resetSearchLayout() {
        binding.viewSearchLayout.editTextSearch.text?.clear()
    }

    private fun showReasonForRejectionBottomSheet(
        ids: MutableList<String>,
        checkWriterIds: MutableList<String>,
        name: String?,
        type: String? = null,
        channel: String = ChannelBankEnum.UBP_TO_UBP.value
    ) {

        val desc = if (dashBoardActivity.viewApprovalsNavigation().visibility == View.VISIBLE) {
            formatString(
                R.string.param_desc_reject_selected_transaction,
                selectedIds.size.plus(checkWriterSelectedIds.size)
            )
        } else {
            formatString(
                when {
                    type == Constant.TYPE_BATCH ->
                        R.string.param_desc_reject_batch_transaction
                    channel.equals(ChannelBankEnum.BILLS_PAYMENT.value, true) ->
                        R.string.param_desc_payment_reject_transaction
                    channel.equals(ChannelBankEnum.CHECK_WRITER.value, true) ->
                        R.string.param_desc_check_reject_transaction
                    channel.equals(ChannelBankEnum.CASH_WITHDRAWAL.name, true) ->
                        R.string.param_desc_cash_withdrawal_reject_transaction
                    else ->
                        R.string.param_desc_transfer_reject_transaction
                },
                formatString(R.string.title_reject),
                name
            )
        }
        rejectionConfirmationBottomSheet = ConfirmationBottomSheet.newInstance(
            R.drawable.ic_close_approval,
            getString(R.string.title_confirmation),
            desc,
            getString(R.string.action_reject),
            getString(R.string.action_cancel),
            null,
            null,
            R.drawable.circle_red
        )
        rejectionConfirmationBottomSheet?.setOnConfirmationPageCallBack(
            object : OnConfirmationPageCallBack {
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
                        formatString(R.string.hint_reason_for_rejection)
                    viewModel.isValidReasonForRejection.onNext(false)
                    textInputEditTextReasonForRejection.imeOptions = EditorInfo.IME_ACTION_DONE
                    textInputEditTextReasonForRejection.setRawInputType(InputType.TYPE_CLASS_TEXT)
                    buttonPositive.setContextCompatBackground(R.drawable.bg_gradient_red)
                    validateForm(textInputEditTextReasonForRejection)
                    linearLayout.addView(view)
                }

                override fun onClickPositiveButtonDialog(data: String?, tag: String?) {
                    if (!viewModel.isValidReasonForRejection.value.notNullable()) {
                        textInputEditTextReasonForRejection.refresh()
                    } else {
                        rejectionConfirmationBottomSheet?.dismiss()
                        viewModel.approval(
                            REQUEST_REJECT,
                            textInputEditTextReasonForRejection.text.toString(),
                            ids,
                            checkWriterIds
                        )
                    }
                }

                override fun onClickNegativeButtonDialog(data: String?, tag: String?) {
                    rejectionConfirmationBottomSheet?.dismiss()
                }
            })
        rejectionConfirmationBottomSheet?.isCancelable = true
        rejectionConfirmationBottomSheet?.show(
            childFragmentManager,
            this::class.java.simpleName
        )
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

    private fun clearTutorial() {
        tutorialResults.clear()
        getRecyclerView().visibility(true)
        getRecyclerViewTutorial().visibility(false)
        updateTutorialController()
        if (binding.viewLoadingState.viewLoadingLayout.visibility != View.VISIBLE) {
            showEmptyState(getTransactions())
        }
    }

    private fun initRxSearchEventListener() {
        binding.viewSearchLayout.editTextSearch.setOnEditorActionListener(
            TextView.OnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    binding.viewSearchLayout.editTextSearch.clearFocus()
                    viewUtil.dismissKeyboard(getAppCompatActivity())
                    fetchApprovalOngoing(true)
                    return@OnEditorActionListener true
                }
                false
            }
        )
        RxTextView.textChangeEvents(binding.viewSearchLayout.editTextSearch)
            .debounce(
                resources.getInteger(R.integer.time_edit_text_search_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe { filter ->
                binding.viewSearchLayout.imageViewClearText.visibility(filter.text().isNotEmpty())
                if (filter.view().isFocused) {
                    pageable.filter = filter.text().toString().nullable()
                    fetchApprovalOngoing(true)
                }
            }.addTo(disposables)
    }


    private fun addItem(channel: String?, transaction: Transaction) {
        if (channel == ChannelBankEnum.CHECK_WRITER.value) {
            if (!checkWriterSelectedIds.contains(transaction.id)) {
                checkWriterSelectedIds.add(transaction.id.notNullable())
            }
        } else {
            if (!selectedIds.contains(transaction.id)) {
                selectedIds.add(transaction.id.notNullable())
            }
        }
    }

    private fun removeItem(channel: String?, transaction: Transaction) {
        if (channel == ChannelBankEnum.CHECK_WRITER.value) {
            checkWriterSelectedIds.remove(transaction.id)
        } else {
            selectedIds.remove(transaction.id)
        }
    }

    private fun selectAll() {
        getTransactions().forEachIndexed { index, result ->
            if (!result.hasSelected) {
                getTransactions()[index].hasSelected = true
                selectedIds.add(result.id.toString())
            }
        }
        updateController()
        updateViews()
    }

    private fun updateButtonSelectedAll() {
        Observable.fromIterable(getTransactions())
            .filter { it.hasSelected }
            .count()
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    dashBoardActivity.binding.viewApprovalsNavigation.textViewSelectAll.text =
                        if (it.toInt() == getTransactions().size)
                            formatString(R.string.action_deselect_all)
                        else
                            formatString(R.string.action_select_all)
                },
                {
                    Timber.e(it, "updateButtonSelectedAll")
                }
            ).addTo(disposables)
    }

    private fun isSelectedAll(): Boolean {
        return getTransactions().size == getTransactions().filter { it.hasSelected }.count()
    }

    private fun updatedSelectedItem(data: MutableList<Transaction>) {
        selectedIds.forEach { id ->
            data.filter { result -> result.id == id }.forEach { it.hasSelected = true }
        }
        checkWriterSelectedIds.forEach { id ->
            data.filter { result -> result.id == id }.forEach { it.hasSelected = true }
        }
    }

    private fun clearAllSelection() {
        selectedIds.clear()
        checkWriterSelectedIds.clear()
        getTransactions().filter { it.hasSelected }.forEach {
            it.hasSelected = false
        }
        updateController()
        updateViews()
    }

    private fun removeBatchIdsFromList(batchIds: MutableList<String>?) {
        batchIds?.forEachIndexed { index, id ->
            val mutableIterator = getTransactions().iterator()
            mutableIterator.forEach {
                if (it.id == id) {
                    mutableIterator.remove()
                    return@forEach
                }
            }
        }
    }

    private fun removeCheckWriterIdsFromList(checkWriterIds: MutableList<String>?) {
        checkWriterIds?.forEachIndexed { index, id ->
            val mutableIterator = getTransactions().iterator()
            mutableIterator.forEach {
                if (it.id == id) {
                    mutableIterator.remove()
                    return@forEach
                }
            }
        }
    }

    private fun scrollToTop() {
        getRecyclerView().post {
            getRecyclerView().scrollToPosition(0)
        }
    }

    fun fetchApprovalOngoing(isInitialLoading: Boolean, isTapToRetry: Boolean = false) {
        if (isTapToRetry) {
            pageable.refreshErrorPagination()
        }
        if (isInitialLoading) {
            pageable.resetPagination()
        } else {
            pageable.resetLoad()
        }
        viewModel.getApprovalList(
            status = STATUS_ONGOING,
            pageable = pageable,
            isInitialLoading = isInitialLoading
        )
    }

    private fun initRecyclerView() {
        initHeaderRow()
        controller.setAdapterCallbacks(this)
        controller.setEpoxyAdapterCallback(this)
        val linearLayoutManager = getLinearLayoutManager()
        getRecyclerView().layoutManager = linearLayoutManager
        getRecyclerView().addOnScrollListener(
            object : PaginationScrollListener(linearLayoutManager) {
                override val totalPageCount: Int
                    get() = pageable.totalPageCount
                override val isLastPage: Boolean
                    get() = pageable.isLastPage
                override val isLoading: Boolean
                    get() = pageable.isLoadingPagination
                override val isFailed: Boolean
                    get() = pageable.isFailed

                override fun loadMoreItems() {
                    if (!pageable.isLoadingPagination) fetchApprovalOngoing(false)
                }
            }
        )
        getRecyclerView().setController(controller)

        getRecyclerViewTutorial().setController(tutorialController)
        tutorialController.setAdapterCallbacks(this)
        tutorialController.setEpoxyAdapterCallback(this)
    }

    private fun initHeaderRow() {
        if (isTableView()) {
            binding.coordinatorLayoutSnackBar.visibility(true)
            binding.swipeRefreshLayoutTable.visibility(true)
            binding.swipeRefreshLayoutApprovalOngoing.visibility(false)
            val headers =
                resources.getStringArray(R.array.array_headers_ongoing_approvals).toMutableList()
            binding.viewTable.linearLayoutRow.removeAllViews()
            headers.forEach {
                val viewRowHeader = layoutInflater.inflate(R.layout.header_table_row, null)
                val textViewHeader =
                    viewRowHeader.findViewById<AppCompatTextView>(R.id.textViewHeader)
                textViewHeader.text = it
                binding.viewTable.linearLayoutRow.addView(viewRowHeader)
            }
        }
    }

    private fun getRecyclerView() =
        if (isTableView()) binding.viewTable.recyclerViewTable else binding.recyclerViewApprovalOngoing

    private fun getRecyclerViewTutorial() =
        if (isTableView()) binding.viewTable.recyclerViewTableTutorial else binding.recyclerViewTutorialApprovalOngoing

    private fun getSwipeRefreshLayout() =
        if (isTableView()) binding.swipeRefreshLayoutTable else binding.swipeRefreshLayoutApprovalOngoing

    private fun getTransactions(): MutableList<Transaction> =
        viewModel.transactions.value ?: mutableListOf()

    companion object {

        const val STATUS_ONGOING = "NOTIFIED"
        const val REQUEST_APPROVE = "APPROVE"
        const val REQUEST_REJECT = "REJECT"

        fun newInstance(): ApprovalOngoingFragment {
            val fragment =
                ApprovalOngoingFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }

    }

    override val viewModelClassType: Class<ApprovalOngoingViewModel>
        get() = ApprovalOngoingViewModel::class.java

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentApprovalOngoingBinding
        get() = FragmentApprovalOngoingBinding::inflate

}
