package com.unionbankph.corporate.fund_transfer.presentation.scheduled.scheduled_transfer_ongoing

import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
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
import com.google.android.material.textfield.TextInputLayout
import com.takusemba.spotlight.Spotlight
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.SettingsSyncEvent
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.dialog.ConfirmationBottomSheet
import com.unionbankph.corporate.app.common.widget.recyclerview.PaginationScrollListener
import com.unionbankph.corporate.app.common.widget.tutorial.OnTutorialListener
import com.unionbankph.corporate.app.common.widget.validator.validation.RxCombineValidator
import com.unionbankph.corporate.approval.data.model.Transaction
import com.unionbankph.corporate.approval.presentation.approval_detail.ApprovalDetailActivity
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.common.presentation.callback.OnConfirmationPageCallBack
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.constant.GravityEnum
import com.unionbankph.corporate.common.presentation.constant.OverlayAnimationEnum
import com.unionbankph.corporate.common.presentation.constant.TutorialScreenEnum
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialError
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialHasTutorial
import com.unionbankph.corporate.common.presentation.viewmodel.TutorialViewModel
import com.unionbankph.corporate.databinding.FragmentManageScheduledTransferOngoingBinding
import com.unionbankph.corporate.fund_transfer.data.model.ScheduledTransferDeletionForm
import com.unionbankph.corporate.fund_transfer.presentation.scheduled.*
import com.unionbankph.corporate.general.presentation.result.ResultLandingPageActivity
import io.reactivex.rxkotlin.addTo

/**
 * Created by herald25santos on 12/03/2019
 */
class ManageScheduledTransferOngoingFragment :
    BaseFragment<FragmentManageScheduledTransferOngoingBinding, ManageScheduledTransferViewModel>(),
    OnTutorialListener,
    OnConfirmationPageCallBack,
    EpoxyAdapterCallback<Transaction> {

    private lateinit var textInputEditTextReasonForRejection: TextInputEditText

    private var snackBarProgressBar: Snackbar? = null

    private val pageable by lazyFast { Pageable() }

    private val controller by lazyFast {
        ManageScheduledTransferController(
            applicationContext,
            viewUtil,
            autoFormatUtil
        )
    }

    private val tutorialController by lazyFast {
        ManageScheduledTransferController(
            applicationContext,
            viewUtil,
            autoFormatUtil
        )
    }

    private var isSelection = false

    override fun beforeLayout(savedInstanceState: Bundle?) {
        super.beforeLayout(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initBinding()
        init()
        initRecyclerView()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initEventBus()
        getSwipeRefreshLayout().apply {
            setColorSchemeResources(getAccentColor())
            setOnRefreshListener {
                getManageScheduledTransfers(true)
            }
        }
        binding.viewApprovalsNavigation.textViewReject.setOnClickListener {
            showDeleteScheduledTransferBottomSheet()
        }
        binding.viewApprovalsNavigation.textViewSelectAll.setOnClickListener {
            if (binding.viewApprovalsNavigation.textViewSelectAll.text == formatString(R.string.action_select_all)) {
                viewModel.selectAll()
            } else {
                viewModel.deSelectAll()
            }
        }
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initTutorialViewModel()
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
        tutorialViewModel.hasTutorial(TutorialScreenEnum.FUND_TRANSFER)
    }

    private fun initViewModel() {
        viewModel.state.observe(this, Observer {

            when (it) {
                is ShowManageScheduledTransferLoading -> {
                    showLoading(
                        binding.viewLoadingState.root,
                        getSwipeRefreshLayout(),
                        getRecyclerView(),
                        binding.textViewState
                    )
                }
                is ShowManageScheduledTransferDismissLoading -> {
                    dismissLoading(
                        binding.viewLoadingState.root,
                        getSwipeRefreshLayout(),
                        getRecyclerView()
                    )
                }
                is ShowManageScheduledTransferEndlessLoading -> {
                    showEndlessProgressBar()
                }
                is ShowManageScheduledTransferEndlessDismissLoading -> {
                    dismissEndlessProgressBar()
                }
                is ShowManageScheduledTransferSubmitLoading -> {
                    showProgressAlertDialog(
                        ManageScheduledTransferOngoingFragment::class.java.simpleName
                    )
                }
                is ShowManageScheduledTransferSubmitDismissLoading -> {
                    dismissProgressAlertDialog()
                }
                is ShowManageScheduledDeleteScheduledTransfer -> {
                    deleteMultipleScheduledTransfer(it.size)
                }
                is ShowManageScheduledTransferError -> {
                    handleOnError(it.throwable)
                }
            }
        })
        viewModel.transactions.observe(this, Observer {
            it?.let {
                updateController(it)
                if (pageable.isInitialLoad) {
                    showEmptyState(it)
                    scrollToTop()
                }
            }
        })
        viewModel.transactionsTestData.observe(this, Observer {
            startViewTutorial(it)
        })
        getManageScheduledTransfers(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_toolbar, menu)
        val menuHelp = menu.findItem(R.id.menu_help)
        val menuEdit = menu.findItem(R.id.menu_edit)
        menuHelp.isVisible = true
        menuEdit.isVisible = getTransaction().isNotEmpty()
        val menuEditView = menuEdit.actionView
        val textViewMenuTitle = menuEditView.findViewById<TextView>(R.id.textViewMenuTitle)
        textViewMenuTitle.text =
            formatString(if (isSelection) R.string.action_done else R.string.action_edit)
        textViewMenuTitle.setOnClickListener {
            onOptionsItemSelected(menuEdit)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_help -> {
                isClickedHelpTutorial = true
                viewModel.getScheduledTransferTutorialTestData()
                true
            }
            R.id.menu_edit -> {
                if (!isSelection) {
                    setMultiSelection(true)
                    updateViews()
                    updateController(getTransaction())
                } else {
                    setMultiSelection(false)
                    viewModel.deSelectAll()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onClickItem(view: View, data: Transaction, position: Int) {
        if (isSelection) {
            viewModel.selectItem(position)
        } else {
            val bundle = Bundle()
            bundle.putString(
                ApprovalDetailActivity.EXTRA_TRANSACTION_DETAIL,
                JsonHelper.toJson(data)
            )
            navigator.navigate(
                (activity as ManageScheduledTransferActivity),
                ApprovalDetailActivity::class.java,
                bundle,
                isClear = false,
                isAnimated = true,
                transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_UP
            )
        }
    }

    override fun onTapToRetry() {
        getManageScheduledTransfers(isInitialLoading = false, isTapToRetry = true)
    }

    override fun onLongClickItem(view: View, data: Transaction, position: Int) {
        if (!isSelection) {
            setMultiSelection(true)
            viewModel.selectItem(position)
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
            clearTutorial()
        } else {
            if (view != null) {
                if (viewTarget.findViewById<TextView>(R.id.textViewTutorialMessage).text ==
                    getString(R.string.msg_tutorial_scheduled_click_sample)
                ) {
                    tutorialEngineUtil.startTutorial(
                        getAppCompatActivity(),
                        getFirstItemTutorial(),
                        R.layout.frame_tutorial_upper_left,
                        if (isTableView()) 0f else resources.getDimension(R.dimen.card_radius),
                        false,
                        getString(R.string.msg_tutorial_scheduled_long_click_sample),
                        GravityEnum.BOTTOM,
                        OverlayAnimationEnum.ANIM_EXPLODE
                    )
                } else if (viewTarget.findViewById<TextView>(R.id.textViewTutorialMessage).text ==
                    getString(R.string.msg_tutorial_scheduled_long_click_sample)
                ) {
                    clearTutorial()
                }
            } else {
                if (isClickedHelpTutorial) {
                    isClickedHelpTutorial = false
                    tutorialEngineUtil.startTutorial(
                        getAppCompatActivity(),
                        getFirstItemTutorial(),
                        R.layout.frame_tutorial_upper_left,
                        if (isTableView()) 0f else resources.getDimension(R.dimen.card_radius),
                        false,
                        getString(R.string.msg_tutorial_scheduled_click_sample),
                        GravityEnum.BOTTOM,
                        OverlayAnimationEnum.ANIM_EXPLODE
                    )
                }
            }
        }
    }

    private fun initEventBus() {
        eventBus.actionSyncEvent.flowable.subscribe {
            if (it.eventType == ActionSyncEvent.ACTION_UPDATE_SCHEDULED_TRANSFER_LIST) {
                setMultiSelection(false)
                getSwipeRefreshLayout().isRefreshing = true
                getManageScheduledTransfers(true)
            }
        }.addTo(disposables)
        eventBus.settingsSyncEvent.flowable.subscribe {
            if (it.eventType == SettingsSyncEvent.ACTION_PUSH_TUTORIAL_SCHEDULED_TRANSFER) {
                (activity as ManageScheduledTransferActivity).getViewPager().currentItem = 0
                isClickedHelpTutorial = true
                viewModel.getScheduledTransferTutorialTestData()
            }
        }.addTo(disposables)
    }

    private fun updateController(data: MutableList<Transaction> = getTransactionsLiveData()) {
        controller.setData(data, isSelection, pageable, isTableView())
    }

    private fun updateTutorialController(data: MutableList<Transaction> = mutableListOf()) {
        tutorialController.setData(data, false, pageable, isTableView())
    }

    private fun updateViews(count: Int = 0) {
        binding.viewApprovalsNavigation.textViewReject.text = formatString(
            R.string.params_cancel,
            count.toString()
        )
        if (count == 0) {
            binding.viewApprovalsNavigation.textViewReject.isEnabled = false
            binding.viewApprovalsNavigation.textViewReject.alpha = 0.5f
        } else {
            binding.viewApprovalsNavigation.textViewReject.isEnabled = true
            binding.viewApprovalsNavigation.textViewReject.alpha = 1f
        }
        binding.viewApprovalsNavigation.textViewSelectAll.text =
            if (count == getTransaction().size)
                formatString(R.string.action_deselect_all)
            else
                formatString(R.string.action_select_all)
    }

    private fun showEndlessProgressBar() {
        getRecyclerView().post {
            pageable.isLoadingPagination = true
            if (isTableView()) {
                if (snackBarProgressBar == null) {
                    snackBarProgressBar = viewUtil.showCustomSnackBar(
                        binding.constraintLayout,
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
        getAppCompatActivity().invalidateOptionsMenu()
        if (isTableView()) {
            binding.viewTable.linearLayoutRow.visibility(data.isNotEmpty())
        }
        if (data.isNotEmpty()) {
            if (binding.textViewState.visibility == View.VISIBLE)
                binding.textViewState.visibility = View.GONE
        } else {
            binding.textViewState.text = getString(R.string.title_no_active_scheduled_transfer)
            binding.textViewState.visibility = View.VISIBLE
        }
    }

    private fun setMultiSelection(isMultipleSelection: Boolean) {
        isSelection = isMultipleSelection
        getSwipeRefreshLayout().isEnabled = !isMultipleSelection
        binding.viewApprovalsNavigation.root.visibility(isMultipleSelection)
        activity?.invalidateOptionsMenu()
    }

    private fun deleteMultipleScheduledTransfer(size: Int) {
        val bundle = Bundle()
        bundle.putString(
            ResultLandingPageActivity.EXTRA_PAGE,
            ResultLandingPageActivity.PAGE_SCHEDULED_TRANSFER_LIST
        )
        bundle.putString(
            ResultLandingPageActivity.EXTRA_TITLE,
            getString(R.string.title_transaction_deleted)
        )
        bundle.putString(
            ResultLandingPageActivity.EXTRA_DESC,
            if (size > 1) {
                formatString(
                    R.string.msg_successfully_delete_scheduled_transfer,
                    "$size transactions"
                )
            } else {
                formatString(
                    R.string.msg_successfully_delete_scheduled_transfer,
                    "$size transaction"
                )
            }
        )
        bundle.putString(
            ResultLandingPageActivity.EXTRA_BUTTON,
            getString(R.string.action_close)
        )
        navigator.navigate(
            (activity as ManageScheduledTransferActivity),
            ResultLandingPageActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun showDeleteScheduledTransferBottomSheet() {
        val batchIds = viewModel.transactions.value?.let {
            it.filter { it.hasSelected }
                .map { it.id?.toLong() ?: 0 }
                .toMutableList()
        } ?: mutableListOf()
        val desc = if (batchIds.size > 1) {
            formatString(
                R.string.param_desc_reject_batch_transaction,
                formatString(R.string.title_cancel),
                "${batchIds.size} scheduled transactions"
            )
        } else {
            val name = getTransactionNameTitle(viewModel.transactions.value?.get(0))
            formatString(
                R.string.param_desc_transfer_reject_transaction,
                formatString(R.string.title_cancel),
                name
            )
        }
        val rejectionConfirmationBottomSheet = ConfirmationBottomSheet.newInstance(
            R.drawable.ic_close_approval,
            getString(R.string.title_confirmation),
            desc,
            formatString(R.string.action_confirm),
            formatString(R.string.action_cancel),
            null,
            null,
            R.drawable.circle_red
        )
        rejectionConfirmationBottomSheet.setOnConfirmationPageCallBack(
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
                        formatString(R.string.hint_reason_for_cancellation)
                    buttonPositive.text =
                        formatString(R.string.action_confirm)
                    viewModel.isValidReasonForCancellation.onNext(false)
                    textInputEditTextReasonForRejection.imeOptions = EditorInfo.IME_ACTION_DONE
                    textInputEditTextReasonForRejection.setRawInputType(InputType.TYPE_CLASS_TEXT)
                    validateForm(textInputEditTextReasonForRejection)
                    linearLayout.addView(view)
                }

                override fun onClickPositiveButtonDialog(data: String?, tag: String?) {
                    if (!viewModel.isValidReasonForCancellation.value.notNullable()) {
                        textInputEditTextReasonForRejection.refresh()
                    } else {
                        rejectionConfirmationBottomSheet.dismiss()
                        viewModel.deleteScheduledTransfer(
                            ScheduledTransferDeletionForm(
                                batchIds,
                                textInputEditTextReasonForRejection.text.toString()
                            )
                        )
                    }
                }

                override fun onClickNegativeButtonDialog(data: String?, tag: String?) {
                    rejectionConfirmationBottomSheet.dismiss()
                }
            })
        rejectionConfirmationBottomSheet.isCancelable = true
        rejectionConfirmationBottomSheet.show(
            childFragmentManager,
            this::class.java.simpleName
        )

    }

    private fun getTransactionNameTitle(transaction: Transaction?): String? {
        transaction?.let {
            return if (it.beneficiaryName != null) {
                it.beneficiaryName
            } else {
                viewUtil.getAccountNumberFormat(it.destinationAccountNumber)
            }
        }
        return Constant.EMPTY
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
                viewModel.isValidReasonForCancellation.onNext(it)
            }.addTo(disposables)
    }

    private fun clearTutorial() {
        viewModel.transactionsTestData.value?.clear()
        updateTutorialController()
        getRecyclerView().visibility(true)
        getRecyclerViewTutorial().visibility(false)
    }

    private fun getFirstItemTutorial(): View {
        return if (isTableView()) {
            getRecyclerViewTutorial()
                .findViewHolderForAdapterPosition(0)
                ?.itemView!!
        } else {
            getRecyclerViewTutorial()
                .findViewHolderForAdapterPosition(0)
                ?.itemView
                ?.findViewById<androidx.cardview.widget.CardView>(R.id.cardViewContent)!!
        }
    }

    private fun scrollToTop() {
        getRecyclerView().post {
            getRecyclerView().scrollToPosition(0)
        }
    }

    private fun startViewTutorial(data: MutableList<Transaction> = mutableListOf()) {
        if (isTableView()) binding.viewTable.linearLayoutRow.visibility(true)
        if (data.isNotEmpty()) {
            getRecyclerView().visibility(false)
            updateTutorialController(data)
            viewUtil.animateRecyclerView(getRecyclerViewTutorial(), true)
        }
        tutorialEngineUtil.startTutorial(
            getAppCompatActivity(),
            R.drawable.ic_tutorial_scheduled_transaction,
            getString(R.string.title_scheduled_transfers),
            getString(R.string.msg_tutorial_scheduled_landing)
        )
    }

    private fun getManageScheduledTransfers(isInitialLoading: Boolean, isTapToRetry: Boolean = false) {
        if (isTapToRetry) {
            pageable.refreshErrorPagination()
        }
        if (isInitialLoading) {
            pageable.resetPagination()
        } else {
            pageable.resetLoad()
        }
        viewModel.getManageScheduledTransfers(
            ManageScheduledTransferViewModel.STATUS_ONGOING,
            pageable = pageable,
            isInitialLoading = isInitialLoading
        )
    }

    private fun getTransaction() = viewModel.transactions.value.notNullable()

    private fun initBinding() {
        viewModel.selectedCount
            .subscribe {
                updateViews(it)
            }.addTo(disposables)
    }

    private fun init() {
        tutorialEngineUtil.setOnTutorialListener(this)
        binding.viewApprovalsNavigation.textViewApprove.visibility(false)
    }

    private fun initRecyclerView() {
        initHeaderRow()
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
                    if (!pageable.isLoadingPagination) getManageScheduledTransfers(false)
                }
            }
        )
        getRecyclerView().setController(controller)
        controller.setAdapterCallbacks(this)

        getRecyclerViewTutorial().setController(tutorialController)
        tutorialController.setAdapterCallbacks(this)
    }

    private fun initHeaderRow() {
        if (isTableView()) {
            binding.swipeRefreshLayoutTable.visibility(true)
            binding.swipeRefreshLayoutManageScheduledTransfer.visibility(false)
            val headers =
                resources.getStringArray(R.array.array_headers_manage_scheduled_transfer)
                    .toMutableList()
            headers.forEach {
                val viewRowHeader = layoutInflater.inflate(R.layout.header_table_row, null)
                val textViewHeader =
                    viewRowHeader.findViewById<AppCompatTextView>(R.id.textViewHeader)
                textViewHeader.text = it
                binding.viewTable.linearLayoutRow.addView(viewRowHeader)
            }
        }
    }

    private fun getTransactionsLiveData(): MutableList<Transaction> {
        return viewModel.transactions.value.notNullable()
    }

    private fun getRecyclerView() =
        if (isTableView()) binding.viewTable.recyclerViewTable else binding.recyclerViewApprovalManageScheduledTransfer

    private fun getRecyclerViewTutorial() =
        if (isTableView()) binding.viewTable.recyclerViewTableTutorial else binding.recyclerViewTutorial

    private fun getSwipeRefreshLayout() =
        if (isTableView()) binding.swipeRefreshLayoutTable else binding.swipeRefreshLayoutManageScheduledTransfer

    companion object {
        fun newInstance(): ManageScheduledTransferOngoingFragment {
            val fragment =
                ManageScheduledTransferOngoingFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }

    override val layoutId: Int
        get() = R.layout.fragment_manage_scheduled_transfer_ongoing

    override val viewModelClassType: Class<ManageScheduledTransferViewModel>
        get() = ManageScheduledTransferViewModel::class.java
}
