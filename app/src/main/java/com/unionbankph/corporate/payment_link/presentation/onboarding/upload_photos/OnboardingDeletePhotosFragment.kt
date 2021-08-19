package com.unionbankph.corporate.payment_link.presentation.onboarding.upload_photos

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.unionbankph.corporate.R
import kotlinx.android.synthetic.main.bottom_sheet_onboarding_delete_photo.*

class OnboardingDeletePhotosFragment : BottomSheetDialogFragment() {

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

        btn_delete.setOnClickListener {
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
}