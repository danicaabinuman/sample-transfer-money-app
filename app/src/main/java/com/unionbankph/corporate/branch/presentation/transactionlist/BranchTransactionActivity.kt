package com.unionbankph.corporate.branch.presentation.transactionlist

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.platform.bus.event.InputSyncEvent
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.dialog.ConfirmationBottomSheet
import com.unionbankph.corporate.branch.presentation.model.BranchTransaction
import com.unionbankph.corporate.branch.presentation.model.BranchTransactionForm
import com.unionbankph.corporate.branch.presentation.transaction.BranchVisitTransactionFormActivity
import com.unionbankph.corporate.branch.presentation.transactiondetail.BranchTransactionDetailActivity
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.common.presentation.callback.OnConfirmationPageCallBack
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.databinding.ActivityBranchTransactionBinding
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_branch_transaction.*
import kotlinx.android.synthetic.main.widget_transparent_appbar.*


class BranchTransactionActivity :
    BaseActivity<ActivityBranchTransactionBinding, BranchTransactionViewModel>(),
    EpoxyAdapterCallback<BranchTransactionForm> {

    private val controller by lazyFast {
        BranchTransactionController(this, viewUtil, autoFormatUtil)
    }

    private val isDisableAction by lazyFast {
        intent.getBooleanExtra(EXTRA_DISABLE_ACTION, false)
    }

    private var isShowMenuEdit: Boolean = false

    private var deleteTransactionsBottomSheet: ConfirmationBottomSheet? = null

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolBar.toolbar, binding.viewToolBar.appBarLayout)
        setToolbarTitle(binding.viewToolBar.tvToolbar, formatString(R.string.title_branch_transaction))
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initRecyclerView()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initEventBus()
        binding.buttonDelete.setOnClickListener {
            showDeleteTransactionsBottomSheet()
        }
        binding.buttonSelectAll.setOnClickListener {
            if (viewModel.isSelectedAll()) {
                viewModel.deSelectAll()
            } else {
                viewModel.selectAll()
            }
            updateViews()
        }
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initViewModel()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_toolbar, menu)
        val editMenu = menu.findItem(R.id.menu_edit)
        editMenu.isVisible = !isDisableAction && isShowMenuEdit
        val menuEditView = editMenu.actionView
        val textViewMenuTitle = menuEditView.findViewById<TextView>(R.id.textViewMenuTitle)
        textViewMenuTitle.text = formatString(
            if (!viewModel.getSelection())
                R.string.action_edit
            else
                R.string.action_done
        )
        textViewMenuTitle.setOnClickListener {
            onOptionsItemSelected(editMenu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.menu_edit -> {
                if (viewModel.getSelection()) {
                    viewModel.clearSelection()
                    viewModel.activateSelection(false)
                    updateViews()
                } else {
                    viewModel.activateSelection(!viewModel.getSelection())
                }
                updateViews()
                invalidateOptionsMenu()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initViewModel() {
        viewModel.branchTransactionState.observe(this, Observer {
            when (it) {
                is ShowBranchTransactionLoading -> {
                    showLoading(
                        binding.viewLoadingState.root,
                        null,
                        binding.recyclerViewBranchTransaction,
                        binding.textViewState
                    )
                }
                is ShowBranchTransactionDismissLoading -> {
                    dismissLoading(
                        binding.viewLoadingState.root,
                        null,
                        binding.recyclerViewBranchTransaction
                    )
                }
                is ShowBranchTransactionEditedItem -> {
                    updateController(getBranchTransactions())
                }
                is ShowBranchTransactionError -> {
                    handleOnError(it.throwable)
                }
            }
        })
        viewModel.setBranchTransactions(intent.getStringExtra(EXTRA_LIST).notNullable())
        viewModel.branchTransactionsForm.observe(this, Observer {
            isShowMenuEdit = true
            updateController(it)
            if (viewModel.getSelection()) {
                binding.buttonDelete.text =
                    formatString(R.string.params_delete, viewModel.getSelectedCount())
            }
            invalidateOptionsMenu()
        })
        viewModel.isSelection.observe(this, Observer {
            binding.buttonDelete.visibility(it)
            binding.buttonSelectAll.visibility(it)
        })
    }

    override fun onClickItem(view: View, data: BranchTransactionForm, position: Int) {
        if (viewModel.getSelection()) {
            viewModel.setSelectedItem(data, position)
            updateViews()
        } else {
            if (isDisableAction) {
                navigationBranchTransactionDetail(data)
            } else {
                navigationBranchVisitTransactionForm(data, position)
            }
        }
    }

    override fun onLongClickItem(view: View, data: BranchTransactionForm, position: Int) {
        if (isDisableAction) return
        viewModel.activateSelection(!viewModel.getSelection())
        if (viewModel.getSelection()) {
            viewModel.setSelectedItem(data, position)
        } else {
            viewModel.clearSelection()
        }
        updateViews()
    }

    private fun initEventBus() {
        eventBus.inputSyncEvent.flowable.subscribe {
            if (it.eventType == InputSyncEvent.ACTION_EDIT_BRANCH_TRANSACTION) {
                it.payload?.let { branchTransactionString ->
                    val branchTransaction =
                        JsonHelper.fromJson<BranchTransaction>(branchTransactionString)
                    branchTransaction.branchTransactionForm?.let {
                        viewModel.editBranchTransactionByPosition(branchTransaction.position, it)
                    }
                }
            }
        }.addTo(disposables)
    }

    private fun updateViews() {
        updateButton()
        updateController(getBranchTransactions())
    }

    private fun updateController(data: MutableList<BranchTransactionForm>) {
        controller.setData(data, viewModel.getSelection())
    }

    private fun updateButton() {
        binding.buttonDelete.text =
            formatString(R.string.params_delete, viewModel.getSelectedCount())
        binding.buttonSelectAll.text = formatString(
            if (viewModel.isSelectedAll())
                R.string.action_deselect_all
            else
                R.string.action_select_all
        )
        binding.buttonDelete.isEnabled = viewModel.getSelectedCount() > 0
    }

    private fun initRecyclerView() {
        controller.setAdapterCallbacks(this)
        binding.recyclerViewBranchTransaction.setController(controller)
    }

    private fun showDeleteTransactionsBottomSheet(position: Int = 0) {
        val isSelection = viewModel.getSelection()
        deleteTransactionsBottomSheet = ConfirmationBottomSheet.newInstance(
            R.drawable.ic_warning_white,
            formatString(
                if (isSelection)
                    R.string.title_delete_deposit_transactions
                else
                    R.string.title_delete_deposit_transaction
            ),
            if (isSelection) {
                formatString(
                    R.string.msg_delete_deposit_transactions,
                    viewModel.getSelectedCount()
                )
            } else {
                formatString(
                    R.string.msg_delete_deposit_transaction
                )
            },
            formatString(R.string.action_yes),
            formatString(R.string.action_no)
        )
        deleteTransactionsBottomSheet?.setOnConfirmationPageCallBack(
            object : OnConfirmationPageCallBack {
                override fun onClickPositiveButtonDialog(data: String?, tag: String?) {
                    deleteTransactionsBottomSheet?.dismiss()
                    if (viewModel.getSelection()) {
                        viewModel.deleteSelectedItems()
                        updateViews()
                        if (getBranchTransactions().size == 0) onBackPressed()
                    } else {
                        viewModel.deleteItem(position)
                        updateViews()
                        if (getBranchTransactions().size == 0) onBackPressed()
                    }
                }

                override fun onClickNegativeButtonDialog(data: String?, tag: String?) {
                    deleteTransactionsBottomSheet?.dismiss()
                }
            })
        deleteTransactionsBottomSheet?.show(
            supportFragmentManager,
            TAG_DELETE_TRANSACTION_DIALOG
        )
    }

    private fun getBranchTransactions(): MutableList<BranchTransactionForm> {
        return viewModel.branchTransactionsForm.value.notNullable()
    }

    private fun navigationBranchVisitTransactionForm(
        branchTransactionForm: BranchTransactionForm,
        position: Int
    ) {
        val bundle = Bundle().apply {
            putBoolean(BranchVisitTransactionFormActivity.EXTRA_IS_EDIT, true)
            putString(
                BranchVisitTransactionFormActivity.EXTRA_BRANCH_TRANSACTION,
                JsonHelper.toJson(branchTransactionForm)
            )
            putInt(BranchVisitTransactionFormActivity.EXTRA_POSITION, position)
        }
        navigator.navigate(
            this,
            BranchVisitTransactionFormActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun navigationBranchTransactionDetail(branchTransactionForm: BranchTransactionForm) {
        val bundle = Bundle().apply {
            putString(
                BranchTransactionDetailActivity.EXTRA_BRANCH_TRANSACTION_DETAIL,
                JsonHelper.toJson(branchTransactionForm)
            )
        }
        navigator.navigate(
            this,
            BranchTransactionDetailActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    companion object {
        const val EXTRA_LIST = "list"
        const val EXTRA_DISABLE_ACTION = "disable_action"
        const val TAG_DELETE_TRANSACTION_DIALOG = "delete_transaction_dialog"
    }

    override val layoutId: Int
        get() = R.layout.activity_branch_transaction

    override val viewModelClassType: Class<BranchTransactionViewModel>
        get() = BranchTransactionViewModel::class.java

}
