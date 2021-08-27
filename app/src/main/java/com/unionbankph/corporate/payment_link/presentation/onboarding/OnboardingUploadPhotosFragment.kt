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
import com.unionbankph.corporate.app.base.BaseBottomSheetDialog
import com.unionbankph.corporate.common.presentation.viewmodel.GeneralViewModel
import com.unionbankph.corporate.databinding.BottomSheetUploadBirBinding
import com.unionbankph.corporate.databinding.BottomSheetUploadPhotosBinding

class OnboardingUploadPhotosFragment :
    BaseBottomSheetDialog<BottomSheetUploadPhotosBinding, GeneralViewModel>() {

    private var listener : OnOnboardingUploadPhotosFragmentInteraction? = null

    override fun onViewsBound() {
        super.onViewsBound()
        binding.btnGallery.setOnClickListener {
            listener?.openGallery()
        }

        binding.btnCamera.setOnClickListener {
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

    override val viewModelClassType: Class<GeneralViewModel>
        get() = GeneralViewModel::class.java
}