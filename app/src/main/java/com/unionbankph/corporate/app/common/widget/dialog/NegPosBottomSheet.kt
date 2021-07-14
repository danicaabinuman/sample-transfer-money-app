package com.unionbankph.corporate.app.common.widget.dialog

import android.content.Context
import android.os.Bundle
import android.view.View
import com.jakewharton.rxbinding2.view.RxView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseBottomSheetDialog
import com.unionbankph.corporate.common.presentation.viewmodel.NegPosBottomSheetViewModel
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.bottom_sheet_neg_pos.*
import java.util.concurrent.TimeUnit

class NegPosBottomSheet :
    BaseBottomSheetDialog<NegPosBottomSheetViewModel>(R.layout.bottom_sheet_neg_pos) {

    private var dismissOnActionClicked = true

    private var callbackListener: OnNegativePositiveBottomSheetDialogCallback? = null

    override fun onViewsBound() {
        super.onViewsBound()
        isCancelable = arguments!!.getBoolean(EXTRA_IS_CANCELABLE, true)
        dismissOnActionClicked = arguments!!.getBoolean(EXTRA_DISMISS_ON_ACTION_CLICKED, true)

        initTitle()
        initDescription()
        initNegativeButton()
        initPositiveButton()
    }

    private fun initTitle() {
        val titleArg = arguments?.getString(EXTRA_TITLE)
        if (!titleArg.isNullOrEmpty()) {
            textViewTitle.text = titleArg
        } else {
            textViewTitle.visibility = View.GONE
        }
    }

    private fun initDescription() {
        val descArgs = arguments?.getString(EXTRA_DESCRIPTION)
        if (!descArgs.isNullOrEmpty()) {
            textViewDescription.text = descArgs
        }
    }

    private fun initNegativeButton() {
        val negativeButtonTextArgs = arguments?.getString(EXTRA_NEGATIVE_BUTTON_TEXT)

        if (!negativeButtonTextArgs.isNullOrEmpty()) {
            buttonNegative.apply {
                text = negativeButtonTextArgs

                RxView.clicks(buttonNegative)
                    .throttleFirst(
                        3.toLong(),
                        TimeUnit.SECONDS
                    )
                    .subscribe {
                        callbackListener?.onNegativeButtonClicked()
                        if (dismissOnActionClicked) dismiss()
                    }.addTo(disposables)
            }
        } else {
            buttonNegative.visibility = View.GONE
        }
    }

    private fun initPositiveButton() {
        val positiveButtonTextArgs = arguments?.getString(EXTRA_POSITIVE_BUTTON_TEXT)

        if (!positiveButtonTextArgs.isNullOrEmpty()) {
            buttonPositive.apply {
                text = positiveButtonTextArgs

                RxView.clicks(this)
                    .throttleFirst(
                        3.toLong(),
                        TimeUnit.SECONDS
                    )
                    .subscribe {
                        callbackListener?.onPositiveButtonClicked()
                        if (dismissOnActionClicked) dismiss()
                    }.addTo(disposables)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnNegativePositiveBottomSheetDialogCallback) {
            callbackListener = context
        } else {
            throw RuntimeException("$context must implement OnNegativePositiveBottomSheetDialogCallback")
        }
    }

    override fun onDetach() {
        super.onDetach()
        callbackListener = null
    }

    interface OnNegativePositiveBottomSheetDialogCallback {
        fun onNegativeButtonClicked ()
        fun onPositiveButtonClicked ()
    }

    companion object {

        const val TAG = "NegativePositiveBottomSheetDialog"

        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_DESCRIPTION = "extra_description"
        const val EXTRA_NEGATIVE_BUTTON_TEXT = "extra_negative_button_text"
        const val EXTRA_POSITIVE_BUTTON_TEXT = "extra_positive_button_text"
        const val EXTRA_IS_CANCELABLE = "extra_is_cancelable"
        const val EXTRA_DISMISS_ON_ACTION_CLICKED = "extra_dismiss_on_action_clicked"

        @JvmStatic
        fun newInstance(
            title: String = "",
            description: String? = "",
            negativeButtonText: String = "",
            positiveButtonText: String = "",
            isCancelable: Boolean = true,
            dismissOnActionClicked: Boolean = true
        ) = NegPosBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(EXTRA_TITLE, title)
                    putString(EXTRA_DESCRIPTION, description)
                    putString(EXTRA_NEGATIVE_BUTTON_TEXT, negativeButtonText)
                    putString(EXTRA_POSITIVE_BUTTON_TEXT, positiveButtonText)
                    putBoolean(EXTRA_IS_CANCELABLE, isCancelable)
                    putBoolean(EXTRA_DISMISS_ON_ACTION_CLICKED, dismissOnActionClicked)
                }
            }
    }
}