package com.unionbankph.corporate.app.common.widget.dialog

import android.os.Bundle
import android.view.View
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseBottomSheetDialog
import com.unionbankph.corporate.app.common.extension.setContextCompatBackground
import com.unionbankph.corporate.app.common.extension.toHtmlSpan
import com.unionbankph.corporate.common.presentation.callback.OnConfirmationPageCallBack
import com.unionbankph.corporate.settings.presentation.SettingsViewModel
import kotlinx.android.synthetic.main.bottom_sheet_confirmation.*

class ConfirmationBottomSheet : BaseBottomSheetDialog<SettingsViewModel>(R.layout.bottom_sheet_confirmation) {

    private var callback: OnConfirmationPageCallBack? = null

    override fun onViewsBound() {
        super.onViewsBound()
        init()
    }

    private fun init() {
        arguments?.getInt(EXTRA_ICON)?.let { imageViewIcon.setImageResource(it) }
        arguments?.getInt(EXTRA_DRAWABLE_CIRCLE)?.let {
            viewCicleIcon.setContextCompatBackground(it)
        }
        if (arguments?.getString(EXTRA_TITLE) == null) {
            textViewTitle.visibility = View.GONE
        } else {
            textViewTitle.text = arguments?.getString(EXTRA_TITLE)
        }
        if (arguments?.getString(EXTRA_DESC) == null) {
            textViewDescription.visibility = View.GONE
        } else {
            textViewDescription.text = arguments?.getString(EXTRA_DESC)?.toHtmlSpan()
        }
        if (arguments?.getString(EXTRA_ACTION_POSITIVE) == null) {
            buttonPositive.visibility = View.GONE
        } else {
            buttonPositive.visibility = View.VISIBLE
            buttonPositive.text = arguments?.getString(EXTRA_ACTION_POSITIVE)
        }
        if (arguments?.getString(EXTRA_ACTION_NEGATIVE) == null) {
            buttonNegative.visibility = View.GONE
        } else {
            buttonNegative.visibility = View.VISIBLE
            buttonNegative.text = arguments?.getString(EXTRA_ACTION_NEGATIVE)
        }
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        buttonNegative.setOnClickListener {
            callback?.onClickNegativeButtonDialog(arguments?.getString(EXTRA_DATA), this.tag)
        }
        buttonPositive.setOnClickListener {
            callback?.onClickPositiveButtonDialog(arguments?.getString(EXTRA_DATA), this.tag)
        }
        linearLayoutDynamicFields.visibility = View.VISIBLE
        callback?.onDataSetDialog(
            this.tag,
            linearLayoutDynamicFields,
            buttonPositive,
            buttonNegative,
            arguments?.getString(EXTRA_DATA),
            arguments?.getString(EXTRA_DYNAMIC_DATA)
        )
    }

    fun setOnConfirmationPageCallBack(onConfirmationPageCallBack: OnConfirmationPageCallBack) {
        this.callback = onConfirmationPageCallBack
    }

    companion object {

        const val EXTRA_ICON = "icon"
        const val EXTRA_TITLE = "title"
        const val EXTRA_DESC = "desc"
        const val EXTRA_ACTION_POSITIVE = "action_positive"
        const val EXTRA_ACTION_NEGATIVE = "action_negative"
        const val EXTRA_DATA = "data"
        const val EXTRA_DYNAMIC_DATA = "dynamic_data"
        const val EXTRA_DRAWABLE_CIRCLE = "drawable_circle"

        fun newInstance(
            icon: Int,
            title: String,
            desc: String? = null,
            actionPositive: String? = null,
            actionNegative: String? = null,
            data: String? = null,
            dynamicData: String? = null,
            drawableCircle: Int = R.drawable.circle_solid_orange
        ): ConfirmationBottomSheet {
            val fragment =
                ConfirmationBottomSheet()
            val bundle = Bundle()
            bundle.putInt(EXTRA_ICON, icon)
            bundle.putInt(EXTRA_DRAWABLE_CIRCLE, drawableCircle)
            bundle.putString(EXTRA_TITLE, title)
            bundle.putString(EXTRA_DESC, desc)
            bundle.putString(EXTRA_ACTION_POSITIVE, actionPositive)
            bundle.putString(EXTRA_ACTION_NEGATIVE, actionNegative)
            bundle.putString(EXTRA_DATA, data)
            bundle.putString(EXTRA_DYNAMIC_DATA, dynamicData)
            fragment.arguments = bundle
            return fragment
        }
    }
}
