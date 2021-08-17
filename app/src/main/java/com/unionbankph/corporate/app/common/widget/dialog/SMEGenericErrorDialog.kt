package com.unionbankph.corporate.app.common.widget.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.unionbankph.corporate.R
import com.unionbankph.corporate.databinding.DialogOopsSomethingWentWrongBinding

class SMEGenericErrorDialog : DialogFragment(){

    private var _binding: DialogOopsSomethingWentWrongBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        _binding = DialogOopsSomethingWentWrongBinding.inflate(LayoutInflater.from(context))


        val dialog = activity?.let {
            Dialog(it)
        }

        if(dialog != null) {
            val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
            val height = (resources.displayMetrics.heightPixels * 0.40).toInt()
            dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.setContentView(binding.root)

            binding.btnOopsSomethingWentWrongClose.setOnClickListener{
                dismiss()
            }
        }
        return dialog!!
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
