package com.unionbankph.corporate.fund_transfer.presentation.proposed_transfer

import android.os.Bundle
import android.os.SystemClock
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.afollestad.materialdialogs.list.listItems
import com.google.android.material.button.MaterialButton
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.platform.bus.event.ProposedTransferDateSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.common.data.model.ProposedTransferDate
import com.unionbankph.corporate.common.presentation.constant.DateFormatEnum
import com.unionbankph.corporate.common.presentation.constant.TimeZoneEnum
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.corporate.data.model.RecurrenceTypes
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import com.wdullaer.materialdatetimepicker.time.Timepoint
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_proposed_transfer_date.*
import kotlinx.android.synthetic.main.widget_edit_text_start_date.*
import kotlinx.android.synthetic.main.widget_transparent_appbar.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Herald Santos
 */
class ProposedTransferDateActivity :
    BaseActivity<ProposedTransferDateViewModel>(R.layout.activity_proposed_transfer_date),
    View.OnClickListener {

    private lateinit var proposedTransferDate: ProposedTransferDate

    private lateinit var recurrencesTypes: MutableList<RecurrenceTypes>

    private lateinit var recurrencesType: RecurrenceTypes

    private var calendarStartDate = Calendar.getInstance()

    private val hasValidForm by lazyFast { intent.getBooleanExtra(EXTRA_HAS_VALID_FORM, false) }

    private var isConvertedTimeZone: Boolean = false

    private var currentOccurrences: Int = 1

    private var currentOccurrencesText: String = OCCURRENCE_DEFAULT_VALUE

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
        if (intent.getStringExtra(EXTRA_PAGE) == PAGE_FUND_TRANSFER) {
            setToolbarTitle(tvToolbar, getString(R.string.title_transaction_date))
        } else {
            setToolbarTitle(tvToolbar, getString(R.string.title_proposed_payment_date))
        }
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel =
            ViewModelProviders.of(this, viewModelFactory)[ProposedTransferDateViewModel::class.java]
        viewModel.state.observe(this, androidx.lifecycle.Observer {
            when (it) {
                is ShowProposedTransferDateLoading -> {
                    showLoading()
                }
                is ShowProposedTransferDateDismissLoading -> {
                    dismissLoading()
                }
                is ShowGetRecurrenceTypes -> {
                    constraintLayoutContent.visibility(true)
                    recurrencesTypes = it.data
                    if (intent.getStringExtra(EXTRA_PTD) == null) {
                        setScheduledDate()
                    } else {
                        setCurrentData()
                    }
                }
                is ShowProposedTransferDateError -> {
                    constraintLayoutContent.visibility(false)
                    handleOnError(it.throwable)
                }
            }
        })
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.radioButtonImmediately -> {
                    proposedTransferDate.immediately = true
                    setImmediate()
                }
                R.id.radioButtonScheduled -> {
                    proposedTransferDate.immediately = false
                    if (intent.getBooleanExtra(EXTRA_SCHEDULED_PERMISSION, false)) {
                        if (intent.getStringExtra(EXTRA_BILLER_ID) == null &&
                            intent.getStringExtra(EXTRA_PAGE) == PAGE_BILLS_PAYMENT
                        ) {
                            textViewNoteMsg.text = formatString(
                                R.string.desc_scheduled_payment_not_available,
                                formatString(
                                    R.string.param_color,
                                    convertColorResourceToHex(getAccentColor()),
                                    formatString(R.string.title_note)
                                )
                            ).toHtmlSpan()
                            hasPermissionScheduledPayment(false)
                        } else {
                            hasPermissionScheduledPayment(true)
                        }
                    } else {
                        hasPermissionScheduledPayment(false)
                    }
                }
            }
        }
        textInputEditTextEnds.setOnClickListener(this)
        textInputEditTextStartDate.setOnClickListener(this)
        textInputEditTextFrequency.setOnClickListener(this)
    }

    override fun onViewsBound() {
        super.onViewsBound()
        setupViews()
        setupInputs()
    }

    /**
     * inflate submit button
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_toolbar, menu)
        val menuActionButton = menu.findItem(R.id.menu_action_button)
        val menuView = menuActionButton.actionView
        val buttonAction = menuView.findViewById<MaterialButton>(R.id.buttonAction)
        buttonAction.text = getString(R.string.action_save)
        buttonAction.enableButton(viewModel.isEnabledButton.value.notNullable())
        menuActionButton.isVisible = true
        buttonAction.setOnClickListener { onOptionsItemSelected(menuActionButton) }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.menu_action_button -> {
                clickSubmit()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Permission to enable scheduled payment
     */
    private fun hasPermissionScheduledPayment(hasPermission: Boolean) {
        constraintLayoutScheduled.isVisible = hasPermission
        textViewNoteMsg.isVisible = !hasPermission
        viewModel.isEnabledButton.onNext(hasPermission)
    }

    /**
     * Initial view setup
     */
    private fun setupViews() {
        radioButtonScheduled.isEnabled = intent.getBooleanExtra(EXTRA_ENABLED_SCHEDULED, true)
        setImmediate()
    }


    /**
     * Initiate viewmodel bindings
     */
    private fun setupInputs() {
        viewModel.isEnabledButton.subscribe {
            invalidateOptionsMenu()
        }.addTo(disposables)
    }


    /**
     * Set to immediate payment
     */
    private fun setImmediate() {
        viewModel.isEnabledButton.onNext(true)
        constraintLayoutScheduled.isVisible = false
        textViewNoteMsg.isVisible = true
        textViewNoteMsg.text = formatString(
            R.string.msg_note,
            formatString(
                R.string.param_color,
                convertColorResourceToHex(getAccentColor()),
                formatString(R.string.title_note)
            ),
            formatString(
                R.string.param_color,
                convertColorResourceToHex(getAccentColor()),
                formatString(R.string.title_fully_approved).toLowerCase()
            )
        ).toHtmlSpan()
    }

    /**
     * Set to scheduled payment
     */
    private fun setScheduledDate() {
        recurrencesType = recurrencesTypes[0]
        proposedTransferDate = ProposedTransferDate()
        proposedTransferDate.immediately = true
        if (hasValidForm) {
            val currentMinute = calendarStartDate.get(Calendar.MINUTE)
            val appendHour = if (currentMinute == 0) {
                0
            } else {
                1
            }
            calendarStartDate.add(Calendar.HOUR_OF_DAY, appendHour)
            if (appendHour == 1 && currentMinute <= 30) {
                calendarStartDate.set(Calendar.MINUTE, 0)
            } else {
                calendarStartDate.set(Calendar.MINUTE, 30)
            }
            proposedTransferDate.frequency = recurrencesType.name
            proposedTransferDate.recurrenceTypeId = recurrencesType.id
            proposedTransferDate.occurrences = currentOccurrences
            proposedTransferDate.occurrencesText = currentOccurrencesText
            setTextInputEditTextTransferDate()
            proposedTransferDate.startDate = viewUtil.getDateFormatByTimeMilliSeconds(
                calendarStartDate.timeInMillis,
                DateFormatEnum.DATE_FORMAT_ISO_Z.value
            )
            textInputEditTextFrequency.setText(recurrencesType.name)
        }
    }

    override fun onClick(view: View) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) return
        mLastClickTime = SystemClock.elapsedRealtime()

        when (view.id) {
            R.id.textInputEditTextStartDate -> {
                showStartDateDialog()
            }
            R.id.textInputEditTextEnds -> {
                showRecurrencesDialog()
            }
            R.id.textInputEditTextFrequency -> {
                showRecurrenceTypesDialog()
            }
        }
    }

    private fun showStartDateDialog() {
        var calendar = Calendar.getInstance()
        calendar = viewUtil.convertCalendarByTimeZone(calendar, TimeZoneEnum.GMT_8.value)
        showDatePicker(
            minDate = calendar,
            calendar = calendar,
            callback = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                calendarStartDate.set(year, monthOfYear, dayOfMonth)
                val timePoints = ArrayList<Timepoint>()
                val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
                val currentMinute = calendar.get(Calendar.MINUTE)
                var appendHour = if (currentMinute == 0) {
                    0
                } else {
                    1
                }
                if (calendar.get(Calendar.DAY_OF_MONTH) == dayOfMonth) {
                    for (i in currentHour..23) {
                        if (appendHour == 1 && currentMinute <= 30) {
                            timePoints.add(Timepoint(currentHour.plus(appendHour)))
                        }
                        timePoints.add(Timepoint(currentHour.plus(appendHour), 30))
                        appendHour++
                    }
                } else {
                    for (i in 0..23) {
                        timePoints.add(Timepoint(currentHour.plus(i)))
                        timePoints.add(Timepoint(currentHour.plus(i), 30))
                    }
                }
                val timePointsArray: Array<Timepoint> = timePoints.toTypedArray()
                showTimerPicker(
                    calendar,
                    timePointsArray,
                    TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute, second ->
                        calendarStartDate.set(
                            year,
                            monthOfYear,
                            dayOfMonth,
                            hourOfDay,
                            minute,
                            second
                        )
                        if (recurrencesType.name != FREQUENCY_TYPE_ONE_TIME) {
                            setOccurrencesEndDate()
                        }
                        setTextInputEditTextTransferDate()
                        proposedTransferDate.startDate = viewUtil.getDateFormatByCalendar(
                            calendarStartDate,
                            DateFormatEnum.DATE_FORMAT_ISO_Z.value
                        )
                        textInputEditTextEnds.setText(currentOccurrencesText)
                    }
                )
            }
        )
    }

    /**
     * Load previous data from selection of payment
     */
    private fun setCurrentData() {
        proposedTransferDate = JsonHelper.fromJson(intent.getStringExtra(EXTRA_PTD))
        if (proposedTransferDate.immediately!!) {
            radioButtonImmediately.isChecked = true
            setImmediate()
            constraintLayoutScheduled.visibility = View.GONE
        } else {
            hasPermissionScheduledPayment(true)
            radioButtonScheduled.isChecked = true
            textInputEditTextFrequency.setText(proposedTransferDate.frequency)
            recurrencesType = RecurrenceTypes(
                proposedTransferDate.recurrenceTypeId,
                proposedTransferDate.frequency
            )
            calendarStartDate = viewUtil.getCalendarByDateString(
                proposedTransferDate.startDate, DateFormatEnum.DATE_FORMAT_ISO_Z.value
            )
            isConvertedTimeZone = true
            setTextInputEditTextTransferDate()
            if (proposedTransferDate.endDate != null &&
                !proposedTransferDate.frequency.equals(FREQUENCY_TYPE_ONE_TIME, true)
            ) {
                currentOccurrences = proposedTransferDate.occurrences ?: 1
                currentOccurrencesText =
                    proposedTransferDate.occurrencesText ?: OCCURRENCE_DEFAULT_VALUE
                textInputEditTextEnds.setText(currentOccurrencesText)
                textViewStartDate.setText(R.string.title_start_date)
                textViewTip.setText(R.string.title_note)
                textViewEndDate.visibility = View.VISIBLE
                textInputLayoutEnds.visibility = View.VISIBLE
                setOccurrencesEndDate()
            } else {
                textViewStartDate.setText(R.string.title_transaction_date)
                textViewTip.setText(R.string.title_tip)
                textViewTipMsg.setText(R.string.msg_tip)
                textViewEndDate.visibility = View.GONE
                textInputLayoutEnds.visibility = View.GONE
            }
        }
    }

    private fun showRecurrenceTypesDialog() {
        val recurrenceNames = if (intent.getStringExtra(EXTRA_PAGE) == PAGE_BILLS_PAYMENT) {
            recurrencesTypes
                .filter { it.name != null && it.id == "1" }
                .map { it.name.notNullable() }
                .toMutableList()
        } else {
            recurrencesTypes
                .filter { it.name != null }
                .map { it.name.notNullable() }
                .toMutableList()
        }

        MaterialDialog(this).show {
            lifecycleOwner(this@ProposedTransferDateActivity)
            title(R.string.title_frequency)
            listItems(
                items = recurrenceNames,
                selection = { _, index, text ->
                    recurrencesType = recurrencesTypes[index]
                    proposedTransferDate.frequency = recurrencesType.name
                    proposedTransferDate.recurrenceTypeId = recurrencesType.id
                    if (text.toString().equals(FREQUENCY_TYPE_ONE_TIME, true)) {
                        proposedTransferDate.endDate = null
                        this@ProposedTransferDateActivity.textViewStartDate.setText(R.string.title_transaction_date)
                        this@ProposedTransferDateActivity.textViewTip.setText(R.string.title_tip)
                        this@ProposedTransferDateActivity.textViewTipMsg.setText(R.string.msg_tip)
                        this@ProposedTransferDateActivity.textViewEndDate.visibility = View.GONE
                        this@ProposedTransferDateActivity.textInputLayoutEnds.visibility = View.GONE
                        this@ProposedTransferDateActivity.textInputEditTextEnds.text?.clear()
                    } else {
                        this@ProposedTransferDateActivity.textViewStartDate.setText(R.string.title_start_date)
                        this@ProposedTransferDateActivity.textViewTip.setText(R.string.title_note)
                        this@ProposedTransferDateActivity.textViewEndDate.visibility = View.VISIBLE
                        this@ProposedTransferDateActivity.textInputLayoutEnds.visibility =
                            View.VISIBLE
                        this@ProposedTransferDateActivity.textInputEditTextEnds.setText(
                            currentOccurrencesText
                        )
                        setOccurrencesEndDate()
                    }
                    this@ProposedTransferDateActivity.textInputEditTextFrequency.setText(text)
                })
        }
    }

    private fun showRecurrencesDialog() {
        val recurrencesItem = 99
        val recurrenceNames = mutableListOf<String>()
        recurrenceNames.add(OCCURRENCE_DEFAULT_VALUE)
        for (i in 3..recurrencesItem) {
            recurrenceNames.add("After $i occurrences")
        }
        MaterialDialog(this).show {
            lifecycleOwner(this@ProposedTransferDateActivity)
            title(R.string.title_ends)
            listItems(
                items = recurrenceNames,
                selection = { _, _, text ->
                    this@ProposedTransferDateActivity.textInputEditTextEnds.setText(text)
                    this@ProposedTransferDateActivity.textViewTip.setText(R.string.title_note)
                    currentOccurrencesText = text.toString()
                    proposedTransferDate.occurrencesText = currentOccurrencesText
                    currentOccurrences = trimOccurrences(text.toString())
                    proposedTransferDate.occurrences = currentOccurrences
                    setOccurrencesEndDate()
                })
        }
    }

    private fun setOccurrencesEndDate() {
        val calendarOccurrenceDate = getDateOccurrencesEndDate()
        val dateString = viewUtil.getDateFormatByTimeMilliSeconds(
            calendarOccurrenceDate.timeInMillis,
            DateFormatEnum.DATE_FORMAT_DATE.value
        )
        proposedTransferDate.endDate = viewUtil.getDateFormatByCalendar(
            calendarOccurrenceDate,
            DateFormatEnum.DATE_FORMAT_ISO_Z.value
        )
        textViewTipMsg.text =
            formatString(
                R.string.msg_tip_3,
                recurrencesType.name?.toLowerCase(),
                formatString(
                    R.string.param_color,
                    convertColorResourceToHex(getAccentColor()),
                    dateString
                )
            ).toHtmlSpan()
    }

    private fun trimOccurrences(occurrences: String): Int {
        val trimmedOccurrences = occurrences.replace("After ", "")
        return trimmedOccurrences.replace(" occurrences", "").toInt().minus(1)
    }

    private fun getDateOccurrencesEndDate(): Calendar {
        val calendar = Calendar.getInstance()
        calendar.set(
            calendarStartDate.get(Calendar.YEAR),
            calendarStartDate.get(Calendar.MONTH),
            calendarStartDate.get(Calendar.DATE),
            calendarStartDate.get(Calendar.HOUR_OF_DAY),
            calendarStartDate.get(Calendar.MINUTE),
            calendarStartDate.get(Calendar.SECOND)
        )
        when {
            recurrencesType.name.equals("Daily", true) -> {
                calendar.add(Calendar.DAY_OF_WEEK, currentOccurrences)
            }
            recurrencesType.name.equals("Weekly", true) -> {
                calendar.add(Calendar.WEEK_OF_MONTH, currentOccurrences)
            }
            recurrencesType.name.equals("Monthly", true) -> {
                calendar.add(Calendar.MONTH, currentOccurrences)
            }
            recurrencesType.name.equals("Annually", true) -> {
                calendar.add(Calendar.YEAR, currentOccurrences)
            }
        }
        return calendar
    }

    private fun showLoading() {
        constraintLayoutContent.visibility(false)
        viewLoadingState.visibility(true)
    }

    private fun dismissLoading() {
        viewLoadingState.visibility(false)
    }

    private fun setTextInputEditTextTransferDate() {
        if (!isConvertedTimeZone) {
            calendarStartDate =
                viewUtil.convertCalendarByTimeZone(calendarStartDate, TimeZoneEnum.GMT_8.value)
            isConvertedTimeZone = true
        }
        val startDate = viewUtil.getDateFormatByTimeMilliSeconds(
            calendarStartDate.timeInMillis,
            DateFormatEnum.DATE_FORMAT_DEFAULT.value
        )
        textInputEditTextStartDate.setText(
            if (!viewUtil.isGMTPlus8()) formatString(
                R.string.param_gmt_8_value,
                startDate
            ) else startDate
        )
    }

    private fun clickSubmit() {
        if (proposedTransferDate.immediately == true) {
            proposedTransferDate.endDate = null
            proposedTransferDate.occurrencesText = null
            proposedTransferDate.occurrences = null
            proposedTransferDate.recurrenceTypeId = null
            proposedTransferDate.frequency = null
            proposedTransferDate.startDate = viewUtil.getDateFormatByTimeMilliSeconds(
                viewUtil.getCurrentDate(),
                ViewUtil.DATE_FORMAT_ISO_Z
            )
        }
        eventBus.proposedTransferDateSyncEvent.emmit(
            BaseEvent(
                ProposedTransferDateSyncEvent.ACTION_SET_PROPOSED_TRANSFER_DATE,
                proposedTransferDate
            )
        )
        onBackPressed()
    }

    companion object {
        const val EXTRA_PAGE = "page"
        const val EXTRA_PTD = "ptd"
        const val EXTRA_BILLER_ID = "biller_id"
        const val EXTRA_ENABLED_SCHEDULED = "enabled_scheduled"
        const val EXTRA_SCHEDULED_PERMISSION = "scheduled_permission"
        const val EXTRA_HAS_VALID_FORM = "has_valid_form"

        const val PAGE_FUND_TRANSFER = "fund_transfer"
        const val PAGE_BILLS_PAYMENT = "bills_payment"

        const val FREQUENCY_TYPE_ONE_TIME = "One-time"
        const val OCCURRENCE_DEFAULT_VALUE = "After 2 occurrences"
    }
}
