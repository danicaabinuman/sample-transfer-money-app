package com.unionbankph.corporate.payment_link.presentation.onboarding

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.unionbankph.corporate.R
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.payment_link_channels.FeesAndChargesFragment
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
            listener?.openGallery()
        }

        btnCamera.setOnClickListener {
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

//    private fun openGalleryForImages() {
//        if (Build.VERSION.SDK_INT < 19) {
//            var intent = Intent()
//            intent.type = "image/*"
//            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
//            intent.action = Intent.ACTION_GET_CONTENT
//            startActivityForResult(Intent.createChooser(intent, "Choose pictures"), REQUEST_CODE)
//        } else {
//            var intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
//            intent.addCategory(Intent.CATEGORY_OPENABLE)
//            intent.type = "image/*"
//            startActivityForResult(intent, REQUEST_CODE)
//        }
//    }

    interface OnOnboardingUploadPhotosFragmentInteraction {
        fun openGallery()
        fun openCamera()
    }

    companion object{
        const val TAG = "BottomSheetDialogUploadPhotos"

//        fun newInstance() : OnboardingUploadPhotosFragment

        @JvmStatic
        fun newInstance() =
            OnboardingUploadPhotosFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}