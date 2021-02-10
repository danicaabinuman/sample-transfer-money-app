package com.unionbankph.corporate.app.common.extension

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import com.wdullaer.materialdatetimepicker.time.Timepoint
import java.util.*

fun getCalendar(): Calendar {
    return Calendar.getInstance()
}

fun getMinCalendar(): Calendar {
    return getCalendar().apply { add(Calendar.YEAR, -20) }
}

fun getMaxCalendar(): Calendar {
    return getCalendar().apply { add(Calendar.YEAR, 20) }
}

fun Fragment.showDatePicker(
    minDate: Calendar? = getMinCalendar(),
    maxDate: Calendar = getMaxCalendar(),
    calendar: Calendar = getCalendar(),
    callback: DatePickerDialog.OnDateSetListener
) {
    (activity as AppCompatActivity).showDatePicker(minDate, maxDate, calendar, callback)
}

fun AppCompatActivity.showDatePicker(
    minDate: Calendar? = getMinCalendar(),
    maxDate: Calendar = getMaxCalendar(),
    calendar: Calendar = getCalendar(),
    callback: DatePickerDialog.OnDateSetListener
) {
    val datePickerDialog = DatePickerDialog.newInstance(
        callback,
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    datePickerDialog.minDate = minDate
    datePickerDialog.maxDate = maxDate
    datePickerDialog.show(
        fragmentManager,
        this::class.java.simpleName
    )
}

fun AppCompatActivity.showTimerPicker(
    calendar: Calendar,
    timePointsArray: Array<Timepoint>,
    callback: TimePickerDialog.OnTimeSetListener
) {
    val timePickerDialog = TimePickerDialog.newInstance(
        callback,
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        false
    )
    timePickerDialog.setSelectableTimes(timePointsArray)
    timePickerDialog.show(
        fragmentManager,
        this::class.java.simpleName
    )
}
