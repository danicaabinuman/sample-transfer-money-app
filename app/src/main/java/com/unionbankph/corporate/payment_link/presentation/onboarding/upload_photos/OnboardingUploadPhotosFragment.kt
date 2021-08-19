package com.unionbankph.corporate.payment_link.presentation.onboarding.upload_photos

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.unionbankph.corporate.R
import kotlinx.android.synthetic.main.bottom_sheet_upload_photos.*

class OnboardingUploadPhotosFragment : BottomSheetDialogFragment() {

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

        btnGallery.setOnClickListener {
            dismiss()
            listener?.openGallery()
        }

        btnCamera.setOnClickListener {
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
}