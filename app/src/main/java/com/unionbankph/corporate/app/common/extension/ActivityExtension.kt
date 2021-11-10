package com.unionbankph.corporate.app.common.extension

import android.content.Intent
import android.graphics.Bitmap
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.text.Spanned
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.material.button.MaterialButton
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.App
import com.unionbankph.corporate.app.common.widget.dialog.ConfirmationBottomSheet
import com.unionbankph.corporate.app.util.BitmapUtil
import com.unionbankph.corporate.common.presentation.callback.OnConfirmationPageCallBack


fun AppCompatActivity.runPostDelayed(func: (() -> Unit), delayMillisecond: Long) {
    Handler(Looper.getMainLooper()).postDelayed(
        {
            func.invoke()
        }, delayMillisecond
    )
}

fun AppCompatActivity.startShareMediaActivity(
    bitmap: Bitmap
) {
    val bitmapUtil = BitmapUtil(this)
    val file = bitmapUtil.saveBitmap(
        bitmap,
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)?.absolutePath,
        "Image_${System.currentTimeMillis()}"
    )
    val uri = file?.let {
        FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", it)
    }
    val shareIntent = Intent(Intent.ACTION_SEND)
    shareIntent.type = "image/*"
    shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
    startActivity(
        Intent.createChooser(
            shareIntent,
            formatString(R.string.title_share_transaction_details)
        )
    )
}

fun AppCompatActivity.formatString(stringResId: Int, vararg args: Any?): String =
    String.format(getString(stringResId, *args))

fun AppCompatActivity.convertColorResourceToHex(color: Int): String =
    "#" + Integer.toHexString(ContextCompat.getColor(this, color) and 0xffffff)

fun AppCompatActivity.showToast(message: String?, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun AppCompatActivity.getHtmlSpannedString(
    @StringRes
    id: Int
): Spanned = getString(id).toHtmlSpan()

fun AppCompatActivity.getHtmlSpannedString(
    @StringRes
    id: Int,
    vararg formatArgs: Any?
): Spanned = getString(id, *formatArgs).toHtmlSpan()

fun AppCompatActivity.getQuantityHtmlSpannedString(
    @PluralsRes
    id: Int,
    quantity: Int
): Spanned = resources.getQuantityString(id, quantity).toHtmlSpan()

fun AppCompatActivity.getQuantityHtmlSpannedString(
    @PluralsRes
    id: Int,
    quantity: Int,
    vararg formatArgs: Any?
): Spanned = resources.getQuantityString(id, quantity, *formatArgs).toHtmlSpan()

@ColorInt
fun AppCompatActivity.getColorFromAttr(
    @AttrRes attrColor: Int,
    typedValue: TypedValue = TypedValue(),
    resolveRefs: Boolean = true
): Int {
    this.theme.resolveAttribute(attrColor, typedValue, resolveRefs)
    return typedValue.data
}

fun AppCompatActivity.getAccentColor(): Int {
    return if (App.isSME()) {
        R.color.colorAccentSME
    } else {
        R.color.colorAccentPortal
    }
}

fun AppCompatActivity.showErrorBottomSheet(
    title: String,
    message: String,
    actionPositive: String? = null,
    actionNegative: String? = null
) {
    val errorBottomSheet = ConfirmationBottomSheet.newInstance(
        R.drawable.ic_warning_white,
        title,
        message,
        actionPositive,
        actionNegative
    )
    errorBottomSheet.setOnConfirmationPageCallBack(object : OnConfirmationPageCallBack {
        override fun onClickPositiveButtonDialog(data: String?, tag: String?) {
            errorBottomSheet.dismiss()
        }

        override fun onClickNegativeButtonDialog(data: String?, tag: String?) {
            errorBottomSheet.dismiss()
        }
    })
    errorBottomSheet.show(
        this.supportFragmentManager,
        "ErrorBottomSheet"
    )
}

fun AppCompatActivity.showToolTip(title: String, content: String) {
    val activity = this
    val toolTipDialog = MaterialDialog(activity).apply {
        lifecycleOwner(activity)
        customView(R.layout.dialog_tool_tip)
    }
    val buttonClose =
        toolTipDialog.view.findViewById<AppCompatButton>(R.id.buttonClose)
    val textViewTitle =
        toolTipDialog.view.findViewById<TextView>(R.id.textViewTitle)
    val textViewContent =
        toolTipDialog.view.findViewById<TextView>(R.id.textViewContent)
    textViewTitle.text = title
    textViewContent.text = content
    buttonClose.setOnClickListener { toolTipDialog.dismiss() }
    toolTipDialog.window?.attributes?.windowAnimations =
        R.style.SlideUpAnimation
    toolTipDialog.window?.setGravity(Gravity.CENTER)
    toolTipDialog.show()
}