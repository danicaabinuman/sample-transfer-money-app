package com.unionbankph.corporate.app.common.widget.dialog

import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseBottomSheetDialog
import com.unionbankph.corporate.app.common.extension.convertColorResourceToHex
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.getAccentColor
import com.unionbankph.corporate.app.common.extension.toHtmlSpan
import com.unionbankph.corporate.settings.presentation.SettingsViewModel
import kotlinx.android.synthetic.main.bottom_sheet_session_time_out.*

class SessionTimeOutBottomSheet : BaseBottomSheetDialog<SettingsViewModel>(R.layout.bottom_sheet_session_time_out) {

    private lateinit var onBottomSheetSessionTimeOutListener: OnBottomSheetSessionTimeOutListener

    override fun onViewsBound() {
        super.onViewsBound()
        isCancelable = false
        setSessionTimerDesc(arguments?.getLong(EXTRA_INITIAL_SECOND))
        buttonSessionTimeOut.setOnClickListener {
            onBottomSheetSessionTimeOutListener.onClickBottomSheetAction()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onBottomSheetSessionTimeOutListener.onDismissSessionTimeOutDialog()
    }

    fun setOnSessionTimeOutListener(
        onBottomSheetSessionTimeOutListener: OnBottomSheetSessionTimeOutListener
    ) {
        this.onBottomSheetSessionTimeOutListener = onBottomSheetSessionTimeOutListener
    }

    fun setSessionTimerDesc(timePeriod: Long?) {
        activity?.runOnUiThread {
            textViewSessionTimeOutDesc?.text = formatString(
                R.string.params_session_time_out,
                formatString(
                    R.string.param_color,
                    convertColorResourceToHex(getAccentColor()),
                    "$timePeriod seconds"
                )
            ).toHtmlSpan()
        }
    }

    override fun show(manager: FragmentManager, tag: String?) {
        try {
            val ft = manager.beginTransaction()
            ft.add(this, tag)
            ft.commitAllowingStateLoss()
        } catch (ignored: IllegalStateException) {
        }
    }

    interface OnBottomSheetSessionTimeOutListener {
        fun onClickBottomSheetAction()
        fun onDismissSessionTimeOutDialog()
    }

    companion object {

        const val EXTRA_INITIAL_SECOND = "initial_second"

        fun newInstance(initialSecond: Long): SessionTimeOutBottomSheet {
            val fragment =
                SessionTimeOutBottomSheet()
            val bundle = Bundle()
            bundle.putLong(EXTRA_INITIAL_SECOND, initialSecond)
            fragment.arguments = bundle
            return fragment
        }
    }
}
