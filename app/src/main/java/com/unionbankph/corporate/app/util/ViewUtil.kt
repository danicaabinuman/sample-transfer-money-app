package com.unionbankph.corporate.app.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Handler
import android.provider.MediaStore
import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.redmadrobot.inputmask.MaskedTextChangedListener
import com.redmadrobot.inputmask.PolyMaskTextChangedListener
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.App
import com.unionbankph.corporate.app.common.extension.clearTheme
import com.unionbankph.corporate.app.common.extension.notEmpty
import com.unionbankph.corporate.app.common.extension.setContextCompatBackgroundColor
import com.unionbankph.corporate.app.common.extension.setThemeToDark
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidationResult
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidator
import com.unionbankph.corporate.app.receiver.IMMResultReceiver
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.constant.DateFormatEnum
import io.reactivex.Observable
import me.leolin.shortcutbadger.ShortcutBadger
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

/**
 * Created by Herald Santos on 7/3/2017.
 */
class ViewUtil(private val mContext: Context) {

    companion object {
        const val DATE_FORMAT_ISO = "yyyy-MM-dd'T'HH:mm:ss.SSS"
        const val DATE_FORMAT_ISO_Z = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
        const val DATE_FORMAT_ISO_WITHOUT_T = "yyyy-MM-dd HH:mm:ss.SSS"
        const val DATE_FORMAT_ISO_DATE = "yyyy-MM-dd"
        const val DATE_FORMAT_DEFAULT = "MMMM dd, yyyy, h:mm a"
        const val DATE_FORMAT_DATE = "MMMM dd, yyyy"
        const val DATE_FORMAT_THREE_DATE = "MMM dd, yyyy"
        const val DATE_FORMAT_DATE_SLASH = "MM/dd/yyyy"
        const val DATE_FORMAT_WITHOUT_TIME = "MMMM dd, yyyy, EEEE"
        const val DATE_FORMAT_TIME = "h:mm a"

        const val REGEX_FORMAT_MOBILE_NUMBER_PH = "^9\\d+\$"
        const val REGEX_FORMAT_HAS_NUMBER = ".*\\d+.*"
        const val REGEX_FORMAT_HAS_ALPHA = ".*[A-Za-z].*"
        const val REGEX_FORMAT_HAS_ALPHA_UPPERCASE = ".*[A-Z].*"
        const val REGEX_FORMAT_HAS_SYMBOL = ".*[^A-Za-z0-9].*"
        const val REGEX_FORMAT_HAVE_NO_WHITE_SPACE = "^(?=.*\d)[a-zA-Z\d]{8,13}$"
//        const val REGEX_FORMAT_HAVE_NO_WHITE_SPACE = "^(?=.*\\d)[a-zA-Z\\d]{8,13}\$"
        const val REGEX_FORMAT_ALPHA_NUMERIC = ".*^[a-zA-Z0-9].*"
        const val REGEX_FORMAT_VALID_AMOUNT = "^(.*[^.]\$)"
        const val REGEX_FORMAT_AMOUNT_CHECK_DEPOSIT =
            "(?![,.0]*\$)(^([A-Z]{1,3} )?(?![,.0]*\$)[1-4]*[0-9]{0,2}(,{0,1}\\d{3})?(.[0-9]{1,2})?\$|^499,999(.0{1,2})?\$)"
        const val REGEX_FORMAT_AMOUNT =
            "(?![,.0]*\$)(^([A-Z]{1,3} )?(?![,.0]*\$)[0-9]+(,\\d{3})*?(\\.\\d+?)?\$)"
        const val REGEX_FORMAT_AMOUNT_OPTIONAL =
            "(^([A-Z]{1,3} )?(\\d+|\\d{1,3}(,\\d{3})*)(\\.\\d+)?\$)"
    }

    private val formatStrings =
        mutableListOf(DATE_FORMAT_ISO, DATE_FORMAT_ISO_WITHOUT_T, DATE_FORMAT_ISO_DATE)

    /**
     * Keyboard
     */
    fun showKeyboard(activity: Activity) {
        val mContext = activity.applicationContext
        val inputMethodManager = mContext
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    fun dismissKeyboard(activity: Activity) {
        val focus = activity.currentFocus
        if (focus != null) {
            val mContext = activity.applicationContext
            val inputMethodManager = mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(focus.windowToken, 0)
        }
    }

    fun isShownKeyboard(view: View, activity: Activity): Boolean? {
        val result = IMMResultReceiver()
        val res: Int
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, 0, result)
        // if keyboard doesn't change, handle the keypress
        res = result.result
        return res == InputMethodManager.RESULT_UNCHANGED_SHOWN ||
                res == InputMethodManager.RESULT_UNCHANGED_HIDDEN
    }

    fun isShownKeyboard(TAG: String, contentView: View): Boolean? {
        val isShown = booleanArrayOf(false)
        contentView.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            contentView.getWindowVisibleDisplayFrame(r)
            val screenHeight = contentView.rootView.height

            // r.bottom is the position above soft keypad or device button.
            // if keypad is shown, the r.bottom is smaller than that before.
            val keypadHeight = screenHeight - r.bottom

            Log.d(TAG, "keypadHeight = $keypadHeight")

            isShown[0] = keypadHeight > screenHeight * 0.15
        }
        return isShown[0]
    }

    fun isSoftKeyboardShown(view: View?): Boolean {
        val r = Rect()
        // r will be populated with the coordinates of your view that area still
        // visible.
        var keypadHeight = 0
        var screenHeight = 0
        // r will be populated with the coordinates of your view that area still visible.
        if (view != null) {
            view.getWindowVisibleDisplayFrame(r)
            screenHeight = view.rootView.height
            keypadHeight = screenHeight - r.bottom
        }
        return keypadHeight > screenHeight * 0.15
    }

    /**
     * Edittext
     * TextInputLayout
     */
    fun setInputType(editText: EditText?, inputTypeEnum: String?) {
        when (inputTypeEnum) {
            "string",
            "email address" -> editText?.inputType =
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS or
                        InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS
            "numeric_padding",
            "digit",
            "numeric",
            "number" -> editText?.inputType = InputType.TYPE_CLASS_NUMBER
            "multiline" -> editText?.inputType =
                InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            else -> editText?.inputType = InputType.TYPE_CLASS_TEXT
        }
    }

    fun setEditTextMaskListener(editText: EditText, maskFormat: String) {
        val affineFormats = ArrayList<String>()
        affineFormats.add(maskFormat)
        val listener = PolyMaskTextChangedListener(
            maskFormat, affineFormats, editText,
            object : MaskedTextChangedListener.ValueListener {
                override fun onTextChanged(maskFilled: Boolean, extractedValue: String) {
                    Timber.d("extractedValue: $extractedValue")
                    Timber.d("maskFilled: $maskFilled")
                }
            }
        )
        editText.addTextChangedListener(listener)
        editText.onFocusChangeListener = listener
    }

    fun setEditTextMaxLength(editText: EditText?, length: Int) {
        val filterArray = arrayOfNulls<InputFilter>(1)
        filterArray[0] = InputFilter.LengthFilter(length)
        editText?.filters = filterArray
    }

    fun setEditTextFilter(editText: EditText?, regex: String) {
        editText?.filters = arrayOf(InputFilter { source, start, end, dest, dstart, dend ->
            if (source.toString().matches(Regex(regex))) {
                null
            } else ""
        })
    }

    fun getTextInputLayout(editText: View?): TextInputLayout? {
        var currentView = editText
        for (i in 0..1) {
            val parent = currentView?.parent
            if (parent is TextInputLayout) {
                return parent
            } else {
                currentView = parent as View
            }
        }
        return null
    }

    fun rxTextChangesAmount(
        isFocusChanged: Boolean,
        isValueChanged: Boolean,
        et: EditText,
        vararg patternMatches: RxValidator.PatternMatch
    ): Observable<RxValidationResult<EditText>> {
        val rxValidator = RxValidator.createFor(et).apply {
            patternMatches.forEach {
                patternMatches(
                    it.invalidValueMessage,
                    it.pattern
                )
            }
            if (isFocusChanged) {
                onFocusChanged()
            }
            if (isValueChanged) {
                onValueChanged()
            } else {
                onSubscribe()
            }
        }
        return if (isValueChanged) rxValidator.toObservable()
            .debounce {
                Observable.timer(
                    et.resources.getInteger(R.integer.time_edit_text_debounce).toLong(),
                    TimeUnit.MILLISECONDS
                )
            } else rxValidator.toObservable()
    }

    fun rxTextChanges(
        isFocusChanged: Boolean,
        isValueChanged: Boolean,
        minLength: Int,
        maxLength: Int,
        editText: EditText,
        customErrorMessage: String? = null
    ): Observable<RxValidationResult<EditText>> {

        val rxValidator = RxValidator.createFor(editText)
            .nonEmpty(
                customErrorMessage ?: if (editText.hint != null) {
                    String.format(
                        mContext.getString(R.string.error_specific_field),
                        editText.hint
                    )
                } else {
                    String.format(
                        mContext.getString(R.string.error_specific_field),
                        getTextInputLayout(editText)?.hint
                    )
                }
            )

        if (isFocusChanged) {
            rxValidator.onFocusChanged()
        }
        if (isValueChanged) {
            rxValidator.onValueChanged()
        } else {
            rxValidator.onSubscribe()
        }
        if (minLength > 0) {
            rxValidator.minLength(
                minLength,
                customErrorMessage ?: String.format(
                    mContext.getString(R.string.error_validation_min),
                    minLength.toString()
                )
            )
        }

        if (maxLength > editText.text.toString().trim { it <= ' ' }.length) {
            rxValidator.maxLength(
                maxLength,
                String.format(
                    mContext.getString(R.string.error_validation_max),
                    maxLength.toString()
                )
            )
        }

        return if (isValueChanged) rxValidator.toObservable()
            .debounce {
                Observable.timer(
                    editText.resources.getInteger(R.integer.time_edit_text_debounce).toLong(),
                    TimeUnit.MILLISECONDS
                )
            } else rxValidator.toObservable()
    }

    fun setError(editText: RxValidationResult<EditText>) {
        val editTextParent = getTextInputLayout(editText.item)
        if (editTextParent != null) {
            if (editText.item.tag != null && editText.item.tag.toString() == Constant.REFRESH) {
                editTextParent.error = null
                editTextParent.isErrorEnabled = false
                editText.item.tag = Constant.CLEAR
            } else {
                editTextParent.error = if (!editText.isProper) editText.message else null
                editTextParent.isErrorEnabled = !editText.isProper
            }
        } else {
            if (editText.item.tag != null && editText.item.tag.toString() == Constant.REFRESH) {
                editText.item.error = null
                editText.item.tag = Constant.CLEAR
            } else {
                editText.item.error = if (!editText.isProper) editText.message else null
            }
        }
    }

    fun setCounterFlowError(editText: RxValidationResult<EditText>) {
        val editTextParent = getTextInputLayout(editText.item)
        if (editTextParent != null) {
            if (editText.item.tag != null && editText.item.tag.toString() == Constant.REFRESH) {
                editTextParent.error = null
                editText.item.tag = Constant.CLEAR
            } else {
                editTextParent.error = if (!editText.isProper) editText.message else null
            }
        }
    }

    fun setPasswordConfirmError(editText: RxValidationResult<EditText>, imageView: ImageView) {
        imageView.let {
            val editTextParent = getTextInputLayout(editText.item)
            if (editTextParent != null) {
                if (editText.isProper) {
                    it.clearTheme()
                    it.setImageResource(R.drawable.ic_password_valid)
                } else {
                    it.setThemeToDark(App.isSME())
                    it.setImageResource(R.drawable.ic_password_invalid)
                }
            }
        }
    }

    /**
     * Snackbar
     */
    fun snackBarMessage(view: View, message: String, time: Int): Snackbar {
        val snackbar = Snackbar.make(view, message, time)
        initSnackBarView(snackbar.view, R.color.colorWhiteDirty, R.color.colorWhiteDirty)
        return snackbar
    }

    fun snackBarWithAction(
        view: View,
        message: String,
        action: String,
        duration: Int,
        callback: View.OnClickListener
    ): Snackbar {
        val snackBar = Snackbar.make(
            view,
            message,
            duration
        )
        snackBar.setAction(action, callback)
        snackBar.setActionTextColor(
            ContextCompat.getColor(view.context, R.color.colorWhiteDirty)
        )
        initSnackBarView(snackBar.view, R.color.colorWhiteDirty, R.color.colorWhiteDirty)
        return snackBar
    }

    private fun initSnackBarView(view: View, textColor: Int, actionColor: Int) {
        val textView =
            view.findViewById<View>(R.id.snackbar_text) as TextView
        val button = view.findViewById<View>(R.id.snackbar_action) as Button

        textView.setTextColor(ContextCompat.getColor(mContext, textColor))
        textView.maxLines = mContext.resources.getInteger(R.integer.snack_bar_max_line)

        button.setTextColor(ContextCompat.getColor(mContext, actionColor))

        view.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorGray))
        val viewParams = view.layoutParams as ViewGroup.MarginLayoutParams
        viewParams.setMargins(
            viewParams.leftMargin,
            viewParams.topMargin,
            viewParams.rightMargin,
            viewParams.bottomMargin + getNavHeight()
        )
    }

    fun showCustomSnackBar(view: View, layout: Int, duration: Int): Snackbar {
        val snackBar = Snackbar.make(view, "", duration)
        // inflate view
        val snackView = LayoutInflater.from(mContext).inflate(layout, null)
        snackBar.view.setContextCompatBackgroundColor(R.color.colorWhite)
        // set background
        // snackbar.view.background = mContext.getDrawable(R.drawable.round_edges)
        val snackBarView = snackBar.view as Snackbar.SnackbarLayout
        val parentParams = snackBarView.layoutParams
        // margin sides
        // parentParams.setMargins(marginFromSides, 0, marginFromSides, marginFromSides)
        parentParams.height = FrameLayout.LayoutParams.WRAP_CONTENT
        parentParams.width = FrameLayout.LayoutParams.MATCH_PARENT
        snackBarView.layoutParams = parentParams
        snackBarView.addView(snackView, 0)
        return snackBar
    }

    /**
     * Json assets reader
     */
    fun getFileText(activity: Activity, txtFile: String): String {
        val text: String?
        try {
            val inputStream = activity.assets.open("txt/$txtFile.txt")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            text = String(buffer)
        } catch (ex: IOException) {
            Timber.e(ex, "getFileText")
            return ""
        }
        return text
    }

    fun loadJSONFromAsset(activity: Activity, file: String): String {
        val json: String?
        try {
            val inputStream = activity.assets.open("json/$file.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            json = String(buffer)
        } catch (ex: IOException) {
            Timber.e(ex, "loadJSONFromAsset")
            return ""
        }

        return json
    }

    /**
     * Other views
     */
    fun scrollToView(scrollViewParent: NestedScrollView, view: View) {
        // Get deepChild Offset
        val childOffset = Point()
        getDeepChildOffset(scrollViewParent, view.parent, view, childOffset)
        // Scroll to child.
        scrollViewParent.smoothScrollTo(0, childOffset.y)
    }

    fun getDeepChildOffset(
        mainParent: ViewGroup,
        parent: ViewParent,
        child: View,
        accumulatedOffset: Point
    ) {
        val parentGroup = parent as ViewGroup
        accumulatedOffset.x += child.left
        accumulatedOffset.y += child.top
        if (parentGroup == mainParent) {
            return
        }
        getDeepChildOffset(mainParent, parentGroup.parent, parentGroup, accumulatedOffset)
    }

    fun setWindowFlag(activity: Activity, bits: Int, on: Boolean) {
        val win = activity.window
        val winParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }

    fun setTint(drawable: Drawable, color: Int): Drawable {
        val wrappedDrawable = DrawableCompat.wrap(drawable)
        DrawableCompat.setTint(wrappedDrawable, color)
        return wrappedDrawable
    }

    fun makeBadge(count: Int, imageView: AppCompatImageView, textView: TextView) {
        if (count > 0) {
            imageView.visibility = View.VISIBLE
            textView.visibility = View.VISIBLE
            textView.text = count.toString()
        } else {
            imageView.visibility = View.GONE
            textView.visibility = View.GONE
        }
    }

    fun getBytes(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }

    private fun getNavHeight(): Int {
        val resources = mContext.resources
        return if (hasNavBar(resources)) {
            val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
            return if (resourceId > 0) {
                resources.getDimensionPixelSize(resourceId)
            } else 0
        } else 0
    }

    private fun hasNavBar(resources: Resources): Boolean {
        val id = resources.getIdentifier("config_showNavigationBar", "bool", "android")
        return id > 0 && resources.getBoolean(id)
    }

    fun getScreenWidth(activity: Activity): Int {
        val display = activity.windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        return size.x
    }

    fun getScreenHeight(activity: Activity): Int {
        val display = activity.windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        return size.y
    }

    fun setToolbarMenu(
        menuItem: MenuItem,
        layoutId: Int,
        id: Int,
        onClickListener: View.OnClickListener
    ) {
        menuItem.setActionView(layoutId)
        menuItem.isVisible = true
        val view = menuItem.actionView.findViewById<View>(id)
        view.setOnClickListener(onClickListener)
    }

    fun setGreyscale(v: View, greyscale: Boolean): View {
        if (greyscale) {
            // Create a paint object with 0 saturation (black and white)
            val cm = ColorMatrix()
            cm.setSaturation(0f)

            val greyscalePaint = Paint()
            greyscalePaint.colorFilter = ColorMatrixColorFilter(cm)
            // Create a hardware layer with the greyscale paint
            v.setLayerType(View.LAYER_TYPE_HARDWARE, greyscalePaint)
        } else {
            // Remove the hardware layer
            v.setLayerType(View.LAYER_TYPE_NONE, null)
        }
        return v
    }

    fun setShortCutBadge(context: Context, badgeCount: Int) {
        if (badgeCount > 0) {
            ShortcutBadger.applyCount(context, badgeCount)
        } else {
            ShortcutBadger.removeCount(context)
        }
    }

    fun makeMeasureSpec(dimension: Int): Int {
        return if (dimension > 0) {
            View.MeasureSpec.makeMeasureSpec(dimension, View.MeasureSpec.EXACTLY)
        } else {
            View.MeasureSpec.UNSPECIFIED
        }
    }

    fun animateRecyclerView(recyclerView: RecyclerView, isVisible: Boolean) {
        recyclerView.visibility = if (isVisible) View.VISIBLE else View.GONE
        recyclerView.post {
            val context = recyclerView.context
            val animation =
                AnimationUtils.loadLayoutAnimation(
                    context,
                    R.anim.anim_layout_animation_fall_down
                )
            recyclerView.layoutAnimation = animation
            recyclerView.scheduleLayoutAnimation()
        }
    }

    fun setFocusOnView(scrollView: ScrollView, view: View) {
        scrollView.post {
            scrollView.scrollTo(0, view.top)
        }
    }

    fun setSmoothScrollFocusOnView(scrollView: ScrollView, view: View) {
        Handler().postDelayed(
            {
                scrollView.smoothScrollTo(0, view.bottom)
            }, view.resources.getInteger(R.integer.time_delay_smooth_scroll).toLong()
        )
    }

    fun getDrawableById(id: String): Int {
        return try {
            val res = R.drawable::class.java
            val field = res.getField(id)
            return field.getInt(null)
        } catch (e: Exception) {
            R.drawable.ic_help_outline_gray_24dp
        }
    }

    fun startAnimateView(isShown: Boolean, view: View, animationResId: Int) {
        val animation = AnimationUtils.loadAnimation(mContext, animationResId)
        view.startAnimation(animation)
        view.visibility(isShown)
    }

    /**
     * Dates
     * Calendars
     */
    fun getCurrentMonth(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.MONTH)
    }

    @SuppressLint("SimpleDateFormat")
    fun isValidDateFormat(format: String, value: String?): Boolean {
        if (value == null) return false
        var date: Date? = null
        try {
            val sdf = SimpleDateFormat(format)
            date = sdf.parse(value)
            if (value != sdf.format(date)) {
                date = null
            }
        } catch (ex: Exception) {
            Timber.e(ex, "isValidDateFormat")
        }

        return date != null
    }

    fun getDay(): String {
        val calendar = Calendar.getInstance()
        val simpleDateFormat = SimpleDateFormat("EEEE")
        return simpleDateFormat.format(calendar.time)
    }

    fun getDateDay(): String {
        val calendar = Calendar.getInstance()
        val simpleDateFormat = SimpleDateFormat("dd")
        return simpleDateFormat.format(calendar.time)
    }

    fun getMonth(): String {
        val calendar = Calendar.getInstance()
        val simpleDateFormat = SimpleDateFormat("MMMM")
        return simpleDateFormat.format(calendar.time)
    }

    @SuppressLint("SimpleDateFormat")
    fun isGMTPlus8(): Boolean {
        val calendar = Calendar.getInstance()
        return SimpleDateFormat("Z").format(calendar.time) == "+0800"
    }

    fun getCurrentYear(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.YEAR)
    }

    fun getCurrentDate(): Long {
        return System.currentTimeMillis() - 1000
    }

    @SuppressLint("SimpleDateFormat")
    fun getCurrentDateString(): String {
        val date = Calendar.getInstance().time
        val simpleDateFormat = SimpleDateFormat(DATE_FORMAT_DEFAULT)
        return simpleDateFormat.format(date)
    }

    fun getYearInMillis(year: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        return calendar.timeInMillis
    }

    @SuppressLint("SimpleDateFormat")
    fun getCalendarByDateString(dateString: String?, givenFormat: String): Calendar {
        val calendarEndDate = Calendar.getInstance()
        val date = SimpleDateFormat(givenFormat).parse(dateString)
        calendarEndDate.time = date
        return calendarEndDate
    }

    @SuppressLint("SimpleDateFormat")
    fun getDateFormatByDateString(
        dateString: String?,
        givenFormat: String,
        desireFormat: String
    ): String {
        return if (dateString != null) {
            try {
                val date = SimpleDateFormat(givenFormat).parse(dateString)
                val simpleDateFormat = SimpleDateFormat(desireFormat)
                simpleDateFormat.format(date)
            } catch (e: Exception) {
                dateString.notEmpty()
            }
        } else {
            Constant.EMPTY
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun getDateFormatByTimeMilliSeconds(timeMilliSeconds: Long?, desireFormat: String): String {
        return if (timeMilliSeconds != null) {
            val simpleDateFormat = SimpleDateFormat(desireFormat)
            simpleDateFormat.format(timeMilliSeconds)
        } else {
            Constant.EMPTY
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun findDateFormatByDateString(dateString: String?, desireFormat: String): String? {
        formatStrings.forEach {
            try {
                val date = SimpleDateFormat(it).parse(dateString)
                val simpleDateFormat = SimpleDateFormat(desireFormat)
                return simpleDateFormat.format(date)
            } catch (e: Exception) {
                dateString.notEmpty()
            }
        }
        return Constant.EMPTY
    }

    @SuppressLint("SimpleDateFormat")
    fun getCalendarByTimeZone(timeZone: String): Calendar {
        val calendar = Calendar.getInstance()
        val dateString =
            getDateByTimeZoneFormat(calendar, DateFormatEnum.DATE_FORMAT_ISO.value, timeZone)
        return getCalendarByDateString(dateString, DateFormatEnum.DATE_FORMAT_ISO.value)
    }

    @SuppressLint("SimpleDateFormat")
    fun getDateFormatByCalendar(calendar: Calendar?, desireFormat: String): String {
        val simpleDateFormat = SimpleDateFormat(desireFormat)
        return simpleDateFormat.format(calendar?.timeInMillis)
    }

    @SuppressLint("SimpleDateFormat")
    fun getDateByTimeZoneFormat(
        calendar: Calendar,
        desireFormat: String,
        timeZone: String
    ): String {
        val simpleDateFormat = SimpleDateFormat(desireFormat)
        simpleDateFormat.timeZone = TimeZone.getTimeZone(timeZone)
        return simpleDateFormat.format(calendar.time)
    }

    fun convertCalendarByTimeZone(calendar: Calendar, timeZone: String): Calendar {
        if (!isGMTPlus8()) {
            val dateString =
                getDateByTimeZoneFormat(calendar, DateFormatEnum.DATE_FORMAT_ISO.value, timeZone)
            return getCalendarByDateString(dateString, DateFormatEnum.DATE_FORMAT_ISO.value)
        }
        return calendar
    }

    @SuppressLint("SimpleDateFormat")
    fun getDateTimeAgoByCreatedDate(createdDate: String?): String {
        var convertedTime = ""
        try {
            val dateFormat = SimpleDateFormat(DATE_FORMAT_ISO_WITHOUT_T)
            val pastDate = dateFormat.parse(createdDate)
            val nowTime = Date()
            val dateDiff = nowTime.time - pastDate.time

            val simpleDateFormat = SimpleDateFormat(DATE_FORMAT_THREE_DATE)
            val formattedDate = simpleDateFormat.format(pastDate)

            val second = TimeUnit.MILLISECONDS.toSeconds(dateDiff)
            val minute = TimeUnit.MILLISECONDS.toMinutes(dateDiff)
            val hour = TimeUnit.MILLISECONDS.toHours(dateDiff)
            val day = TimeUnit.MILLISECONDS.toDays(dateDiff)
            when {
                second < 60 -> convertedTime = "Just Now"
                minute < 60 -> convertedTime = "${minute}m"
                hour <= 24 -> convertedTime = "${hour}h"
                day >= 1 -> convertedTime = formattedDate
            }
        } catch (e: Exception) {
            Timber.e(e, "getDateTimeAgoByCreatedDate")
        }
        return convertedTime
    }

    /**
     * Capture view thru Bitmap
     */
    fun getBitmapByView(view: View): Bitmap {
        val bitmap: Bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) {
            bgDrawable.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return bitmap
    }

    fun saveReceipt(cr: ContentResolver, viewHeader: View, viewBody: View, title: String) {
        // Define a bitmap with the same size as the view
        val bodyBitmap =
            Bitmap.createBitmap(viewBody.width, viewBody.height, Bitmap.Config.ARGB_8888)
        val headerBitmap =
            Bitmap.createBitmap(viewHeader.width, viewHeader.height, Bitmap.Config.ARGB_8888)
        val receiptBitmap = Bitmap.createBitmap(
            viewBody.width,
            viewHeader.height + viewBody.height,
            Bitmap.Config.ARGB_8888
        )
        // Bind a canvas to it
        val canvasHeader = Canvas(headerBitmap)
        // Get the view's background
        var bgDrawable = viewHeader.background
        if (bgDrawable != null)
        // has background drawable, then draw it on the canvas
            bgDrawable.draw(canvasHeader)
        else
        // does not have background drawable, then draw white background on the canvas
            canvasHeader.drawColor(Color.WHITE)
        // draw the view on the canvas
        viewHeader.draw(canvasHeader)

        val canvasBody = Canvas(bodyBitmap)
        // Get the view's background
        bgDrawable = viewBody.background
        if (bgDrawable != null)
        // has background drawable, then draw it on the canvas
            bgDrawable.draw(canvasBody)
        else
        // does not have background drawable, then draw white background on the canvas
            canvasBody.drawColor(Color.WHITE)
        // draw the view on the canvas
        viewBody.draw(canvasBody)
        // return the bitmap

        val canvasReceipt = Canvas(receiptBitmap)
        canvasReceipt.drawBitmap(headerBitmap, 0F, 0f, null)
        canvasReceipt.drawBitmap(bodyBitmap, 0f, headerBitmap.height.toFloat(), null)

        MediaStore.Images.Media.insertImage(cr, receiptBitmap, title, "")
    }

    /**
     * Text
     */
    fun getCorporateOrganizationInitial(organizationName: String?): String {
        if (organizationName != null) {
            val roleInitial = organizationName.trim().split(" ")
            return if (roleInitial.size > 1) {
                roleInitial[0][0].toString() + roleInitial[1][0].toString()
            } else {
                roleInitial[0][0].toString()
            }
        }
        return ""
    }

    fun getAccountNumberFormat(accountNumber: String?): String {
        if (accountNumber == null) return Constant.EMPTY
        val accountNumberStringBuilder = StringBuilder()
        accountNumber.forEachIndexed { index, c ->
            if ((index + 1) % 4 == 0) {
                accountNumberStringBuilder.append("$c ")
            } else {
                accountNumberStringBuilder.append(c)
            }
        }
        return accountNumberStringBuilder.toString().trim()
    }

    fun getOTPCode(message: String): String {
        val p = Pattern.compile("(\\d{6})")
        val m = p.matcher(message)
        return if (m.find()) {
            m.group(0)
        } else ""
    }

    fun getStringOrEmpty(string: String?): String {
        return if (string != null && string != "") {
            string
        } else {
            Constant.EMPTY
        }
    }

    fun getStringOrDefaultString(string: String?, default: String?): String? {
        return if (string != null && string != "") {
            string
        } else {
            default
        }
    }

    fun padLeftZeros(inputString: String, length: Int): String {
        if (inputString.length >= length) {
            return inputString
        }
        val stringBuilder = StringBuilder()
        while (stringBuilder.length < (length - inputString.length)) {
            stringBuilder.append("0")
        }
        stringBuilder.append(inputString)

        return stringBuilder.toString()
    }
}
