package com.unionbankph.corporate.payment_link.presentation.setup_payment_link.card_acceptance_option.upload_documents

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseBottomSheetDialog
import com.unionbankph.corporate.common.presentation.viewmodel.GeneralViewModel
import com.unionbankph.corporate.databinding.BottomSheetUploadBirBinding

class CardAcceptanceUploadDocumentFragment :
    BaseBottomSheetDialog<BottomSheetUploadBirBinding, GeneralViewModel>() {

    private var listener: OnUploadBIRDocs? = null

    override fun onViewsBound() {
        super.onViewsBound()

        binding.btnAddDocument.setOnClickListener {
            listener?.openFileManager()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnUploadBIRDocs){
            listener = context
        }
    }

    interface OnUploadBIRDocs{
        fun openFileManager()
    }

    companion object{
        const val TAG = "BottomSheetDialogUploadBIR"

        @JvmStatic
        fun newInstance() =
            CardAcceptanceUploadDocumentFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    override val bindingBinder: (View) -> BottomSheetUploadBirBinding
        get() = BottomSheetUploadBirBinding::bind

    override val layoutId: Int
        get() = R.layout.bottom_sheet_upload_bir

    override val viewModelClassType: Class<GeneralViewModel>
        get() = GeneralViewModel::class.java
}