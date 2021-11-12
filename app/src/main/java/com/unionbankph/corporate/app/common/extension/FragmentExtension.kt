package com.unionbankph.corporate.app.common.extension

import android.os.Handler
import android.os.Looper
import android.text.Spanned
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.material.button.MaterialButton
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.App
import com.unionbankph.corporate.app.base.FragmentViewBindingDelegate

fun Fragment.runPostDelayed(func: (() -> Unit), delayMillisecond: Long) {
    Handler(Looper.getMainLooper()).postDelayed(
        {
            func.invoke()
        }, delayMillisecond
    )
}

fun Fragment.formatString(stringResId: Int, vararg args: Any?): String =
    String.format(getString(stringResId, *args))

fun Fragment.showToast(message: String?, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(activity, message, duration).show()
}

fun Fragment.getHtmlSpannedString(
    @StringRes
    id: Int
): Spanned = getString(id).toHtmlSpan()

fun Fragment.getHtmlSpannedString(
    @StringRes
    id: Int,
    vararg formatArgs: Any?
): Spanned = getString(id, *formatArgs).toHtmlSpan()

fun Fragment.getQuantityHtmlSpannedString(
    @PluralsRes
    id: Int,
    quantity: Int
): Spanned = resources.getQuantityString(id, quantity).toHtmlSpan()

fun Fragment.getQuantityHtmlSpannedString(
    @PluralsRes
    id: Int,
    quantity: Int,
    vararg formatArgs: Any?
): Spanned = resources.getQuantityString(id, quantity, *formatArgs).toHtmlSpan()

fun Fragment.convertColorResourceToHex(color: Int): String =
    "#" + Integer.toHexString(
        ContextCompat.getColor(
            (activity as AppCompatActivity),
            color
        ) and 0xffffff
    )

@ColorInt
fun Fragment.getColorFromAttr(
    @AttrRes attrColor: Int,
    typedValue: TypedValue = TypedValue(),
    resolveRefs: Boolean = true
): Int {
    this.context?.theme?.resolveAttribute(attrColor, typedValue, resolveRefs)
    return typedValue.data
}

fun Fragment.getAccentColor(): Int {
    return if (App.isSME()) {
        R.color.colorAccentSME
    } else {
        R.color.colorAccentPortal
    }
}

fun Fragment.showToolTip(title: String, content: String) {
    val activity = this.activity!!
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

fun <T : ViewBinding> Fragment.viewBinding(viewBindingFactory: (View) -> T) =
    FragmentViewBindingDelegate(this, viewBindingFactory)