package com.unionbankph.corporate.branch.presentation.transaction

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.widget.edittext.ImeOptionEditText
import com.unionbankph.corporate.app.common.widget.validator.validation.RxCombineValidator
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidator
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.branch.presentation.constant.BranchVisitTypeEnum
import com.unionbankph.corporate.branch.presentation.model.BranchTransactionForm
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.constant.DateFormatEnum
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_branch_visit_transaction_form.*
import kotlinx.android.synthetic.main.widget_edit_text_start_date.*
import kotlinx.android.synthetic.main.widget_transparent_appbar.*
import java.text.DecimalFormat
import java.util.*
import java.util.regex.Pattern

class BranchVisitTransactionFormActivity :
    BaseActivity<BranchVisitTransactionFormViewModel>(R.layout.activity_branch_visit_transaction_form),
    ImeOptionEditText.OnImeOptionListener {

    private lateinit var textInputEditTextCheckAccountNumber: TextInputEditText

    private lateinit var textInputEditTextCheckNumber: TextInputEditText

    private lateinit var imeOptionEditText: ImeOptionEditText

    private lateinit var buttonAction: Button

    private var formDisposable: Disposable? = null

    private val isEdit by lazyFast { intent.getBooleanExtra(EXTRA_IS_EDIT, false) }

    private var branchTransactionForm = BranchTransactionForm()

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
        setToolbarTitle(tvToolbar, formatString(R.string.title_add_a_transaction))
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel =
            ViewModelProviders.of(
                this,
                viewModelFactory
            )[BranchVisitTransactionFormViewModel::class.java]
        viewModel.transactionFormState.observe(this, androidx.lifecycle.Observer {
            when (it) {
                is ShowBranchVisitTransactionEdit -> {
                    viewUtil.dismissKeyboard(this)
                    onBackPressed()
                }
            }
        })
        viewModel.branchTransactionFormState.observe(this, androidx.lifecycle.Observer {
            viewUtil.dismissKeyboard(this)
            onBackPressed()
        })
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initTransactionForm()
        initImeOptionEditText()
        initEditForm()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        radioGroupTypeOfDeposit.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioButtonCash -> {
                    initCash()
                }
                R.id.radioButtonCheck -> {
                    initCheck(radioButtonUnionBank.isChecked)
                }
            }
        }
        radioGroupCheckType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioButtonUnionBank -> {
                    initCheck(true)
                }
                R.id.radioButtonOtherBank -> {
                    initCheck(false)
                }
            }
        }
        textInputEditTextStartDate.setOnClickListener {
            showDateOnClickDialog()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.menu_action_button -> {
                clickAdd()
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
        buttonAction.text = formatString(
            if (isEdit)
                R.string.action_edit
            else
                R.string.action_add
        )
        buttonAction.enableButton(true)
        helpMenu.isVisible = false
        menuActionButton.isVisible = true
        buttonAction.setOnClickListener { onOptionsItemSelected(menuActionButton) }
        return super.onCreateOptionsMenu(menu)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initTransactionForm() {
        textInputLayoutRemarks.setContextCompatBackground(R.color.colorTransparent)
        textInputEditTextRemarks.isEnabled = true
        val textInputLayoutCheckAccountNumber =
            viewCheckAccountNumber.findViewById<TextInputLayout>(R.id.textInputLayout)
        textInputLayoutCheckAccountNumber.hint = formatString(R.string.hint_check_account_number)
        textInputEditTextCheckAccountNumber =
            viewCheckAccountNumber.findViewById(R.id.textInputEditText)
        textInputEditTextCheckAccountNumber.id = R.id.textInputEditTextCheckAccountNumber
        viewUtil.setInputType(textInputEditTextCheckAccountNumber, "number")
        viewUtil.setEditTextMaxLength(
            textInputEditTextCheckAccountNumber,
            resources.getInteger(R.integer.max_length_account_number_ubp_without_mask)
        )
        textInputEditTextCheckAccountNumber.setOnTouchListener(View.OnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= textInputEditTextCheckAccountNumber.right -
                    textInputEditTextCheckAccountNumber.compoundDrawables[DRAWABLE_RIGHT].bounds.width()
                ) {
                    val checkAccountNumberDialog =
                        MaterialDialog(this@BranchVisitTransactionFormActivity).apply {
                            lifecycleOwner(this@BranchVisitTransactionFormActivity)
                            customView(R.layout.dialog_check_account_number_tip)
                        }
                    val buttonClose =
                        checkAccountNumberDialog.view.findViewById<MaterialButton>(R.id.buttonClose)
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
        val textInputLayoutCheckNumber =
            viewCheckNumber.findViewById<TextInputLayout>(R.id.textInputLayout)
        textInputLayoutCheckNumber.hint = formatString(R.string.hint_check_number)
        textInputEditTextCheckNumber =
            viewCheckNumber.findViewById(R.id.textInputEditText)
        textInputEditTextCheckNumber.id = R.id.textInputEditTextCheckNumber
        viewUtil.setInputType(textInputEditTextCheckNumber, "number")
        viewUtil.setEditTextMaxLength(
            textInputEditTextCheckNumber,
            resources.getInteger(R.integer.max_length_check_number)
        )
        textInputEditTextCheckNumber.setOnTouchListener(View.OnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= textInputEditTextCheckNumber.right -
                    textInputEditTextCheckNumber.compoundDrawables[DRAWABLE_RIGHT].bounds.width()
                ) {
                    val checkNumberDialog =
                        MaterialDialog(this@BranchVisitTransactionFormActivity).apply {
                            lifecycleOwner(this@BranchVisitTransactionFormActivity)
                            customView(R.layout.dialog_check_number_tip)
                        }
                    val buttonClose =
                        checkNumberDialog.view.findViewById<MaterialButton>(R.id.buttonClose)
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
        textInputLayoutStartDate.hint = formatString(R.string.hint_date_front_of_check)
    }

    private fun validateForm(isValueChanged: Boolean) {
        formDisposable?.let { disposables.remove(it) }
        val textInputEditTextCheckAccountNumberObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = isValueChanged,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = textInputEditTextCheckAccountNumber
        )
        val textInputEditTextCheckNumberObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = isValueChanged,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_check_number),
            editText = textInputEditTextCheckNumber
        )
        val textInputEditTextCheckDateObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = isValueChanged,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = textInputEditTextStartDate
        )
        val amountObservable = viewUtil.rxTextChangesAmount(
            true,
            isValueChanged,
            et_amount,
            RxValidator.PatternMatch(
                formatString(R.string.error_input_valid_amount),
                Pattern.compile(ViewUtil.REGEX_FORMAT_VALID_AMOUNT)
            ),
            RxValidator.PatternMatch(
                formatString(R.string.error_specific_field, formatString(R.string.title_amount)),
                Pattern.compile(ViewUtil.REGEX_FORMAT_AMOUNT)
            ),
            RxValidator.PatternMatch(
                formatString(R.string.error_input_valid_amount),
                Pattern.compile(ViewUtil.REGEX_FORMAT_AMOUNT_OPTIONAL)
            )
        )
        initSetError(textInputEditTextCheckAccountNumberObservable)
        initSetError(textInputEditTextCheckNumberObservable)
        initSetError(textInputEditTextCheckDateObservable)
        initSetError(amountObservable)

        formDisposable = RxCombineValidator(
            textInputEditTextCheckAccountNumberObservable,
            textInputEditTextCheckNumberObservable,
            textInputEditTextCheckDateObservable,
            amountObservable
        )
            .asObservable()
            .distinctUntilChanged()
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .doOnNext {
                isValidForm = it
                if (!isValueChanged && isValidForm) {
                    clickAdd()
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
        formDisposable?.let { disposables.add(it) }
        initEditTextDefaultValue()
    }

    private fun initEditTextDefaultValue() {
        setDefaultValue(textInputEditTextCheckAccountNumber)
        setDefaultValue(textInputEditTextCheckNumber)
        setDefaultValue(textInputEditTextStartDate)
        setDefaultValue(et_amount)
    }

    private fun setDefaultValue(textInputEditText: EditText) {
        if (textInputEditText.length() != 0) {
            textInputEditText.setText(textInputEditText.text.toString())
        }
    }

    private fun showMissingFieldDialog() {
        showMaterialDialogError(
            message = formatString(R.string.msg_error_missing_fields)
        )
    }

    private fun showDateOnClickDialog() {
        val calendar = if (textInputEditTextStartDate.length() > 0) {
            textInputEditTextStartDate.tag.toString().convertToCalendar()
        } else {
            Calendar.getInstance()
        }
        showDatePicker(
            minDate = Calendar.getInstance().apply { add(Calendar.DATE, -180) },
            maxDate = Calendar.getInstance(),
            calendar = calendar,
            callback = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, monthOfYear, dayOfMonth)
                textInputEditTextStartDate.tag = viewUtil.getDateFormatByCalendar(
                    selectedDate,
                    DateFormatEnum.DATE_FORMAT_ISO_DATE.value
                )
                textInputEditTextStartDate.setText(
                    viewUtil.getDateFormatByCalendar(
                        selectedDate,
                        DateFormatEnum.DATE_FORMAT_DATE.value
                    )
                )
            }
        )
    }

    private fun initImeOptionEditText() {
        imeOptionEditText =
            ImeOptionEditText()
        imeOptionEditText.addEditText(
            textInputEditTextCheckAccountNumber,
            textInputEditTextCheckNumber,
            et_amount,
            textInputEditTextRemarks
        )
        imeOptionEditText.setOnImeOptionListener(this)
        imeOptionEditText.startListener()
    }

    private fun initCash() {
        textViewCheckType.visibility(false)
        radioGroupCheckType.visibility(false)
        textViewCheckAccountNumber.visibility(false)
        viewCheckAccountNumber.visibility(false)
        textViewCheckNumber.visibility(false)
        viewCheckNumber.visibility(false)
        textViewCheckDate.visibility(false)
        viewCheckDateCheck.visibility(false)
        textInputEditTextCheckAccountNumber.setText(EMPTY_VALUE)
        textInputEditTextCheckNumber.setText(EMPTY_VALUE)
        textInputEditTextStartDate.setText(EMPTY_VALUE)
    }

    private fun initCheck(onUs: Boolean) {
        textViewCheckType.visibility(true)
        radioGroupCheckType.visibility(true)
        textViewCheckAccountNumber.visibility(onUs)
        viewCheckAccountNumber.visibility(onUs)
        textViewCheckNumber.visibility(true)
        viewCheckNumber.visibility(true)
        textViewCheckDate.visibility(onUs)
        viewCheckDateCheck.visibility(onUs)
        if (onUs) {
            if (branchTransactionForm.type == BranchVisitTypeEnum.CHECK_DEPOSIT_ON_US.value && isEdit) {
                textInputEditTextCheckAccountNumber.setText(branchTransactionForm.accountNumber)
                textInputEditTextCheckNumber.setText(branchTransactionForm.checkNumber)
                textInputEditTextStartDate.setText(
                    viewUtil.getDateFormatByDateString(
                        branchTransactionForm.checkDate,
                        DateFormatEnum.DATE_FORMAT_ISO_DATE.value,
                        DateFormatEnum.DATE_FORMAT_DATE.value
                    )
                )
                textInputEditTextStartDate.tag = branchTransactionForm.checkDate
            } else {
                textInputEditTextCheckAccountNumber.text?.clear()
                textInputEditTextCheckNumber.text?.clear()
                textInputEditTextStartDate.text?.clear()
            }
        } else {
            if (branchTransactionForm.type == BranchVisitTypeEnum.CHECK_DEPOSIT_OFF_US.value && isEdit) {
                textInputEditTextCheckNumber.setText(branchTransactionForm.checkNumber)
            } else {
                textInputEditTextCheckNumber.text?.clear()
            }
            textInputEditTextCheckAccountNumber.setText(EMPTY_VALUE)
            textInputEditTextStartDate.setText(EMPTY_VALUE)
        }
    }

    private fun clearFormFocus() {
        constraintLayoutParent.requestFocus()
        constraintLayoutParent.isFocusableInTouchMode = true
    }

    private fun clickAdd() {
        if (isValidForm) {
            clearFormFocus()
            addBranchTransaction()
        } else {
            if (isInitialSubmitForm) {
                isInitialSubmitForm = false
                validateForm(false)
            } else {
                showMissingFieldDialog()
            }
        }
    }

    private fun addBranchTransaction() {
        val branchTransactionForm = BranchTransactionForm().apply {
            type = getType()
            amount = DecimalFormat("####0.00").format(et_amount.getNumericValue())
            currency = "PHP"
            textInputEditTextStartDate.tag?.let {
                checkDate = emptyToNullValue(it.toString())
            }
            checkNumber = emptyToNullValue(textInputEditTextCheckNumber.text.toString())
            accountNumber = emptyToNullValue(textInputEditTextCheckAccountNumber.text.toString())
            remarks =
                if (textInputEditTextRemarks.text.toString() == "")
                    null
                else
                    textInputEditTextRemarks.text.toString()
        }
        if (isEdit) {
            viewModel.editBranchTransaction(
                branchTransactionForm,
                intent.getIntExtra(EXTRA_POSITION, 0)
            )
        } else {
            viewModel.addBranchTransaction(branchTransactionForm)
        }
    }

    private fun initEditForm() {
        if (isEdit) {
            branchTransactionForm =
                JsonHelper.fromJson(intent.getStringExtra(EXTRA_BRANCH_TRANSACTION))
            when (branchTransactionForm.type) {
                BranchVisitTypeEnum.CHECK_DEPOSIT_ON_US.value -> {
                    radioButtonUnionBank.isChecked = true
                    radioButtonCheck.isChecked = true
                    initCheck(true)
                }
                BranchVisitTypeEnum.CHECK_DEPOSIT_OFF_US.value -> {
                    radioButtonOtherBank.isChecked = true
                    radioButtonCheck.isChecked = true
                    initCheck(false)
                }
                else -> {
                    radioButtonCash.isEnabled = true
                    initCash()
                }
            }
            et_amount.setText(branchTransactionForm.amount)
            textInputEditTextRemarks.setText(branchTransactionForm.remarks)
        } else {
            initCash()
        }
    }

    private fun emptyToNullValue(value: String): String? {
        return if (value == Constant.EMPTY) {
            null
        } else {
            value
        }
    }

    private fun getType(): String? {
        return if (radioButtonCash.isChecked)
            BranchVisitTypeEnum.CASH_DEPOSIT.value
        else {
            if (radioButtonUnionBank.isChecked)
                BranchVisitTypeEnum.CHECK_DEPOSIT_ON_US.value
            else
                BranchVisitTypeEnum.CHECK_DEPOSIT_OFF_US.value
        }
    }

    companion object {
        const val EXTRA_IS_EDIT = "is_edit"
        const val EXTRA_POSITION = "position"
        const val EXTRA_BRANCH_TRANSACTION = "branch_transaction"
        const val DRAWABLE_RIGHT = 2
        const val EMPTY_VALUE = "-"
    }
}
