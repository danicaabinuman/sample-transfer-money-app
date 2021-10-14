package com.unionbankph.corporate.payment_link.presentation.setup_payment_link.card_acceptance_option.upload_documents

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseBottomSheetDialog
import com.unionbankph.corporate.common.presentation.viewmodel.NegPosBottomSheetViewModel
import com.unionbankph.corporate.databinding.BottomSheetUploadBirBinding

class CardAcceptanceUploadDocumentFragment : BaseBottomSheetDialog<BottomSheetUploadBirBinding, NegPosBottomSheetViewModel>() {

    private var listener: OnUploadDocs? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_upload_bir, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.btnTakeBIRPhoto.setOnClickListener {
            dismiss()
            listener?.openCamera()
        }

        binding.btnAddBIRPhoto.setOnClickListener {
            listener?.openGallery()
        }

        binding.btnAddDocument.setOnClickListener {
            listener?.openFileManager()
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnUploadDocs){
            listener = context
        }
    }

    interface OnUploadDocs{
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

    override val bindingBinder: (View) -> BottomSheetUploadBirBinding
        get() = BottomSheetUploadBirBinding::bind
    override val layoutId: Int
        get() = R.layout.bottom_sheet_upload_bir
    override val viewModelClassType: Class<NegPosBottomSheetViewModel>
        get() = NegPosBottomSheetViewModel::class.java

}