package com.unionbankph.corporate.general.presentation.transaction_filter

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.extension.showDatePicker
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.platform.bus.event.InputSyncEvent
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.bills_payment.data.model.Biller
import com.unionbankph.corporate.bills_payment.presentation.biller.BillerMainActivity
import com.unionbankph.corporate.bills_payment.presentation.biller.biller_all.AllBillerFragment
import com.unionbankph.corporate.common.presentation.constant.DateFormatEnum
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.general.data.model.TransactionFilterForm
import com.unionbankph.corporate.settings.presentation.form.SelectorData
import com.unionbankph.corporate.settings.presentation.selector.SelectorActivity
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_transaction_filter.*
import kotlinx.android.synthetic.main.widget_transparent_appbar.*
import java.util.*
import javax.annotation.concurrent.ThreadSafe

/**
 * Created by herald25santos on 2020-02-18
 */
class TransactionFilterActivity :
    BaseActivity<TransactionFilterViewModel>(R.layout.activity_transaction_filter) {

    private lateinit var textInputEditTextStartDate: TextInputEditText
    private lateinit var textInputEditTextEndDate: TextInputEditText

    private lateinit var textInputEditTextChannel: TextInputEditText
    private lateinit var textInputEditTextStatus: TextInputEditText
    private lateinit var textInputEditTextBillerName: TextInputEditText

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
        setToolbarTitle(tvToolbar, formatString(R.string.title_filters))
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel =
            ViewModelProviders.of(this, viewModelFactory)[TransactionFilterViewModel::class.java]
        viewModel.uiState.observe(this, EventObserver {
            when (it) {
                is UiState.Complete -> {
                    onBackPressed()
                }
                is UiState.Error -> {
                    handleOnError(it.throwable)
                }
            }
        })
        viewModel.uiStateChannels.observe(this, androidx.lifecycle.Observer {
            when (it) {
                is UiState.Loading -> {
                    showProgressAlertDialog(TransactionFilterActivity::class.java.simpleName)
                }
                is UiState.Complete -> {
                    dismissProgressAlertDialog()
                }
                is UiState.Error -> {
                    handleOnError(it.throwable)
                }
            }
        })
        viewModel.uiStateStatuses.observe(this, androidx.lifecycle.Observer {
            when (it) {
                is UiState.Loading -> {
                    showProgressAlertDialog(TransactionFilterActivity::class.java.simpleName)
                }
                is UiState.Complete -> {
                    dismissProgressAlertDialog()
                }
                is UiState.Error -> {
                    handleOnError(it.throwable)
                }
            }
        })
        viewModel.channels.observe(this, androidx.lifecycle.Observer {
            navigateSelectorScreen(SelectorActivity.CHANNEL_SELECTOR)
        })
        viewModel.statuses.observe(this, androidx.lifecycle.Observer {
            navigateSelectorScreen(SelectorActivity.STATUS_SELECTOR)
        })
    }

    override fun onViewsBound() {
        super.onViewsBound()
        init()
        initBinding()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initEventBus()
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

    private fun initBinding() {
        viewModel.screen.onNext(intent.getStringExtra(EXTRA_SCREEN).notNullable())
        intent.getStringExtra(EXTRA_TRANSACTION_FILTER_FORM)?.let {
            val transactionFilterForm = JsonHelper.fromJson<TransactionFilterForm>(it)
            transactionFilterForm.channels?.let {
                viewModel.channelsInput.onNext(it)
            }
            transactionFilterForm.channelsValueToDisplay?.let {
                viewModel.channelsToDisplayInput.onNext(it)
            }
            transactionFilterForm.channelsStateData?.let {
                viewModel.channelsStateDataInput.onNext(it)
            }
            transactionFilterForm.statuses?.let {
                viewModel.statusInput.onNext(it)
            }
            transactionFilterForm.statusesValueToDisplay?.let {
                viewModel.statusToDisplayInput.onNext(it)
            }
            transactionFilterForm.statusesStateData?.let {
                viewModel.statusesStateDataInput.onNext(it)
            }
            transactionFilterForm.biller?.let {
                viewModel.billerInput.onNext(it)
            }
            transactionFilterForm.startDate?.let {
                viewModel.startDateInput.onNext(
                    viewUtil.getCalendarByDateString(
                        it,
                        DateFormatEnum.DATE_FORMAT_ISO_DATE.value
                    )
                )
            }
            transactionFilterForm.endDate?.let {
                viewModel.endDateInput.onNext(
                    viewUtil.getCalendarByDateString(
                        it,
                        DateFormatEnum.DATE_FORMAT_ISO_DATE.value
                    )
                )
            }
        }
        viewModel.screen
            .subscribe {
                if (it == BILLS_PAYMENT_SCREEN) {
                    textViewChannel.visibility(false)
                    viewChannel.visibility(false)
                    textViewBillerName.visibility(true)
                    viewBillerName.visibility(true)
                } else {
                    textViewChannel.visibility(true)
                    viewChannel.visibility(true)
                    textViewBillerName.visibility(false)
                    viewBillerName.visibility(false)
                }
            }.addTo(disposables)
        viewModel.startDateInput
            .subscribe {
                textInputEditTextStartDate.setText(
                    viewUtil.getDateFormatByCalendar(
                        it,
                        DateFormatEnum.DATE_FORMAT_DATE.value
                    )
                )
            }.addTo(disposables)
        viewModel.endDateInput
            .subscribe {
                textInputEditTextEndDate.setText(
                    viewUtil.getDateFormatByCalendar(
                        it,
                        DateFormatEnum.DATE_FORMAT_DATE.value
                    )
                )
            }.addTo(disposables)
        viewModel.channelsToDisplayInput
            .subscribe {
                textInputEditTextChannel.setText(it)
            }.addTo(disposables)
        viewModel.statusToDisplayInput
            .subscribe {
                textInputEditTextStatus.setText(it)
            }.addTo(disposables)
        viewModel.billerInput
            .subscribe {
                textInputEditTextBillerName.setText(it.name)
            }.addTo(disposables)
    }

    private fun init() {
        val textInputLayoutStartDate =
            viewStartDate.findViewById<TextInputLayout>(R.id.textInputLayoutStartDate)
        val textInputLayoutEndDate =
            viewEndDate.findViewById<TextInputLayout>(R.id.textInputLayoutStartDate)
        val textInputLayoutChannel =
            viewChannel.findViewById<TextInputLayout>(R.id.textInputLayoutSelector)
        val textInputLayoutStatus =
            viewStatus.findViewById<TextInputLayout>(R.id.textInputLayoutSelector)
        val textInputLayoutBillerName =
            viewBillerName.findViewById<TextInputLayout>(R.id.textInputLayoutSelector)
        val viewBorderStartDate =
            viewStartDate.findViewById<View>(R.id.viewBorderProposedStartDate)
        val viewBorderEndDate =
            viewEndDate.findViewById<View>(R.id.viewBorderProposedStartDate)
        val buttonApplyFilter = viewPrimaryButton.findViewById<Button>(R.id.buttonPrimary)
        val buttonClearFilter = viewSecondaryButton.findViewById<Button>(R.id.buttonSecondary)
        textInputEditTextStartDate =
            viewStartDate.findViewById(R.id.textInputEditTextStartDate)
        textInputEditTextEndDate =
            viewEndDate.findViewById(R.id.textInputEditTextStartDate)
        textInputEditTextChannel =
            viewChannel.findViewById(R.id.textInputEditTextSelector)
        textInputEditTextStatus =
            viewStatus.findViewById(R.id.textInputEditTextSelector)
        textInputEditTextBillerName =
            viewBillerName.findViewById(R.id.textInputEditTextSelector)
        viewBorderStartDate.visibility(false)
        viewBorderEndDate.visibility(false)
        textInputLayoutStartDate.hint = formatString(R.string.title_start_date)
        textInputLayoutEndDate.hint = formatString(R.string.title_end_date)
        textInputLayoutChannel.hint = formatString(R.string.hint_select_channel)
        textInputLayoutStatus.hint = formatString(R.string.hint_select_status)
        textInputLayoutBillerName.hint = formatString(R.string.hint_select_biller_name)
        buttonApplyFilter.text = formatString(R.string.action_apply_filter)
        buttonClearFilter.text = formatString(R.string.action_clear_filter)

        textInputEditTextStartDate.tag = formatString(R.string.title_start_date)
        textInputEditTextEndDate.tag = formatString(R.string.title_end_date)
        textInputEditTextChannel.setOnClickListener {
            viewModel.onClickedChannels()
        }
        textInputEditTextStatus.setOnClickListener {
            viewModel.onClickedStatuses()
        }
        textInputEditTextBillerName.setOnClickListener {
            navigateBillersScreen()
        }
        textInputEditTextStartDate.setOnClickListener {
            showDatePicker(
                textInputEditTextStartDate,
                currentSelectedDate = viewModel.startDateInput.value
            )
        }
        textInputEditTextEndDate.setOnClickListener {
            showDatePicker(
                textInputEditTextEndDate,
                viewModel.startDateInput.value,
                viewModel.endDateInput.value
            )
        }
        buttonApplyFilter.setOnClickListener {
            viewModel.onClickedApplyFilter()
        }
        buttonClearFilter.setOnClickListener {
            viewModel.onClickedClearFilter()
        }
    }

    private fun initEventBus() {
        eventBus.inputSyncEvent.flowable.subscribe {
            when (it.eventType) {
                InputSyncEvent.ACTION_INPUT_FILTER_CHANNEL -> {
                    val selectorData = JsonHelper.fromJson<SelectorData>(it.payload)
                    viewModel.channelsInput.onNext(selectorData.items.notNullable())
                    viewModel.channelsStateDataInput.onNext(selectorData.stateData.notNullable())
                    viewModel.channelsToDisplayInput.onNext(selectorData.valueToDisplay.notNullable())
                }
                InputSyncEvent.ACTION_INPUT_FILTER_STATUS -> {
                    val selectorData = JsonHelper.fromJson<SelectorData>(it.payload)
                    viewModel.statusInput.onNext(selectorData.items.notNullable())
                    viewModel.statusesStateDataInput.onNext(selectorData.stateData.notNullable())
                    viewModel.statusToDisplayInput.onNext(selectorData.valueToDisplay.notNullable())
                }
                InputSyncEvent.ACTION_INPUT_BILLER -> {
                    val biller = JsonHelper.fromJson<Biller>(it.payload)
                    viewModel.billerInput.onNext(biller)
                }
            }
        }.addTo(disposables)
    }

    private fun showDatePicker(
        textInputEditText: TextInputEditText,
        minDate: Calendar? = null,
        currentSelectedDate: Calendar? = null
    ) {
        val calendar = Calendar.getInstance()
        showDatePicker(
            minDate = minDate,
            maxDate = calendar,
            calendar = currentSelectedDate ?: calendar,
            callback = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, monthOfYear, dayOfMonth)
                }
                if (textInputEditText.tag == formatString(R.string.title_start_date)) {
                    viewModel.fillStartDateInput(selectedDate)
                } else {
                    viewModel.endDateInput.onNext(selectedDate)
                }
            }
        )
    }

    private fun navigateSelectorScreen(selector: String) {
        val items = if (selector == SelectorActivity.CHANNEL_SELECTOR) {
            SelectorData(
                items = viewModel.channelsInput.value,
                selectors = viewModel.channels.value,
                stateData = viewModel.channelsStateDataInput.value,
                valueToDisplay = viewModel.channelsToDisplayInput.value
            )
        } else {
            SelectorData(
                items = viewModel.statusInput.value,
                selectors = viewModel.statuses.value,
                stateData = viewModel.statusesStateDataInput.value,
                valueToDisplay = viewModel.statusToDisplayInput.value
            )
        }
        navigator.navigate(
            this,
            SelectorActivity::class.java,
            Bundle().apply {
                putString(SelectorActivity.EXTRA_SELECTOR, selector)
                putParcelable(SelectorActivity.EXTRA_ITEMS, items)
            },
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun navigateBillersScreen() {
        val bundle = Bundle()
        bundle.putString(
            BillerMainActivity.EXTRA_PAGE,
            BillerMainActivity.PAGE_BILLS_PAYMENT_FILTER
        )
        bundle.putString(
            BillerMainActivity.EXTRA_TYPE,
            AllBillerFragment.TYPE_BILLER
        )
        navigator.navigate(
            this,
            BillerMainActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    @ThreadSafe
    companion object {
        const val EXTRA_SCREEN = "screen"
        const val EXTRA_TRANSACTION_FILTER_FORM = "transaction_filter_form"
        const val FUND_TRANSFER_SCREEN = "fund_transfer_screen"
        const val BILLS_PAYMENT_SCREEN = "bills_payment_screen"
    }
}
