package com.unionbankph.corporate.mcd.presentation.form

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.view.View.OnTouchListener
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.widget.AppCompatButton

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.account.presentation.account_selection.AccountSelectionActivity
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.asDriver
import com.unionbankph.corporate.app.common.extension.enableButton
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.showDatePicker
import com.unionbankph.corporate.app.common.platform.bus.event.AccountSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.InputSyncEvent
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.dialog.ConfirmationBottomSheet
import com.unionbankph.corporate.app.common.widget.edittext.ImeOptionEditText
import com.unionbankph.corporate.app.common.widget.validator.validation.RxCombineValidator
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidator
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.common.presentation.callback.OnConfirmationPageCallBack
import com.unionbankph.corporate.common.presentation.constant.DateFormatEnum
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.ShowGeneralGetOrganizationName
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.databinding.ActivityCheckDepositFormBinding
import com.unionbankph.corporate.fund_transfer.presentation.bank.BankActivity
import com.unionbankph.corporate.mcd.data.form.CheckDepositForm
import com.unionbankph.corporate.mcd.presentation.confirmation.CheckDepositConfirmationActivity
import com.unionbankph.corporate.mcd.presentation.constant.CheckDepositTypeEnum
import com.unionbankph.corporate.mcd.presentation.list.CheckDepositActivity
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import io.reactivex.rxkotlin.addTo
import java.util.*
import java.util.regex.Pattern

/**
 * Created by herald25santos on 2019-10-23
 */
class CheckDepositFormActivity :
    BaseActivity<ActivityCheckDepositFormBinding, CheckDepositFormViewModel>(),
    View.OnClickListener, OnConfirmationPageCallBack, ImeOptionEditText.OnImeOptionListener {

    private lateinit var textInputLayoutAccountNumber: TextInputLayout

    private lateinit var textInputLayoutCheckNumber: TextInputLayout

    private lateinit var textInputLayoutDateOnCheck: TextInputLayout

    private lateinit var textInputEditTextCheckAccountNumber: TextInputEditText

    private lateinit var textInputEditTextCheckNumber: TextInputEditText

    private lateinit var textInputEditTextDateOnCheck: TextInputEditText

    private lateinit var progressBarCheckNumber: ProgressBar

    private lateinit var buttonAction: MaterialButton

    private lateinit var imeOptionEditText: ImeOptionEditText

    private var cancelTransactionBottomSheet: ConfirmationBottomSheet? = null

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initViewModel()
        initGeneralViewModel()
    }

    override fun onViewsBound() {
        super.onViewsBound()
        init()
        initBinding()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initEventBus()
        initListener()
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

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.textInputEditTextDepositTo -> {
                navigateAccountSelectionScreen()
            }
            R.id.textInputEditTextIssuingBank -> {
                navigateIssuingBank()
            }
        }
    }

    override fun onClickPositiveButtonDialog(data: String?, tag: String?) {
        cancelTransactionBottomSheet?.dismiss()
        cacheManager.clear(CheckDepositTypeEnum.FRONT_OF_CHECK.name.toLowerCase())
        cacheManager.clear(CheckDepositTypeEnum.BACK_OF_CHECK.name.toLowerCase())
        navigator.navigateClearUpStack(
            this,
            CheckDepositActivity::class.java,
            null,
            isClear = true,
            isAnimated = true
        )
    }

    override fun onClickNegativeButtonDialog(data: String?, tag: String?) {
        if (tag == TAG_CANCEL_TRANSACTION_DIALOG) {
            cancelTransactionBottomSheet?.dismiss()
        }
    }

    private fun initEventBus() {
        eventBus.inputSyncEvent.flowable.subscribe {
            if (it.eventType == InputSyncEvent.ACTION_INPUT_BANK_RECEIVER) {
                viewModel.bankInput.onNext(JsonHelper.fromJson(it.payload))
            }
        }.addTo(disposables)
        eventBus.accountSyncEvent.flowable.subscribe {
            if (it.eventType == AccountSyncEvent.ACTION_UPDATE_SELECTED_ACCOUNT) {
                val selectedDepositAccount = it.payload ?: Account()
                viewModel.depositToAccount.onNext(selectedDepositAccount)
            }
        }.addTo(disposables)
    }

    private fun initListener() {
        binding.textInputEditTextIssuingBank.setOnClickListener(this)
        binding.textInputEditTextDepositTo.setOnClickListener(this)
    }

    private fun initViewModel() {
        viewModel.uiState.observe(this, EventObserver {
            when (it) {
                is UiState.Error -> {
                    handleOnError(it.throwable)
                }
            }
        })
    }

    private fun initGeneralViewModel() {
        generalViewModel.state.observe(this, Observer {
            when (it) {
                is ShowGeneralGetOrganizationName -> {
                    setToolbarTitle(
                        binding.viewToolbar.textViewTitle,
                        binding.viewToolbar.textViewCorporationName,
                        formatString(R.string.title_check_deposit),
                        it.orgName
                    )
                }
            }
        })
        generalViewModel.getOrgName()
    }

    private fun navigateIssuingBank() {
        val bundle = Bundle().apply {
            putString(
                BankActivity.EXTRA_CHANNEL_ID,
                BankActivity.CHANNEL_CHECK_DEPOSIT
            )
        }
        navigator.navigate(
            this,
            BankActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun navigateAccountSelectionScreen() {
        val bundle = Bundle().apply {
            putString(
                AccountSelectionActivity.EXTRA_PAGE,
                AccountSelectionActivity.PAGE_CHECK_DEPOSIT
            )
            viewModel.depositToAccount.value?.id?.let {
                putString(AccountSelectionActivity.EXTRA_ID, it.toString())
            }
            viewModel.permissionId.value?.let {
                putString(AccountSelectionActivity.EXTRA_PERMISSION_ID, it)
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

    @SuppressLint("ClickableViewAccessibility")
    private fun init() {
        binding.viewChannelHeader.textViewChannel.text = formatString(R.string.title_check_deposit)
        binding.viewChannelHeader.imageViewChannel.setImageResource(R.drawable.ic_check_deposit)
        binding.viewChannelHeader.textViewServiceFee.text = formatString(R.string.value_service_fee_free)
        textInputLayoutAccountNumber = binding.viewCheckAccountNumber.textInputLayout
        textInputLayoutCheckNumber = binding.viewCheckNumber.textInputLayout
        textInputLayoutDateOnCheck = binding.viewDateOnCheck.textInputLayoutStartDate
        textInputEditTextCheckAccountNumber =
            binding.viewCheckAccountNumber.textInputEditText
        textInputEditTextCheckNumber = binding.viewCheckNumber.textInputEditText
        textInputEditTextDateOnCheck = binding.viewDateOnCheck.textInputEditTextStartDate
        progressBarCheckNumber = binding.viewCheckNumber.textInputEditTextProgressBar
        textInputLayoutAccountNumber.hint = formatString(R.string.hint_check_account_number)
        textInputLayoutCheckNumber.hint = formatString(R.string.hint_check_number)
        textInputLayoutDateOnCheck.hint = formatString(R.string.hint_date_front_of_check)
        textInputEditTextCheckAccountNumber.id = R.id.textInputEditTextCheckAccountNumber
        textInputEditTextCheckNumber.id = R.id.textInputEditTextCheckNumber
        viewUtil.setInputType(textInputEditTextCheckAccountNumber, "number")
        viewUtil.setInputType(textInputEditTextCheckNumber, "number")
        viewUtil.setEditTextMaxLength(
            textInputEditTextCheckAccountNumber,
            resources.getInteger(R.integer.max_length_account_number_ubp_without_mask)
        )
        viewUtil.setEditTextMaxLength(
            textInputEditTextCheckNumber,
            resources.getInteger(R.integer.max_length_check_number)
        )
        textInputEditTextDateOnCheck.setOnClickListener {
            showDateOnClickDialog()
        }
        textInputEditTextCheckAccountNumber.setOnTouchListener(OnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= textInputEditTextCheckAccountNumber.right -
                    textInputEditTextCheckAccountNumber.compoundDrawables[DRAWABLE_RIGHT].bounds.width()
                ) {
                    val checkAccountNumberDialog =
                        MaterialDialog(this@CheckDepositFormActivity).apply {
                            lifecycleOwner(this@CheckDepositFormActivity)
                            customView(R.layout.dialog_check_account_number_tip)
                        }
                    val buttonClose =
                        checkAccountNumberDialog.view.findViewById<AppCompatButton>(R.id.buttonClose)
                    buttonClose.setOnClickListener { checkAccountNumberDialog.dismiss() }
                    checkAccountNumberDialog.window?.attributes?.windowAnimations =
                        R.style.SlideUpAnimation
                    checkAccountNumberDialog.window?.setGravity(Gravity.CENTER)
                    checkAccountNumberDialog.show()
                    return@OnTouchListener true
                }
            }
            false
        })
        textInputEditTextCheckNumber.setOnTouchListener(OnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= textInputEditTextCheckNumber.right -
                    textInputEditTextCheckNumber.compoundDrawables[DRAWABLE_RIGHT].bounds.width()
                ) {
                    val checkNumberDialog =
                        MaterialDialog(this@CheckDepositFormActivity).apply {
                            lifecycleOwner(this@CheckDepositFormActivity)
                            customView(R.layout.dialog_check_number_tip)
                        }
                    val buttonClose =
                        checkNumberDialog.view.findViewById<AppCompatButton>(R.id.buttonClose)
                    buttonClose.setOnClickListener { checkNumberDialog.dismiss() }
                    checkNumberDialog.window?.attributes?.windowAnimations =
                        R.style.SlideUpAnimation
                    checkNumberDialog.window?.setGravity(Gravity.CENTER)
                    checkNumberDialog.show()
                    return@OnTouchListener true
                }
            }
            false
        })
        initImeOptionEditText()
        // initRxSearchEventListener()
    }

    private fun initBinding() {
        viewModel.setCheckDepositUpload(intent.getStringExtra(EXTRA_CHECK_DEPOSIT_UPLOAD))
        viewModel.checkDepositForm
            .subscribe {
                populateCheckFields(it)
            }.addTo(disposables)
        viewModel.bankInput
            .subscribe {
                binding.textInputEditTextIssuingBank.setText(it.bank)
            }.addTo(disposables)
        viewModel.dateOnCheckInput
            .subscribe {
                textInputEditTextDateOnCheck.setText(
                    viewUtil.getDateFormatByCalendar(
                        it,
                        DateFormatEnum.DATE_FORMAT_DATE.value
                    )
                )
            }.addTo(disposables)
        viewModel.depositToAccount
            .subscribe {
                binding.textInputEditTextDepositTo.setText(
                    (it.name + "\n" + viewUtil.getAccountNumberFormat(it.accountNumber))
                )
            }.addTo(disposables)

        textInputEditTextCheckAccountNumber.asDriver()
            .subscribe {
                viewModel.checkDepositForm.value?.sourceAccount = it
            }.addTo(disposables)
        textInputEditTextCheckNumber.asDriver()
            .subscribe {
                viewModel.checkDepositForm.value?.checkNumber = it
            }.addTo(disposables)
        binding.etAmount.asDriver()
            .subscribe {
                viewModel.checkDepositForm.value?.checkAmount = binding.etAmount.getNumericValue().toString()
            }.addTo(disposables)
        viewModel.checkDepositFormOutput
            .subscribe {
                navigateCheckDepositConfirmationScreen(it)
            }.addTo(disposables)
        binding.tieRemarks.asDriver()
            .subscribe {
                viewModel.checkDepositForm.value?.remarks = it
            }.addTo(disposables)
    }

    private fun populateCheckFields(checkDepositForm: CheckDepositForm) {
        textInputEditTextCheckAccountNumber.setText(checkDepositForm.sourceAccount)
        textInputEditTextCheckNumber.setText(checkDepositForm.checkNumber)
    }

    private fun initImeOptionEditText() {
        imeOptionEditText =
            ImeOptionEditText()
        imeOptionEditText.addEditText(
            textInputEditTextCheckAccountNumber,
            textInputEditTextCheckNumber,
            binding.etAmount
        )
        imeOptionEditText.setOnImeOptionListener(this)
        imeOptionEditText.startListener()
    }

    private fun showDateOnClickDialog() {
        val calendar = Calendar.getInstance()
        showDatePicker(
            minDate = Calendar.getInstance().apply { add(Calendar.DATE, -180) },
            maxDate = calendar,
            calendar = viewModel.dateOnCheckInput.value ?: calendar,
            callback = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, monthOfYear, dayOfMonth)
                viewModel.dateOnCheckInput.onNext(selectedDate)
            }
        )
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

    private fun validateForm(isValueChanged: Boolean) {
        val textInputEditTextIssuingBankObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = isValueChanged,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.textInputEditTextIssuingBank
        )
        val textInputEditTextCheckAccountNumberObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = isValueChanged,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_account_number_ubp_without_mask),
            editText = textInputEditTextCheckAccountNumber
        )
        val textInputEditTextCheckNumberObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = isValueChanged,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_check_number),
            editText = textInputEditTextCheckNumber
        )
        val amountLimitObservable = viewUtil.rxTextChangesAmount(
            true,
            isValueChanged,
            binding.etAmount,
            RxValidator.PatternMatch(
                formatString(R.string.error_input_valid_amount),
                Pattern.compile(ViewUtil.REGEX_FORMAT_VALID_AMOUNT)
            ),
            RxValidator.PatternMatch(
                formatString(R.string.error_specific_field, formatString(R.string.title_amount)),
                Pattern.compile(ViewUtil.REGEX_FORMAT_AMOUNT)
            ),
            RxValidator.PatternMatch(
                formatString(R.string.error_check_deposit_maximum_amount),
                Pattern.compile(ViewUtil.REGEX_FORMAT_AMOUNT_CHECK_DEPOSIT)
            )
        )
        val textInputEditTextDateOnCheckObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = isValueChanged,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = textInputEditTextDateOnCheck
        )
        val textInputEditTextDepositToObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = isValueChanged,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.textInputEditTextDepositTo
        )
        initSetError(textInputEditTextIssuingBankObservable)
        initSetError(textInputEditTextCheckAccountNumberObservable)
        initSetError(textInputEditTextDateOnCheckObservable)
        initSetError(textInputEditTextDepositToObservable)
        initSetError(textInputEditTextCheckNumberObservable)
        initSetError(amountLimitObservable)

        RxCombineValidator(
            textInputEditTextIssuingBankObservable,
            textInputEditTextCheckAccountNumberObservable,
            textInputEditTextCheckNumberObservable,
            textInputEditTextDateOnCheckObservable,
            amountLimitObservable,
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

    private fun initEditTextDefaultValue() {
        setDefaultValue(binding.textInputEditTextIssuingBank)
        setDefaultValue(textInputEditTextCheckAccountNumber)
        setDefaultValue(textInputEditTextCheckNumber)
        setDefaultValue(textInputEditTextDateOnCheck)
        setDefaultValue(binding.etAmount)
        setDefaultValue(binding.textInputEditTextDepositTo)
    }

    private fun setDefaultValue(textInputEditText: EditText) {
        if (textInputEditText.length() != 0) {
            textInputEditText.setText(textInputEditText.text.toString())
        }
    }

    private fun clearFormFocus() {
        binding.constraintLayoutParent.requestFocus()
        binding.constraintLayoutParent.isFocusableInTouchMode = true
    }

    private fun clickNext() {
        viewUtil.dismissKeyboard(this)
        if (isValidForm) {
            clearFormFocus()
            viewModel.submitForm()
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

    private fun navigateCheckDepositConfirmationScreen(checkDepositForm: String) {
        navigator.navigate(
            this,
            CheckDepositConfirmationActivity::class.java,
            Bundle().apply {
                putString(
                    CheckDepositConfirmationActivity.EXTRA_FORM,
                    checkDepositForm
                )
            },
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    companion object {

        const val EXTRA_CHECK_DEPOSIT_UPLOAD = "check_deposit_upload"

        const val DRAWABLE_RIGHT = 2
        const val TAG_CANCEL_TRANSACTION_DIALOG = "cancel_transaction_dialog"
    }

    override val viewModelClassType: Class<CheckDepositFormViewModel>
        get() = CheckDepositFormViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityCheckDepositFormBinding
        get() = ActivityCheckDepositFormBinding::inflate
}
