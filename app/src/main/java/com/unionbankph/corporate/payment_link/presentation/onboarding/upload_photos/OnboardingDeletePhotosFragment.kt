package com.unionbankph.corporate.payment_link.presentation.onboarding.upload_photos

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseBottomSheetDialog
import com.unionbankph.corporate.common.presentation.viewmodel.NegPosBottomSheetViewModel
import com.unionbankph.corporate.databinding.BottomSheetOnboardingDeletePhotoBinding

class OnboardingDeletePhotosFragment :
    BaseBottomSheetDialog<BottomSheetOnboardingDeletePhotoBinding, NegPosBottomSheetViewModel>() {

    private var listener : OnboardingDeletePhotosInteraction? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_onboarding_delete_photo, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.btnDelete.setOnClickListener {
            this.dismiss()
            listener?.deletePhoto()
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnboardingDeletePhotosInteraction) {
            listener = context
        }
    }

    interface OnboardingDeletePhotosInteraction{
        fun deletePhoto()
    }

    companion object{
        const val TAG = "DeletePhotosFragment"

        @JvmStatic
        fun newInstance() =
            OnboardingDeletePhotosFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    override val bindingBinder: (View) -> BottomSheetOnboardingDeletePhotoBinding
        get() = BottomSheetOnboardingDeletePhotoBinding::bind
    override val layoutId: Int
        get() = R.layout.bottom_sheet_onboarding_delete_photo
    override val viewModelClassType: Class<NegPosBottomSheetViewModel>
        get() = NegPosBottomSheetViewModel::class.java
}