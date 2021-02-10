package com.unionbankph.corporate.app.common.extension

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BulletSpan
import android.util.TypedValue
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatEditText
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.jakewharton.rxbinding2.widget.RxTextView
import com.unionbankph.corporate.app.common.widget.ImprovedBulletSpan
import com.unionbankph.corporate.app.common.widget.LiTagHandler
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.constant.CurrencyEnum
import com.unionbankph.corporate.common.presentation.constant.DateFormatEnum
import io.reactivex.Observable
import timber.log.Timber
import java.io.File
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Since we don't need our objects to be thread safe most of the time, we can use  `LazyThreadSafetyMode.NONE` which has lower
 * overhead.
 */
fun <T> lazyFast(operation: () -> T): Lazy<T> = lazy(LazyThreadSafetyMode.NONE) {
    operation()
}

inline fun <reified T : Any> cast(any: Any?): T? {
    return T::class.javaObjectType.cast(any)
}

fun Boolean?.notNullable(): Boolean {
    return this ?: false
}

fun Int?.notNullable(): Int {
    return this ?: 0
}

fun String?.notNullable(): String {
    return this ?: ""
}

fun String?.nullable(): String? {
    return if (this == "") null else this
}

fun String?.notEmpty(): String {
    return if (this == "" || this == null) "-" else this
}

fun <T, K, R> LiveData<T>.combineWith(
    liveData: LiveData<K>,
    block: (T?, K?) -> R
): LiveData<R> {
    val result = MediatorLiveData<R>()
    result.addSource(this) {
        result.value = block(this.value, liveData.value)
    }
    result.addSource(liveData) {
        result.value = block(this.value, liveData.value)
    }
    return result
}

@SuppressLint("SimpleDateFormat")
fun String?.isValidDateFormat(dateFormatEnum: DateFormatEnum): Boolean {
    if (this == null) return false
    var date: Date? = null
    try {
        val sdf = SimpleDateFormat(dateFormatEnum.value)
        date = sdf.parse(this)
        if (this != sdf.format(date)) {
            date = null
        }
    } catch (ex: Exception) {
        Timber.e(ex, "isValidDateFormat")
    }

    return date != null
}

@SuppressLint("SimpleDateFormat")
fun String?.convertDateToDesireFormat(dateFormatEnum: DateFormatEnum): String? {
    val formatStrings = mutableListOf(
        DateFormatEnum.DATE_FORMAT_ISO.value,
        DateFormatEnum.DATE_FORMAT_ISO_Z.value,
        DateFormatEnum.DATE_FORMAT_ISO_WITHOUT_T.value,
        DateFormatEnum.DATE_FORMAT_ISO_DATE.value,
        DateFormatEnum.DATE_FORMAT_DATE_SLASH.value
    )
    this?.let {
        formatStrings.forEach {
            try {
                val date = SimpleDateFormat(it).parse(this)
                val simpleDateFormat = SimpleDateFormat(dateFormatEnum.value)
                return simpleDateFormat.format(date)
            } catch (e: Exception) {
            }
        }
    }
    return this
}

@SuppressLint("SimpleDateFormat")
fun String?.convertToCalendar(): Calendar {
    val formatStrings = mutableListOf(
        DateFormatEnum.DATE_FORMAT_ISO.value,
        DateFormatEnum.DATE_FORMAT_ISO_Z.value,
        DateFormatEnum.DATE_FORMAT_ISO_WITHOUT_T.value,
        DateFormatEnum.DATE_FORMAT_ISO_DATE.value,
        DateFormatEnum.DATE_FORMAT_DATE_SLASH.value
    )
    this?.let {
        formatStrings.forEach {
            try {
                val calendarEndDate = Calendar.getInstance()
                val date = SimpleDateFormat(it).parse(this)
                calendarEndDate.time = date
                return calendarEndDate
            } catch (e: ParseException) {
            }
        }
    }
    return Calendar.getInstance()
}

fun String?.formatAmount(currency: String?): String {
    this?.let {
        return try {
            val formatter = DecimalFormat("#,##0.00")
            "${currency ?: CurrencyEnum.PHP.name} " + formatter.format(it.toDouble())
        } catch (e: Exception) {
            this
        }
    }
    return Constant.EMPTY
}

fun String?.formatAccountNumber(): String {
    if (this == null) return Constant.EMPTY
    val accountNumberStringBuilder = StringBuilder()
    this.forEachIndexed { index, c ->
        if ((index + 1) % 4 == 0) {
            accountNumberStringBuilder.append("$c ")
        } else {
            accountNumberStringBuilder.append(c)
        }
    }
    return accountNumberStringBuilder.toString().trim()
}

fun String?.isEmpty(): Boolean {
    return when {
        this == "" -> true
        this == null -> true
        else -> false
    }
}

fun AppCompatEditText.asDriver(): Observable<String> {
    return RxTextView.textChanges(this).skip(1).map { it.toString() }
}

fun Context.formatString(stringResId: Int, vararg args: Any?): String =
    String.format(getString(stringResId, *args))

fun String.toHtmlSpan(): Spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
    Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
} else {
    Html.fromHtml(this)
}

fun SpannableStringBuilder.toHtmlSpan(): Spanned =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(this.toString(), Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(this.toString())
    }

fun File.toBitmap(): Bitmap {
    return BitmapFactory.decodeFile(this.path)
}

fun Context.getHtmlSpannedString(
    @StringRes
    id: Int
): Spanned = getString(id).toHtmlSpan()

fun Context.getHtmlSpannedString(
    @StringRes
    id: Int,
    vararg formatArgs: Any?
): Spanned = getString(id, *formatArgs).toHtmlSpan()

fun Context.getQuantityHtmlSpannedString(
    @PluralsRes
    id: Int,
    quantity: Int
): Spanned = resources.getQuantityString(id, quantity).toHtmlSpan()

fun Context.getQuantityHtmlSpannedString(
    @PluralsRes
    id: Int,
    quantity: Int,
    vararg formatArgs: Any?
): Spanned = resources.getQuantityString(id, quantity, *formatArgs).toHtmlSpan()

fun String.supportBullets(context: Context): SpannableStringBuilder {
    val htmlSpannable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(this, Html.FROM_HTML_MODE_COMPACT)
    } else {
        Html.fromHtml(this, null, LiTagHandler())
    }

    val spannableBuilder = SpannableStringBuilder(htmlSpannable)
    val bulletSpans = spannableBuilder.getSpans(0, spannableBuilder.length, BulletSpan::class.java)
    bulletSpans.forEach {
        val start = spannableBuilder.getSpanStart(it)
        val end = spannableBuilder.getSpanEnd(it)
        spannableBuilder.removeSpan(it)
        spannableBuilder.setSpan(
            ImprovedBulletSpan(bulletRadius = dip(context, 2), gapWidth = dip(context, 8)),
            start,
            end,
            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )
    }
    return spannableBuilder
}

private fun dip(context: Context, dp: Int): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        context.resources.displayMetrics
    ).toInt()
}