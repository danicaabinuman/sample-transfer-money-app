package com.unionbankph.corporate.payment_link.presentation.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.unionbankph.corporate.R

class OnboardingDeletePhotosFragment : Fragment() {

    private var listener : OnboardingDeletePhotosInteraction? = null
    private var fullscreenImage : String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_onboarding_delete_photos, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        fullscreenImage = arguments?.getString(FULLSCREEN_IMAGE)
    }

    interface OnboardingDeletePhotosInteraction{
        fun deletePhoto()
    }

    companion object{
        const val TAG = "DeletePhotosFragment"
        const val FULLSCREEN_IMAGE = "Image"

        @JvmStatic
        fun newInstance() =
            OnboardingDeletePhotosFragment().apply {
                arguments = Bundle().apply {

                }
            }

    }
}