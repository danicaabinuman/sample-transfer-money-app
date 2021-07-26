package com.unionbankph.corporate.app.common.widget.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatDialog
import com.jakewharton.rxbinding2.view.RxView
import com.unionbankph.corporate.R
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.dialog_generic_colored_sme.view.*
import kotlinx.android.synthetic.main.dialog_generic_new_design_sme.view.*
import java.util.concurrent.TimeUnit

class DialogFactory {

    fun createSMEDialog(
        context: Context,
        isNewDesign: Boolean = true,
        iconResource: Int? = null,
        title: String? = "",
        description: String = "",
        positiveButtonText: String = context.getString(R.string.action_ok),
        negativeButtonText: String = "",
        onPositiveButtonClicked: (() -> Unit)? = null,
        onNegativeButtonClicked: (() -> Unit)? = null,
        isCancelable: Boolean = true,
        dismissOnActionClicked: Boolean = true
    ) : AppCompatDialog {

        val disposables = CompositeDisposable()

        val dialogLayout = when (isNewDesign) {
            true -> R.layout.dialog_generic_new_design_sme
            else -> R.layout.dialog_generic_colored_sme
        }

        val dialog = AppCompatDialog(context)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(isCancelable)
        val view = LayoutInflater.from(context).inflate(dialogLayout, null)

        /* Dialog Title */
        when (isNewDesign) {
            true -> view.textViewNewDialogTitle
            else -> view.textViewDialogTitle
        }.apply {
            this.visibility = when (title.isNullOrBlank()) {
                true -> View.GONE
                else -> View.VISIBLE
            }
            this.text = title
        }

        /* Dialog Content/Description */
        when (isNewDesign) {
            true -> view.textViewNewDialogContent
            else -> view.textViewDialogContent
        }.apply {
            this.text = description
        }

        /* Dialog Negative Button */
        when (isNewDesign) {
            true -> view.buttonNewNegative
            else -> view.buttonNegative
        }.apply {
            this.visibility = when (negativeButtonText.isEmpty()) {
                true -> View.GONE
                else -> View.VISIBLE
            }
            this.text = negativeButtonText

            RxView.clicks(this)
                .throttleFirst(
                    3.toLong(),
                    TimeUnit.SECONDS)
                .subscribe {
                    onNegativeButtonClicked?.invoke()
                    if (dismissOnActionClicked) dialog.dismiss()
                }.addTo(disposables)
        }

        /* Dialog Positive Button */
        when (isNewDesign) {
            true -> view.buttonNewPositive
            else -> view.buttonPositive
        }.apply {
            this.text = positiveButtonText

            RxView.clicks(this)
                .throttleFirst(
                    3,
                    TimeUnit.SECONDS)
                .subscribe {
                    onPositiveButtonClicked?.invoke()
                    if (dismissOnActionClicked) dialog.dismiss()
                }.addTo(disposables)
        }

        /* Dialog Icon for non-New Dialog */
        if (!isNewDesign) {
            view.imageViewIcon.apply {
                if (iconResource != null) {
                    this.visibility = View.VISIBLE
                    this.setImageResource(iconResource)
                } else {
                    this.visibility = View.GONE
                }
            }
        }

        dialog.setContentView(view)
        dialog.setOnDismissListener {
            disposables.clear()
            disposables.dispose()
        }

        return dialog
    }

}