package com.unionbankph.corporate.app.common.extension

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.AbsoluteSizeSpan
import android.text.style.ClickableSpan
import android.util.TypedValue
import android.view.View
import android.widget.*
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.ObjectKey
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.App
import com.unionbankph.corporate.app.common.platform.glide.GlideApp
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.constant.EditTextStyleEnum
import java.util.*

fun TextView.makeLinks(vararg links: Pair<String, View.OnClickListener>) {
    val spannableString = SpannableString(this.text)
    for (link in links) {
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                Selection.setSelection((view as TextView).text as Spannable, 0)
                view.invalidate()
                link.second.onClick(view)
            }
        }
        val startIndexOfLink = this.text.toString().indexOf(link.first)
        spannableString.setSpan(
            clickableSpan, startIndexOfLink, startIndexOfLink + link.first.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    this.movementMethod =
        LinkMovementMethod.getInstance() // without LinkMovementMethod, link can not click
    this.setText(spannableString, TextView.BufferType.SPANNABLE)
}

fun String.setSectionOfTextSize(
    textToChangeSize: String,
    size: Int
): SpannableStringBuilder {
    val builder = SpannableStringBuilder()
    if (textToChangeSize.isNotEmpty() && textToChangeSize.trim { it <= ' ' } != "") {

        //for counting start/end indexes
        val testText = this.toLowerCase(Locale.US)
        val testTextToBold = textToChangeSize.toLowerCase(Locale.US)
        val startingIndex = testText.indexOf(testTextToBold)
        val endingIndex = startingIndex + testTextToBold.length
        //for counting start/end indexes
        if (startingIndex < 0 || endingIndex < 0) {
            return builder.append(this)
        } else if (startingIndex >= 0 && endingIndex >= 0) {
            builder.append(this)
            builder.setSpan(
                AbsoluteSizeSpan(size, true),
                startingIndex,
                endingIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    } else {
        return builder.append(this)
    }
    return builder
}

fun EditText.addSuffix(suffix: String) {
    val editText = this
    val formattedSuffix = " $suffix"
    var text = ""
    var isSuffixModified = false

    val setCursorPosition: () -> Unit =
        { Selection.setSelection(editableText, editableText.length - formattedSuffix.length) }

    val setEditText: () -> Unit = {
        editText.setText(text)
        setCursorPosition()
    }

    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            val newText = editable.toString()

            if (isSuffixModified) {
                // user tried to modify suffix
                isSuffixModified = false
                setEditText()
            } else if (text.isNotEmpty() && newText.length < text.length && !newText.contains(
                    formattedSuffix
                )
            ) {
                // user tried to delete suffix
                setEditText()
            } else if (!newText.contains(formattedSuffix)) {
                // new input, add suffix
                text = "$newText$formattedSuffix"
                setEditText()
            } else {
                text = newText
            }
        }

        override fun beforeTextChanged(
            charSequence: CharSequence?,
            start: Int,
            count: Int,
            after: Int
        ) {
            charSequence?.let {
                val textLengthWithoutSuffix = it.length - formattedSuffix.length
                if (it.isNotEmpty() && start > textLengthWithoutSuffix) {
                    isSuffixModified = true
                }
            }
        }

        override fun onTextChanged(
            charSequence: CharSequence?,
            start: Int,
            before: Int,
            count: Int
        ) {
        }

    })
}

fun View.setEnableView(isEnable: Boolean) {
    if (isEnable) {
        this.isEnabled = true
        this.isClickable = true
        this.alpha = 1f
    } else {
        this.isEnabled = false
        this.isClickable = false
        this.alpha = 0.5f
    }
}

fun EditText.setEnableView(isEnable: Boolean) {
    val textInputLayout = getTextInputLayout(this)
    if (isEnable) {
        this.isEnabled = true
        this.isClickable = true
        textInputLayout?.setContextCompatBackground(R.color.colorTransparent)
    } else {
        this.isEnabled = false
        this.isClickable = false
        textInputLayout?.setContextCompatBackground(R.drawable.bg_edit_text_disabled)
    }
}

fun EditText.setEnableDropdownFields(isEnable: Boolean) {
    val textInputLayout = getTextInputLayout(this)
    if (isEnable) {
        this.isEnabled = true
        this.isClickable = true
        this.setContextCompatBackground(R.color.colorTransparent)
        textInputLayout?.alpha = 1f
        this.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_vector_drop_down_line, 0)
    } else {
        this.isEnabled = false
        this.isClickable = false
        this.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_vector_drop_down_line_disabled, 0)
        textInputLayout?.alpha = 0.5f
    }
}

fun EditText.setEnableAmount(isEnable: Boolean) {
    val textInputLayout = getTextInputLayout(this)
    if (isEnable) {
        this.isEnabled = true
        this.isClickable = true
        textInputLayout?.setContextCompatBackground(R.color.colorTransparent)
    } else {
        this.isEnabled = false
        this.isClickable = false
        textInputLayout?.setContextCompatBackground(R.drawable.bg_edit_text_amount_disabled)
    }
}

fun TextInputEditText.setStyle(editTextStyleEnum: EditTextStyleEnum) {
    this.isSingleLine = true
    this.tag = Constant.REFRESH
    when (editTextStyleEnum) {
        EditTextStyleEnum.FREE_TEXT -> {
            this.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            this.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            this.isCursorVisible = true
            this.isFocusable = true
            this.isFocusableInTouchMode = true
        }
        EditTextStyleEnum.DATE_PICKER -> {
            this.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.ic_fund_transfer_calender_orange,
                0
            )
            this.inputType = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or
                    InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                    InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            this.isCursorVisible = false
            this.isFocusable = false
            this.isFocusableInTouchMode = false
        }
        EditTextStyleEnum.DROP_DOWN -> {
            this.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.ic_arrow_light_orange_down,
                0
            )
            this.inputType = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or
                    InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                    InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            this.isCursorVisible = false
            this.isFocusable = false
            this.isFocusableInTouchMode = false
        }
    }
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

fun TextView.enableButton(isEnabled: Boolean) {
    this.isEnabled = isEnabled
    if (isEnabled) {
        this.alpha = 1.0f
        this.setContextCompatTextColor(R.color.colorWhite)
    } else {
        this.alpha = 0.5f
        this.setContextCompatTextColor(R.color.colorWhite50)
    }
}

fun TextView.enableButtonMSME(isEnabled: Boolean) {
    this.isEnabled = isEnabled
    if (isEnabled) {
        this.alpha = 1.0f
        this.setContextCompatTextColor(R.color.colorWhite)
    } else {
        this.alpha = 0.5f
        this.setContextCompatTextColor(R.color.dsColorPrimaryButtonTextDisabled)
    }
}

fun TextView.loginEnableButton(isEnabled: Boolean) {
    this.isEnabled = isEnabled
    if (isEnabled) {
        this.setBackgroundResource(R.drawable.bg_gradient_orange)
        this.setContextCompatTextColor(R.color.colorWhite)
    } else {
        this.setBackgroundResource(R.drawable.bg_gradient_orange_disabled)
        this.setContextCompatTextColor(R.color.colorWhite50)
    }
}

fun EditText.refresh() {
    if (this.length() != 0) {
        this.setText(this.text.toString())
    } else {
        this.setText(Constant.EMPTY)
        this.tag = Constant.CLEAR
        this.text?.clear()
    }
}

fun EditText.refreshClear() {
    if (this.length() != 0) {
        this.setText(this.text.toString())
    } else {
        this.tag = Constant.REFRESH
        this.text?.clear()
    }
}

fun EditText.clear() {
    this.tag = Constant.REFRESH
    this.text?.clear()
}

fun EditText.getTextNullable(): String? {
    return if (this.length() > 0 && this.text.toString() == Constant.EMPTY) {
        null
    } else {
        this.text.toString()
    }
}

fun EditText.getEditTextNullable(): String? {
    return if (this.length() == 0) {
        null
    } else {
        this.text.toString()
    }
}

fun EditText.setTextNullable(value: String) {
    if (value.isNotEmpty()) {
        this.setText(value)
    }
}

fun View.visibility(isShow: Boolean) {
    this.visibility = if (isShow) View.VISIBLE else View.GONE
}

fun View.setVisible(isShow: Boolean) {
    this.visibility = if (isShow) View.VISIBLE else View.INVISIBLE
}

fun TextView.setContextCompatTextColor(colorResId: Int) {
    this.setTextColor(ContextCompat.getColor(this.context, colorResId))
}

fun View.setContextCompatBackgroundColor(colorResId: Int) {
    this.setBackgroundColor(ContextCompat.getColor(this.context, colorResId))
}

fun View.setContextCompatBackground(drawableResId: Int?) {
    if (drawableResId == null) {
        this.background = null
    } else {
        this.background = ContextCompat.getDrawable(this.context, drawableResId)
    }
}

fun Drawable.setColor(context: Context, color: Int) {
    this.setColorFilter(ContextCompat.getColor(context, color), PorterDuff.Mode.SRC_ATOP)
}

fun ImageView.setColor(color: Int) {
    this.setColorFilter(
        ContextCompat.getColor(this.context, color),
        PorterDuff.Mode.SRC_ATOP
    )
}

fun ImageView.setThemeToDark(isDark: Boolean) {
    if (isDark) {
        this.setColorFilter(
            ContextCompat.getColor(this.context, R.color.colorInfo),
            PorterDuff.Mode.SRC_ATOP
        )
    } else {
        this.setColorFilter(
            ContextCompat.getColor(this.context, R.color.colorWhite),
            PorterDuff.Mode.SRC_ATOP
        )
    }
}

fun ImageView.clearTheme() {
    this.setColorFilter(
        ContextCompat.getColor(this.context, R.color.colorTransparent),
        PorterDuff.Mode.SRC_ATOP
    )
}

fun ImageView.loadImageByPath(filePath: String) {
    this.setImageBitmap(BitmapFactory.decodeFile(filePath))
}

fun ImageView.loaderImageByUrl(
    filePath: String,
    progressBar: ProgressBar,
    signature: String = "1"
) {
    progressBar.visibility(true)
    GlideApp.with(this)
        .setDefaultRequestOptions(RequestOptions().apply {
            error(R.drawable.image_view_thumbnail)
        })
        .load(filePath)
        .listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                progressBar.visibility(false)
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                progressBar.visibility(false)
                return false
            }
        })
        .signature(ObjectKey(signature))
        .fitCenter()
        .into(this)
}

fun ImageView.loaderImagePreviewByUrl(filePath: String?, view: View, signature: String = "1") {
    val imageViewProgressBar = view.findViewById<ProgressBar>(R.id.imageViewProgressBar)
    val viewOverLay = view.findViewById<View>(R.id.viewOverLay)
    viewOverLay.visibility(false)
    GlideApp.with(this)
        .setDefaultRequestOptions(RequestOptions().apply {
            error(R.drawable.image_view_thumbnail)
        })
        .load(filePath)
        .listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                viewOverLay.visibility(false)
                imageViewProgressBar.visibility(false)
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                viewOverLay.visibility(true)
                imageViewProgressBar.visibility(false)
                return false
            }
        })
        .signature(ObjectKey(signature))
        .fitCenter()
        .into(this)
}

fun ImageView.loadImage(image: Any?, signature: String = "1") = GlideApp.with(this)
    .asBitmap()
    .load(image)
    .signature(ObjectKey(signature))
    .fitCenter()
    .into(this)


@ColorInt
fun Context.getColorFromAttr(
    @AttrRes attrColor: Int,
    typedValue: TypedValue = TypedValue(),
    resolveRefs: Boolean = true
): Int {
    theme.resolveAttribute(attrColor, typedValue, resolveRefs)
    return typedValue.data
}

fun Context.getAccentColor(): Int {
    return if (App.isSME()) {
        R.color.colorAccentSME
    } else {
        R.color.colorAccentPortal
    }
}

fun Int.convertToDP(context: Context) : Int {
    val scale = context.resources.displayMetrics.density
    return (this * scale + 0.5f).toInt()
}

fun CheckBox.setMSMETheme() {
    this.buttonTintList = ColorStateList(
        arrayOf(
            intArrayOf(-android.R.attr.state_checked), // unchecked
            intArrayOf(android.R.attr.state_checked) // checked
        ),
        intArrayOf(
            Color.parseColor("#BFBFBF"),  //unchecked color
            Color.parseColor("#FF8200")
        )
    )
}
