package com.unionbankph.corporate.app.common.widget.dialog

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import com.jakewharton.rxbinding2.view.RxView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseBottomSheetDialog
import com.unionbankph.corporate.common.presentation.callback.OnConfirmationPageCallBack
import com.unionbankph.corporate.common.presentation.viewmodel.NegPosBottomSheetViewModel
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.bottom_sheet_confirmation_new.*
import timber.log.Timber
import java.util.concurrent.TimeUnit

class NewConfirmationBottomSheet :
    BaseBottomSheetDialog<NegPosBottomSheetViewModel>(R.layout.bottom_sheet_confirmation_new) {

    private var dismissOnActionClicked = true

    private var callbackListener: OnButtonClickedDialogCallback? = null

    private var mGravity = GRAVITY_START

    override fun onViewsBound() {
        super.onViewsBound()
        isCancelable = arguments!!.getBoolean(EXTRA_IS_CANCELABLE, true)
        dismissOnActionClicked = arguments!!.getBoolean(EXTRA_DISMISS_ON_ACTION_CLICKED, true)

        mGravity = arguments?.getString(EXTRA_GRAVITY, GRAVITY_START)!!

        initIcon()
        initTitle()
        initDescription()
        initNegativeButton()
        initPositiveButton()
    }

    private fun initIcon() {
        val iconResource = arguments?.getInt(EXTRA_ICON)!!
        if (iconResource != -1) {
            imageViewIcon.apply {
                this.visibility = View.VISIBLE
                this.setImageResource(iconResource)
            }
        } else {
            imageViewIcon.visibility = View.GONE
        }
    }

    private fun initTitle() {
        val titleArg = arguments?.getString(EXTRA_TITLE)
        if (!titleArg.isNullOrEmpty()) {
            textViewTitle.apply {
                text = titleArg
                gravity = when (mGravity.equals(GRAVITY_CENTER, true)) {
                    true -> Gravity.CENTER
                    else -> Gravity.START
                }
            }
        } else {
            textViewTitle.visibility = View.GONE
        }
    }

    private fun initDescription() {
        val descArgs = arguments?.getString(EXTRA_DESCRIPTION)
        if (!descArgs.isNullOrEmpty()) {
            textViewDescription.apply {
                text = descArgs
                gravity = when (mGravity.equals(GRAVITY_CENTER, true)) {
                    true -> Gravity.CENTER
                    else -> Gravity.START
                }
            }
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
        } else {
            buttonPositive.visibility = View.GONE
        }
    }

    fun setCallback(onPositiveClick: () -> Unit, onNegativeClick: (() -> Unit)? = null) {
        callbackListener = object : OnButtonClickedDialogCallback {
            override fun onNegativeButtonClicked() { onNegativeClick?.invoke() }
            override fun onPositiveButtonClicked() = onPositiveClick()
        }
    }

    interface OnButtonClickedDialogCallback {
        fun onNegativeButtonClicked ()
        fun onPositiveButtonClicked ()
    }

    companion object {

        const val TAG = "NegativePositiveBottomSheetDialog"
        const val GRAVITY_CENTER = "center"
        const val GRAVITY_START = "start"

        const val EXTRA_ICON = "extra_icon"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_DESCRIPTION = "extra_description"
        const val EXTRA_NEGATIVE_BUTTON_TEXT = "extra_negative_button_text"
        const val EXTRA_POSITIVE_BUTTON_TEXT = "extra_positive_button_text"
        const val EXTRA_GRAVITY = "extra_gravity"
        const val EXTRA_IS_CANCELABLE = "extra_is_cancelable"
        const val EXTRA_DISMISS_ON_ACTION_CLICKED = "extra_dismiss_on_action_clicked"

        @JvmStatic
        fun newInstance(
            iconResource: Int = -1,
            title: String = "",
            description: String? = "",
            negativeButtonText: String = "",
            positiveButtonText: String = "",
            gravity: String = GRAVITY_START,
            isCancelable: Boolean = true,
            dismissOnActionClicked: Boolean = true
        ) = NewConfirmationBottomSheet().apply {
                arguments = Bundle().apply {
                    putInt(EXTRA_ICON, iconResource)
                    putString(EXTRA_TITLE, title)
                    putString(EXTRA_DESCRIPTION, description)
                    putString(EXTRA_NEGATIVE_BUTTON_TEXT, negativeButtonText)
                    putString(EXTRA_POSITIVE_BUTTON_TEXT, positiveButtonText)
                    putString(EXTRA_GRAVITY, gravity)
                    putBoolean(EXTRA_IS_CANCELABLE, isCancelable)
                    putBoolean(EXTRA_DISMISS_ON_ACTION_CLICKED, dismissOnActionClicked)
                }
            }
    }
}