package com.unionbankph.corporate.app.common.widget.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.unionbankph.corporate.R

class ProgressBarDialog : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.BaseDialogTheme)
        dialog?.window?.attributes?.windowAnimations = R.style.FadeInFadeOutAnimation
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_progress_bar, container)
    }

    companion object {

        fun newInstance(): ProgressBarDialog {
            val frag = ProgressBarDialog()
            val args = Bundle()
            frag.arguments = args
            return frag
        }
    }
}
