package com.unionbankph.corporate.payment_link.presentation.onboarding.upload_photos

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseBottomSheetDialog
import com.unionbankph.corporate.common.presentation.viewmodel.NegPosBottomSheetViewModel
import com.unionbankph.corporate.databinding.BottomSheetUploadPhotosBinding

class OnboardingUploadPhotosFragment : BaseBottomSheetDialog<BottomSheetUploadPhotosBinding, NegPosBottomSheetViewModel>() {

    private var listener : OnOnboardingUploadPhotosFragmentInteraction? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_upload_photos, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.btnGallery.setOnClickListener {
            dismiss()
            listener?.openGallery()
        }

        binding.btnCamera.setOnClickListener {
            dismiss()
            listener?.openCamera()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is OnOnboardingUploadPhotosFragmentInteraction) {
            listener = context
        } else {

        }
    }

    interface OnOnboardingUploadPhotosFragmentInteraction {
        fun openGallery()
        fun openCamera()
    }

    companion object{
        const val TAG = "BottomSheetDialogUploadPhotos"

        @JvmStatic
        fun newInstance() =
            OnboardingUploadPhotosFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    override val bindingBinder: (View) -> BottomSheetUploadPhotosBinding
        get() = BottomSheetUploadPhotosBinding::bind
    override val layoutId: Int
        get() = R.layout.bottom_sheet_upload_photos
    override val viewModelClassType: Class<NegPosBottomSheetViewModel>
        get() = NegPosBottomSheetViewModel::class.java
}