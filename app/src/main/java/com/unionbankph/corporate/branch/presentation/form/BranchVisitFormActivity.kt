package com.unionbankph.corporate.branch.presentation.form

import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.account.presentation.account_selection.AccountSelectionActivity
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.platform.bus.event.AccountSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.InputSyncEvent
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.dialog.ConfirmationBottomSheet
import com.unionbankph.corporate.app.common.widget.edittext.ImeOptionEditText
import com.unionbankph.corporate.app.common.widget.validator.validation.RxCombineValidator
import com.unionbankph.corporate.branch.data.model.Branch
import com.unionbankph.corporate.branch.presentation.branch.BranchActivity
import com.unionbankph.corporate.branch.presentation.confirmation.BranchVisitConfirmationActivity
import com.unionbankph.corporate.branch.presentation.model.BranchTransactionForm
import com.unionbankph.corporate.branch.presentation.model.BranchVisitConfirmationForm
import com.unionbankph.corporate.branch.presentation.transaction.BranchVisitTransactionFormActivity
import com.unionbankph.corporate.branch.presentation.transactionlist.BranchTransactionActivity
import com.unionbankph.corporate.common.presentation.callback.OnConfirmationPageCallBack
import com.unionbankph.corporate.common.presentation.constant.DateFormatEnum
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.ShowGeneralGetOrganizationName
import com.unionbankph.corporate.databinding.ActivityBranchVisitFormBinding
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import io.reactivex.rxkotlin.addTo
import java.util.*

class BranchVisitFormActivity :
    BaseActivity<ActivityBranchVisitFormBinding, BranchVisitFormViewModel>(),
    ImeOptionEditText.OnImeOptionListener, OnConfirmationPageCallBack {

    private lateinit var imeOptionEditText: ImeOptionEditText

    private lateinit var textInputLayoutTransactionDate: TextInputLayout

    private lateinit var textInputLayoutDepositTo: TextInputLayout

    private lateinit var textInputEditTextTransactionDate: TextInputEditText

    private lateinit var textInputEditTextDepositTo: TextInputEditText

    private lateinit var buttonAction: MaterialButton

    private var selectedDepositAccount: Account? = null

    private var cancelTransactionBottomSheet: ConfirmationBottomSheet? = null

    private var deleteTransactionsBottomSheet: ConfirmationBottomSheet? = null

    private var noBranchTransactionBottomSheet: ConfirmationBottomSheet? = null

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initChannelView()
        initCheckDepositForm()
        initImeOptionEditText()
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initViewModel()
        initGeneralViewModel()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initEventBus()
        binding.textInputEditTextBranch.setOnClickListener {
            navigateBranchScreen()
        }
        binding.buttonAddTransaction.setOnClickListener {
            navigateBranchTransactionFormScreen()
        }
        binding.imageViewClose.setOnClickListener {
            showClearTransactionsBottomSheet()
        }
        binding.textViewBranchTransaction.setOnClickListener {
            navigateBranchTransactionScreen()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.menu_action_button -> {
                clickNext()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_toolbar, menu)
        val menuActionButton = menu.findItem(R.id.menu_action_button)
        val menuView = menuActionButton.actionView
        val helpMenu = menu.findItem(R.id.menu_help)
        buttonAction = menuView.findViewById(R.id.buttonAction)
        buttonAction.text = getString(R.string.action_next)
        buttonAction.enableButton(true)
        helpMenu.isVisible = false
        menuActionButton.isVisible = true
        buttonAction.setOnClickListener { onOptionsItemSelected(menuActionButton) }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onBackPressed() {
        showCancelTransactionBottomSheet()
    }

    override fun onClickPositiveButtonDialog(data: String?, tag: String?) {
        when (tag) {
            TAG_CANCEL_TRANSACTION_DIALOG -> {
                cancelTransactionBottomSheet?.dismiss()
                super.onBackPressed()
            }
            TAG_DELETE_TRANSACTION_DIALOG -> {
                deleteTransactionsBottomSheet?.dismiss()
                viewModel.clearBranchTransactionForm()
            }
        }
    }

    override fun onClickNegativeButtonDialog(data: String?, tag: String?) {
        when (tag) {
            TAG_CANCEL_TRANSACTION_DIALOG -> cancelTransactionBottomSheet?.dismiss()
            TAG_DELETE_TRANSACTION_DIALOG -> deleteTransactionsBottomSheet?.dismiss()
            TAG_NO_BRANCH_TRANSACTION_DIALOG -> noBranchTransactionBottomSheet?.dismiss()
        }
    }

    private fun initEventBus() {
        eventBus.inputSyncEvent.flowable.subscribe {
            if (it.eventType == InputSyncEvent.ACTION_INPUT_BRANCH) {
                it.payload?.let {
                    val branch = JsonHelper.fromJson<Branch>(it)
                    viewModel.setBranch(branch)
                    binding.textInputEditTextBranch.setText(
                        ("<b>${branch.name}</b><br>${branch.address.notEmpty()}").toHtmlSpan()
                    )
                }
            } else if (it.eventType == InputSyncEvent.ACTION_ADD_BRANCH_TRANSACTION) {
                it.payload?.let { branchTransactionFormString ->
                    val branchTransactionForm =
                        JsonHelper.fromJson<BranchTransactionForm>(branchTransactionFormString)
                    viewModel.addBranchTransactionForm(branchTransactionForm)
                    showBranchTransaction(getBranchTransactions())
                }
            }
        }.addTo(disposables)
        eventBus.accountSyncEvent.flowable.subscribe {
            if (it.eventType == AccountSyncEvent.ACTION_UPDATE_SELECTED_ACCOUNT) {
                textInputEditTextDepositTo.setText(it.payload?.accountNumber)
            }
        }.addTo(disposables)
        eventBus.actionSyncEvent.flowable.subscribe {
            if (it.eventType == ActionSyncEvent.ACTION_UPDATE_BRANCH_TRANSACTION_LIST) {
                it.payload?.let {
                    val newData = JsonHelper.fromListJson<BranchTransactionForm>(it)
                    viewModel.setBranchTransactions(newData)
                }
            }
        }.addTo(disposables)
    }

    private fun initViewModel() {
        viewModel.branchVisitFormState.observe(this, Observer {
            when (it) {
                is ShowBranchVisitFormProceedToConfirmation -> {
                    navigateConfirmationScreen(it.branchVisitConfirmationForm)
                }
            }
        })
        viewModel.branchTransactionsForm.observe(this, Observer {
            showBranchTransaction(it)
        })
    }

    private fun showBranchTransaction(branchTransactionsForm: MutableList<BranchTransactionForm>) {
        val isShown = branchTransactionsForm.size > 0
        binding.textViewBranchTransactionState.setVisible(!isShown)
        binding.textViewBranchTransaction.setVisible(isShown)
        binding.imageViewClose.visibility(isShown)
        binding.textViewBranchTransaction.text =
            formatString(
                if (branchTransactionsForm.size > 1)
                    R.string.param_deposit_transactions
                else
                    R.string.param_deposit_transaction,
                formatString(
                    R.string.param_color,
                    convertColorResourceToHex(getAccentColor()),
                    branchTransactionsForm.size
                )
            ).toHtmlSpan()
    }

    private fun initGeneralViewModel() {
        generalViewModel.state.observe(this, Observer {
            when (it) {
                is ShowGeneralGetOrganizationName -> {
                    setToolbarTitle(
                        binding.viewToolbar.textViewTitle,
                        binding.viewToolbar.textViewCorporationName,
                        formatString(R.string.title_cash_or_check_deposit),
                        it.orgName
                    )
                }
            }
        })
        generalViewModel.getOrgName()
    }

    private fun initChannelView() {
        binding.viewChannelHeader.textViewChannel.visibility = View.GONE
        binding.viewChannelHeader.imageViewChannel.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        binding.viewChannelHeader.imageViewChannel.setImageResource(R.drawable.ic_channel_ubp)
        binding.viewChannelHeader.textViewServiceFee.text = formatString(R.string.value_service_fee_free)
    }

    private fun initCheckDepositForm() {
        textInputLayoutTransactionDate =
            binding.viewTransactionDate.textInputLayoutStartDate
        textInputLayoutTransactionDate.hint = formatString(R.string.hint_transaction_date)
        textInputEditTextTransactionDate =
            binding.viewTransactionDate.textInputEditTextStartDate
        textInputLayoutDepositTo = binding.viewDepositToForm.textInputLayoutTransferTo
        textInputLayoutDepositTo.hint = formatString(R.string.hint_deposit_to)
        textInputEditTextDepositTo =
            binding.viewDepositToForm.textInputEditTextTransferTo
        textInputLayoutDepositTo.setContextCompatBackground(R.color.colorTransparent)
        textInputEditTextDepositTo.isEnabled = true
        viewUtil.setEditTextMaskListener(
            textInputEditTextDepositTo,
            getString(R.string.hint_account_number_format)
        )
        textInputEditTextTransactionDate.setOnClickListener {
            showTransactionDateDialog()
        }
        val imageViewTransferTo =
            binding.viewDepositToForm.imageViewTransferTo
        imageViewTransferTo.setOnClickListener {
            navigateAccountSelectionScreen()
        }
    }

    private fun validateForm(isValueChanged: Boolean) {
        val textInputEditTextTransactionDateObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = isValueChanged,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = textInputEditTextTransactionDate
        )
        val textInputEditTextBranchObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = isValueChanged,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.textInputEditTextBranch
        )
        val textInputEditTextDepositToObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = isValueChanged,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = textInputEditTextDepositTo
        )
        initSetError(textInputEditTextTransactionDateObservable)
        initSetError(textInputEditTextBranchObservable)
        initSetError(textInputEditTextDepositToObservable)

        RxCombineValidator(
            textInputEditTextTransactionDateObservable,
            textInputEditTextBranchObservable,
            textInputEditTextDepositToObservable
        )
            .asObservable()
            .distinctUntilChanged()
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .doOnNext {
                isValidForm = it
                if (!isValueChanged && isValidForm) {
                    clickNext()
                }
                if (!isValueChanged && !isValidForm) {
                    showMissingFieldDialog()
                }
            }
            .doOnComplete {
                if (!isValueChanged) {
                    validateForm(true)
                }
            }
            .subscribe()
            .addTo(disposables)
        initEditTextDefaultValue()
    }

    private fun initImeOptionEditText() {
        imeOptionEditText =
            ImeOptionEditText()
        imeOptionEditText.addEditText(
            textInputEditTextDepositTo,
            binding.textInputEditTextRemarks
        )
        imeOptionEditText.setOnImeOptionListener(this)
        imeOptionEditText.startListener()
    }

    private fun initEditTextDefaultValue() {
        setDefaultValue(textInputEditTextTransactionDate)
        setDefaultValue(binding.textInputEditTextBranch)
        setDefaultValue(textInputEditTextDepositTo)
    }

    private fun setDefaultValue(textInputEditText: EditText) {
        if (textInputEditText.length() != 0) {
            textInputEditText.setText(textInputEditText.text.toString())
        }
    }

    private fun clickNext() {
        viewUtil.dismissKeyboard(this)
        if (isValidForm) {
            clearFormFocus()
            submitForm()
        } else {
            if (isInitialSubmitForm) {
                isInitialSubmitForm = false
                validateForm(false)
            } else {
                showMissingFieldDialog()
            }
        }
    }

    private fun showMissingFieldDialog() {
        showMaterialDialogError(
            message = formatString(R.string.msg_error_missing_fields)
        )
    }

    private fun showTransactionDateDialog() {
        val calendar = if (textInputEditTextTransactionDate.length() > 0) {
            textInputEditTextTransactionDate.tag.toString().convertToCalendar()
        } else {
            Calendar.getInstance()
        }
        showDatePicker(
            minDate = Calendar.getInstance(),
            maxDate = Calendar.getInstance().apply { add(Calendar.WEEK_OF_MONTH, 2) },
            calendar = calendar,
            callback = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                textInputEditTextTransactionDate.tag = viewUtil.getDateFormatByCalendar(
                    selectedDate,
                    DateFormatEnum.DATE_FORMAT_ISO_DATE.value
                )
                textInputEditTextTransactionDate.setText(
                    viewUtil.getDateFormatByCalendar(
                        selectedDate,
                        DateFormatEnum.DATE_FORMAT_DATE.value
                    )
                )
            }
        )
    }

    private fun clearFormFocus() {
        binding.constraintLayoutParent.requestFocus()
        binding.constraintLayoutParent.isFocusableInTouchMode = true
    }

    private fun showCancelTransactionBottomSheet() {
        cancelTransactionBottomSheet = ConfirmationBottomSheet.newInstance(
            R.drawable.ic_warning_white,
            formatString(R.string.title_cancel_transaction),
            formatString(R.string.msg_cancel_fund_transfer_transaction),
            formatString(R.string.action_yes),
            formatString(R.string.action_no)
        )
        cancelTransactionBottomSheet?.setOnConfirmationPageCallBack(this)
        cancelTransactionBottomSheet?.show(
            supportFragmentManager,
            TAG_CANCEL_TRANSACTION_DIALOG
        )
    }

    private fun showClearTransactionsBottomSheet() {
        deleteTransactionsBottomSheet = ConfirmationBottomSheet.newInstance(
            R.drawable.ic_warning_white,
            formatString(R.string.title_delete_deposit_transactions),
            formatString(
                R.string.msg_delete_deposit_transactions,
                viewModel.branchTransactionsForm.value?.size
            ),
            formatString(R.string.action_yes),
            formatString(R.string.action_no)
        )
        deleteTransactionsBottomSheet?.setOnConfirmationPageCallBack(this)
        deleteTransactionsBottomSheet?.show(
            supportFragmentManager,
            TAG_DELETE_TRANSACTION_DIALOG
        )
    }

    private fun showNoBranchTransactionsBottomSheet() {
        noBranchTransactionBottomSheet = ConfirmationBottomSheet.newInstance(
            R.drawable.ic_warning_white,
            formatString(R.string.title_no_deposit_transaction),
            formatString(R.string.msg_no_deposit_branch_transaction),
            null,
            formatString(R.string.action_no)
        )
        noBranchTransactionBottomSheet?.setOnConfirmationPageCallBack(this)
        noBranchTransactionBottomSheet?.show(
            supportFragmentManager,
            TAG_NO_BRANCH_TRANSACTION_DIALOG
        )
    }

    private fun navigateBranchScreen() {
        navigator.navigate(
            this,
            BranchActivity::class.java,
            null,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun navigateAccountSelectionScreen() {
        val bundle = Bundle().apply {
            putString(
                AccountSelectionActivity.EXTRA_PAGE,
                AccountSelectionActivity.PAGE_BRANCH_VISIT
            )
            selectedDepositAccount?.let {
                putString(
                    AccountSelectionActivity.EXTRA_ID,
                    selectedDepositAccount?.id?.toString()
                )
            }
        }
        navigator.navigate(
            this,
            AccountSelectionActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun navigateBranchTransactionFormScreen() {
        navigator.navigate(
            this,
            BranchVisitTransactionFormActivity::class.java,
            null,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun navigateBranchTransactionScreen() {
        val bundle = Bundle().apply {
            putString(
                BranchTransactionActivity.EXTRA_LIST,
                JsonHelper.toJson(getBranchTransactions())
            )
        }
        navigator.navigate(
            this,
            BranchTransactionActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun getBranchTransactions(): MutableList<BranchTransactionForm> {
        return viewModel.branchTransactionsForm.value.notNullable()
    }

    private fun submitForm() {
        if (getBranchTransactions().size > 0) {
            viewModel.proceedToConfirmation(
                textInputEditTextTransactionDate.tag.toString(),
                textInputEditTextDepositTo.text.toString(),
                intent.getStringExtra(EXTRA_CHANNEL).notNullable(),
                binding.textInputEditTextRemarks.text.toString()
            )
        } else {
            showNoBranchTransactionsBottomSheet()
        }
    }

    private fun navigateConfirmationScreen(branchVisitConfirmationForm: BranchVisitConfirmationForm) {
        val bundle = Bundle().apply {
            putString(
                BranchVisitConfirmationActivity.EXTRA_CONFIRMATION_FORM,
                JsonHelper.toJson(branchVisitConfirmationForm)
            )
            putString(
                BranchVisitConfirmationActivity.EXTRA_TRANSACTION_LIST,
                JsonHelper.toJson(viewModel.branchTransactionsForm.value)
            )
        }
        navigator.navigate(
            this,
            BranchVisitConfirmationActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    companion object {
        const val EXTRA_CHANNEL = "channel"
        const val TAG_CANCEL_TRANSACTION_DIALOG = "cancel_transaction_dialog"
        const val TAG_DELETE_TRANSACTION_DIALOG = "delete_transaction_dialog"
        const val TAG_NO_BRANCH_TRANSACTION_DIALOG = "no_branch_transaction_dialog"
    }

    override val viewModelClassType: Class<BranchVisitFormViewModel>
        get() = BranchVisitFormViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityBranchVisitFormBinding
        get() = ActivityBranchVisitFormBinding::inflate
}
