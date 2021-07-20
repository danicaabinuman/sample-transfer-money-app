package com.unionbankph.corporate.mcd.presentation.filter

import android.os.Bundle
import android.os.SystemClock
import android.view.MenuItem
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.textfield.TextInputEditText
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.presentation.account_selection.AccountSelectionActivity
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.platform.bus.event.AccountSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.InputSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.edittext.ImeOptionEditText
import com.unionbankph.corporate.app.common.widget.validator.validation.RxCombineValidator
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidator
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.common.presentation.constant.DateFormatEnum
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.databinding.ActivityCheckDepositFilterBinding
import com.unionbankph.corporate.settings.presentation.form.Selector
import com.unionbankph.corporate.settings.presentation.form.SelectorData
import com.unionbankph.corporate.settings.presentation.selector.SelectorActivity
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import io.reactivex.rxkotlin.addTo
import java.util.*
import java.util.regex.Pattern
import javax.annotation.concurrent.ThreadSafe

/**
 * Created by herald25santos on 6/23/20
 */
class CheckDepositFilterActivity :
    BaseActivity<ActivityCheckDepositFilterBinding, CheckDepositFilterViewModel>(),
    ImeOptionEditText.OnImeOptionListener {

    private lateinit var tieCheckStartDate: TextInputEditText
    private lateinit var tieCheckEndDate: TextInputEditText

    private lateinit var tieStartDateCreated: TextInputEditText
    private lateinit var tieEndDateCreated: TextInputEditText

    override fun afterLayout(savedInstanceState: Bundle?) {
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
        setToolbarTitle(binding.viewToolbar.tvToolbar, formatString(R.string.title_filters))
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel.applyFilter.observe(this, EventObserver {
            onBackPressed()
            eventBus.actionSyncEvent.emmit(
                BaseEvent(ActionSyncEvent.ACTION_APPLY_FILTER_CHECK_DEPOSIT, it)
            )
        })
    }

    override fun onViewsBound() {
        setupViews()
        setupInputs()
        setupOutputs()
    }

    override fun onInitializeListener() {
        initEventBus()
        initClickListener()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDatePickerDialog(
        currentMinDate: Calendar = Calendar.getInstance().apply { set(Calendar.YEAR, 1900) },
        currentSelected: Calendar,
        datePickerAction: ((value: String) -> Unit)
    ) {
        showDatePicker(
            minDate = currentMinDate,
            maxDate = Calendar.getInstance(),
            calendar = currentSelected,
            callback = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                datePickerAction(
                    viewUtil.getDateFormatByCalendar(
                        Calendar.getInstance().apply {
                            set(year, monthOfYear, dayOfMonth)
                        },
                        DateFormatEnum.DATE_FORMAT_ISO_DATE.value
                    )
                )
            }
        )
    }

    private fun setupViews() {
        initImeOption()
        intent.getStringExtra(EXTRA_CURRENT_FILTER)?.let {
            viewModel.loadInputs(it)
        }
        tieCheckStartDate = binding.viewStartCheckDate.textInputEditTextStartDate
        tieCheckEndDate = binding.viewEndCheckDate.textInputEditTextStartDate

        tieStartDateCreated = binding.viewStartDateCreated.textInputEditTextStartDate
        tieEndDateCreated = binding.viewEndDateCreated.textInputEditTextStartDate

        tieCheckStartDate.hint = formatString(R.string.title_start_date)
        tieCheckEndDate.hint = formatString(R.string.title_end_date)

        tieStartDateCreated.hint = formatString(R.string.title_start_date)
        tieEndDateCreated.hint = formatString(R.string.title_end_date)

        validateForm()
    }

    private fun setupInputs() {
        viewModel.input.also { viewModel ->
            viewModel.checkNumber.subscribe {
                binding.tieCheckNumber.setText(it)
            }.addTo(disposables)
            viewModel.amount.subscribe {
                if (it != "") {
                    val currency = viewModel.depositAccount.value?.currency ?: "PHP"
                    binding.etAmount.setText(("$currency $it"))
                } else {
                    binding.etAmount.clearText()
                }
            }.addTo(disposables)
            viewModel.checkStartDate.subscribe {
                tieCheckStartDate.setText(
                    it.convertDateToDesireFormat(DateFormatEnum.DATE_FORMAT_DATE)
                )
                if (it != null && it != "") {
                    tieCheckEndDate.setEnableView(true)
                }
            }.addTo(disposables)
            viewModel.checkEndDate.subscribe {
                tieCheckEndDate.setText(
                    it.convertDateToDesireFormat(DateFormatEnum.DATE_FORMAT_DATE)
                )
            }.addTo(disposables)
            viewModel.depositAccount.subscribe {
                if (viewModel.hasDepositAccount.value.notNullable() && it.id != null) {
                    binding.tieDepositAccount.setText(("${it.name}\n${it.accountNumber.formatAccountNumber()}"))
                } else {
                    binding.tieDepositAccount.text?.clear()
                }
                binding.etAmount.setCurrencySymbol(it.currency ?: "PHP", true)
            }.addTo(disposables)
            viewModel.status.subscribe {
                binding.tieStatus.setText(it)
            }.addTo(disposables)
            viewModel.startDateCreated.subscribe {
                tieStartDateCreated.setText(
                    it.convertDateToDesireFormat(DateFormatEnum.DATE_FORMAT_DATE)
                )
                if (it != null && it != "") {
                    tieEndDateCreated.setEnableView(true)
                }
            }.addTo(disposables)
            viewModel.endDateCreated.subscribe {
                tieEndDateCreated.setText(
                    it.convertDateToDesireFormat(DateFormatEnum.DATE_FORMAT_DATE)
                )
            }.addTo(disposables)
        }
    }

    private fun setupOutputs() {
        viewModel.clickedDepositAccount.observe(this, EventObserver {
            navigateDepositAccount(it)
        })
        viewModel.statuses.observe(this, EventObserver {
            navigateSelectorScreen(it)
        })
        viewModel.input.statusToDisplay
            .observeOn(schedulerProvider.ui())
            .subscribe {
                binding.tieStatus.setText(it)
            }.addTo(disposables)
    }

    private fun initEventBus() {
        eventBus.accountSyncEvent.flowable.subscribe {
            if (it.eventType == AccountSyncEvent.ACTION_UPDATE_SELECTED_ACCOUNT) {
                it.payload?.let { account ->
                    viewModel.onSelectedDepositAccount(account)
                }
            }
        }.addTo(disposables)
        eventBus.inputSyncEvent.flowable.subscribe {
            when (it.eventType) {
                InputSyncEvent.ACTION_INPUT_FILTER_STATUS -> {
                    val selectorData = JsonHelper.fromJson<SelectorData>(it.payload)
                    viewModel.input.status.onNext(selectorData.items.notNullable())
                    viewModel.input.statusesState.onNext(selectorData.stateData.notNullable())
                    viewModel.input.statusToDisplay.onNext(selectorData.valueToDisplay.notNullable())
                }
            }
        }.addTo(disposables)
    }

    private fun initClickListener() {
        tieCheckStartDate.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                return@setOnClickListener
            }
            mLastClickTime = SystemClock.elapsedRealtime()
            showDatePickerDialog(
                currentSelected = viewModel.input.checkStartDate.value.convertToCalendar()
            ) {
                if (viewModel.input.checkEndDate.hasValue()) {
                    val startDate = it.convertToCalendar().timeInMillis
                    val endDate =
                        viewModel.input.checkEndDate.value.convertToCalendar().timeInMillis
                    if (startDate > endDate) {
                        viewModel.input.checkEndDate.onNext(it)
                    }
                }
                viewModel.input.checkStartDate.onNext(it)
            }
        }
        tieCheckEndDate.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                return@setOnClickListener
            }
            mLastClickTime = SystemClock.elapsedRealtime()
            if (viewModel.input.checkStartDate.hasValue()) {
                showDatePickerDialog(
                    currentMinDate = viewModel.input.checkStartDate.value.convertToCalendar(),
                    currentSelected = viewModel.input.checkEndDate.value.convertToCalendar()
                ) {
                    viewModel.input.checkEndDate.onNext(it)
                }
            } else {
                showDatePickerDialog(
                    currentSelected = viewModel.input.checkEndDate.value.convertToCalendar()
                ) {
                    viewModel.input.checkEndDate.onNext(it)
                }
            }
        }
        binding.tieDepositAccount.setOnClickListener {
            viewModel.onClickedDepositAccount()
        }
        tieStartDateCreated.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                return@setOnClickListener
            }
            mLastClickTime = SystemClock.elapsedRealtime()
            showDatePickerDialog(
                currentSelected = viewModel.input.startDateCreated.value.convertToCalendar()
            ) {
                if (viewModel.input.endDateCreated.hasValue()) {
                    val startDate = it.convertToCalendar().timeInMillis
                    val endDate =
                        viewModel.input.endDateCreated.value.convertToCalendar().timeInMillis
                    if (startDate > endDate) {
                        viewModel.input.endDateCreated.onNext(it)
                    }
                }
                viewModel.input.startDateCreated.onNext(it)
            }
        }
        tieEndDateCreated.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                return@setOnClickListener
            }
            mLastClickTime = SystemClock.elapsedRealtime()
            if (viewModel.input.startDateCreated.hasValue()) {
                showDatePickerDialog(
                    currentMinDate = viewModel.input.startDateCreated.value.convertToCalendar(),
                    currentSelected = viewModel.input.endDateCreated.value.convertToCalendar()
                ) {
                    viewModel.input.endDateCreated.onNext(it)
                }
            } else {
                showDatePickerDialog(
                    currentSelected = viewModel.input.endDateCreated.value.convertToCalendar()
                ) {
                    viewModel.input.endDateCreated.onNext(it)
                }
            }
        }
        binding.tieStatus.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                return@setOnClickListener
            }
            mLastClickTime = SystemClock.elapsedRealtime()
            viewModel.onClickedStatuses()
        }
        binding.btnApplyFilter.setOnClickListener {
            if (!viewModel.input.isValidForm.value.notNullable() &&
                binding.etAmount.getNumericValue() != 0.0
            ) {
                binding.etAmount.refresh()
            } else {
                viewModel.setFreeTextFields(
                    binding.tieCheckNumber.getTextNullable(),
                    binding.etAmount.getExactValue()
                )
                viewModel.onClickedApplyFilter()
            }
        }
        binding.btnClearFilter.setOnClickListener {
            viewModel.onClickedClearFilter()
            clearFocus()
        }
    }

    private fun initImeOption() {
        val imeOptionEditText = ImeOptionEditText()
        imeOptionEditText.addEditText(
            binding.tieCheckNumber,
            binding.etAmount
        )
        imeOptionEditText.setOnImeOptionListener(this)
        imeOptionEditText.startListener()
    }

    private fun validateForm() {
        val amountObservable = viewUtil.rxTextChangesAmount(
            true,
            true,
            binding.etAmount,
            RxValidator.PatternMatch(
                formatString(R.string.error_input_valid_amount),
                Pattern.compile(ViewUtil.REGEX_FORMAT_AMOUNT_OPTIONAL)
            )
        )

        initSetError(amountObservable)

        RxCombineValidator(amountObservable)
            .asObservable()
            .distinctUntilChanged()
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .doOnNext {
                if (binding.etAmount.length() > 0) {
                    viewModel.input.isValidForm.onNext(it)
                } else {
                    viewModel.input.isValidForm.onNext(true)
                }
            }
            .subscribe()
            .addTo(disposables)
    }

    private fun navigateDepositAccount(id: String?) {
        val bundle = Bundle()
        bundle.putString(
            AccountSelectionActivity.EXTRA_PAGE,
            AccountSelectionActivity.PAGE_CHECK_DEPOSIT_FILTER
        )
        bundle.putString(
            AccountSelectionActivity.EXTRA_ID,
            id
        )
        viewModel.permissionId.value?.let {
            bundle.putString(AccountSelectionActivity.EXTRA_PERMISSION_ID, it)
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

    private fun navigateSelectorScreen(statuses: MutableList<Selector>) {
        val items = SelectorData(
            items = viewModel.input.status.value,
            selectors = statuses,
            stateData = viewModel.input.statusesState.value,
            valueToDisplay = viewModel.input.statusToDisplay.value
        )
        navigator.navigate(
            this,
            SelectorActivity::class.java,
            Bundle().apply {
                putString(SelectorActivity.EXTRA_SELECTOR, STATUS_SELECTOR)
                putParcelable(SelectorActivity.EXTRA_ITEMS, items)
            },
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun clearFocus() {
        binding.constraintLayout.post {
            viewUtil.dismissKeyboard(this)
            binding.constraintLayout.requestFocus()
            binding.constraintLayout.isFocusableInTouchMode = true
        }
    }

    @ThreadSafe
    companion object {
        const val EXTRA_CURRENT_FILTER = "current_filter"
        const val STATUS_SELECTOR = "status"
    }

    override val layoutId: Int
        get() = R.layout.activity_check_deposit_filter

    override val viewModelClassType: Class<CheckDepositFilterViewModel>
        get() = CheckDepositFilterViewModel::class.java

}