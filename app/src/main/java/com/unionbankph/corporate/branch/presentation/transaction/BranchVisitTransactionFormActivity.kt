package com.unionbankph.corporate.branch.presentation.transaction

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
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
import com.unionbankph.corporate.databinding.ActivityBranchVisitTransactionFormBinding
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import io.reactivex.disposables.Disposable
import java.text.DecimalFormat
import java.util.*
import java.util.regex.Pattern

class BranchVisitTransactionFormActivity :
    BaseActivity<ActivityBranchVisitTransactionFormBinding, BranchVisitTransactionFormViewModel>(),
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
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
        setToolbarTitle(binding.viewToolbar.tvToolbar, formatString(R.string.title_add_a_transaction))
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
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
        binding.radioGroupTypeOfDeposit.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioButtonCash -> {
                    initCash()
                }
                R.id.radioButtonCheck -> {
                    initCheck(binding.radioButtonUnionBank.isChecked)
                }
            }
        }
        binding.radioGroupCheckType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioButtonUnionBank -> {
                    initCheck(true)
                }
                R.id.radioButtonOtherBank -> {
                    initCheck(false)
                }
            }
        }
        binding.viewCheckDateCheck.textInputEditTextStartDate.setOnClickListener {
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
        binding.textInputLayoutRemarks.setContextCompatBackground(R.color.colorTransparent)
        binding.textInputEditTextRemarks.isEnabled = true
        val textInputLayoutCheckAccountNumber =
            binding.viewCheckAccountNumber.textInputLayout
        textInputLayoutCheckAccountNumber.hint = formatString(R.string.hint_check_account_number)
        textInputEditTextCheckAccountNumber =
            binding.viewCheckAccountNumber.textInputEditText
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
            binding.viewCheckNumber.textInputLayout
        textInputLayoutCheckNumber.hint = formatString(R.string.hint_check_number)
        textInputEditTextCheckNumber =
            binding.viewCheckNumber.textInputEditText
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
        binding.viewCheckDateCheck.textInputLayoutStartDate.hint = formatString(R.string.hint_date_front_of_check)
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
            editText = binding.viewCheckDateCheck.textInputEditTextStartDate
        )
        val amountObservable = viewUtil.rxTextChangesAmount(
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
        setDefaultValue(binding.viewCheckDateCheck.textInputEditTextStartDate)
        setDefaultValue(binding.etAmount)
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
        val calendar = if (binding.viewCheckDateCheck.textInputEditTextStartDate.length() > 0) {
            binding.viewCheckDateCheck.textInputEditTextStartDate.tag.toString().convertToCalendar()
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
                binding.viewCheckDateCheck.textInputEditTextStartDate.tag = viewUtil.getDateFormatByCalendar(
                    selectedDate,
                    DateFormatEnum.DATE_FORMAT_ISO_DATE.value
                )
                binding.viewCheckDateCheck.textInputEditTextStartDate.setText(
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
            binding.etAmount,
            binding.textInputEditTextRemarks
        )
        imeOptionEditText.setOnImeOptionListener(this)
        imeOptionEditText.startListener()
    }

    private fun initCash() {
        binding.textViewCheckType.visibility(false)
        binding.radioGroupCheckType.visibility(false)
        binding.textViewCheckAccountNumber.visibility(false)
        binding.viewCheckAccountNumber.root.visibility(false)
        binding.textViewCheckNumber.visibility(false)
        binding.viewCheckNumber.root.visibility(false)
        binding.textViewCheckDate.visibility(false)
        binding.viewCheckDateCheck.root.visibility(false)
        textInputEditTextCheckAccountNumber.setText(EMPTY_VALUE)
        textInputEditTextCheckNumber.setText(EMPTY_VALUE)
        binding.viewCheckDateCheck.textInputEditTextStartDate.setText(EMPTY_VALUE)
    }

    private fun initCheck(onUs: Boolean) {
        binding.textViewCheckType.visibility(true)
        binding.radioGroupCheckType.visibility(true)
        binding.textViewCheckAccountNumber.visibility(onUs)
        binding.viewCheckAccountNumber.root.visibility(onUs)
        binding.textViewCheckNumber.visibility(true)
        binding.viewCheckNumber.root.visibility(true)
        binding.textViewCheckDate.visibility(onUs)
        binding.viewCheckDateCheck.root.visibility(onUs)
        if (onUs) {
            if (branchTransactionForm.type == BranchVisitTypeEnum.CHECK_DEPOSIT_ON_US.value && isEdit) {
                textInputEditTextCheckAccountNumber.setText(branchTransactionForm.accountNumber)
                textInputEditTextCheckNumber.setText(branchTransactionForm.checkNumber)
                binding.viewCheckDateCheck.textInputEditTextStartDate.setText(
                    viewUtil.getDateFormatByDateString(
                        branchTransactionForm.checkDate,
                        DateFormatEnum.DATE_FORMAT_ISO_DATE.value,
                        DateFormatEnum.DATE_FORMAT_DATE.value
                    )
                )
                binding.viewCheckDateCheck.textInputEditTextStartDate.tag = branchTransactionForm.checkDate
            } else {
                textInputEditTextCheckAccountNumber.text?.clear()
                textInputEditTextCheckNumber.text?.clear()
                binding.viewCheckDateCheck.textInputEditTextStartDate.text?.clear()
            }
        } else {
            if (branchTransactionForm.type == BranchVisitTypeEnum.CHECK_DEPOSIT_OFF_US.value && isEdit) {
                textInputEditTextCheckNumber.setText(branchTransactionForm.checkNumber)
            } else {
                textInputEditTextCheckNumber.text?.clear()
            }
            textInputEditTextCheckAccountNumber.setText(EMPTY_VALUE)
            binding.viewCheckDateCheck.textInputEditTextStartDate.setText(EMPTY_VALUE)
        }
    }

    private fun clearFormFocus() {
        binding.constraintLayoutParent.requestFocus()
        binding.constraintLayoutParent.isFocusableInTouchMode = true
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
            amount = DecimalFormat("####0.00").format(binding.etAmount.getNumericValue())
            currency = "PHP"
            binding.viewCheckDateCheck.textInputEditTextStartDate.tag?.let {
                checkDate = emptyToNullValue(it.toString())
            }
            checkNumber = emptyToNullValue(textInputEditTextCheckNumber.text.toString())
            accountNumber = emptyToNullValue(textInputEditTextCheckAccountNumber.text.toString())
            remarks =
                if (binding.textInputEditTextRemarks.text.toString() == "")
                    null
                else
                    binding.textInputEditTextRemarks.text.toString()
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
                    binding.radioButtonUnionBank.isChecked = true
                    binding.radioButtonCheck.isChecked = true
                    initCheck(true)
                }
                BranchVisitTypeEnum.CHECK_DEPOSIT_OFF_US.value -> {
                    binding.radioButtonOtherBank.isChecked = true
                    binding.radioButtonCheck.isChecked = true
                    initCheck(false)
                }
                else -> {
                    binding.radioButtonCash.isEnabled = true
                    initCash()
                }
            }
            binding.etAmount.setText(branchTransactionForm.amount)
            binding.textInputEditTextRemarks.setText(branchTransactionForm.remarks)
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
        return if (binding.radioButtonCash.isChecked)
            BranchVisitTypeEnum.CASH_DEPOSIT.value
        else {
            if (binding.radioButtonUnionBank.isChecked)
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

    override val viewModelClassType: Class<BranchVisitTransactionFormViewModel>
        get() = BranchVisitTransactionFormViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityBranchVisitTransactionFormBinding
        get() = ActivityBranchVisitTransactionFormBinding::inflate
}
