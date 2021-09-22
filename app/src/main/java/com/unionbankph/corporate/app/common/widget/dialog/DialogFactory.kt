package com.unionbankph.corporate.app.common.widget.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatDialog
import com.jakewharton.rxbinding2.view.RxView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.databinding.DialogGenericColoredSmeBinding
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit

class DialogFactory {

    fun createSMEDialog(
        context: Context,
        isNewDesign: Boolean = true, // No Config for New yet
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

        val dialog = AppCompatDialog(context)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(isCancelable)

        val view = DialogGenericColoredSmeBinding.inflate(LayoutInflater.from(context))

        /* Dialog Title */
        view.textViewDialogTitle.apply {
            this.visibility = when (title.isNullOrBlank()) {
                true -> View.GONE
                else -> View.VISIBLE
            }
            this.text = title
        }

        /* Dialog Content/Description */
        view.textViewDialogContent.apply {
            this.text = description
        }

        /* Dialog Negative Button */
        view.buttonNegative.apply {
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
        view.buttonPositive.apply {
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
        view.imageViewIcon.apply {
            if (iconResource != null) {
                this.visibility = View.VISIBLE
                this.setImageResource(iconResource)
            } else {
                this.visibility = View.GONE
            }
        }

        dialog.setContentView(view.root)
        dialog.setOnDismissListener {
            disposables.clear()
            disposables.dispose()
        }
//        view.imageViewIconPlain.apply {
//            if (iconResource != null) {
//                this.visibility = View.VISIBLE
//                this.setImageResource(iconResource)
//            } else {
//                this.visibility = View.GONE
//            }
//        }
//
//        view.textViewDialogTitlePlain.apply {
//            this.visibility = when (title.isNullOrBlank()) {
//                true -> View.GONE
//                else -> View.VISIBLE
//            }
//            this.text = title
//        }
//
//        view.textViewDialogContentPlain.apply {
//            this.text = content
//        }
//
//        view.buttonPositivePlain.apply {
//            this.text = positiveButtonText
//            this.setOnClickListener {
//                if (dismissOnActionClicked) dialog.dismiss()
//                onPositiveButtonClicked?.invoke()
//            }
//        }
//
//        view.buttonNegativePlain.apply {
//            this.visibility = when (negativeButtonText.isEmpty()) {
//                true -> View.GONE
//                else -> View.VISIBLE
//            }
//            this.text = negativeButtonText
//            this.setOnClickListener {
//                if (dismissOnActionClicked) dialog.dismiss()
//                onNegativeButtonClicked?.invoke()
//            }
//        }

        return dialog
    }

}