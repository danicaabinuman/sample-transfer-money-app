package com.unionbankph.corporate.payment_link.presentation.setup_payment_link.card_acceptance_option.upload_documents

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.unionbankph.corporate.R
import kotlinx.android.synthetic.main.bottom_sheet_upload_bir.*

class CardAcceptanceUploadDocumentFragment : BottomSheetDialogFragment() {

    private var listener: OnUploadBIRDocs? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_upload_bir, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        btnTakeBIRPhoto.setOnClickListener {
            listener?.openCamera()
        }

        btnAddBIRPhoto.setOnClickListener {
            listener?.openGallery()
        }

        btnAddDocument.setOnClickListener {
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
        fun openCamera()
        fun openGallery()
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

}