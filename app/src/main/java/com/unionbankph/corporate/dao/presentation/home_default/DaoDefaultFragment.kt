package com.unionbankph.corporate.dao.presentation.home_default

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.room.Dao
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.auth.presentation.policy.PrivacyPolicyActivity
import com.unionbankph.corporate.dao.presentation.DaoActivity
import com.unionbankph.corporate.databinding.FragmentDaoDefaultBinding

class DaoDefaultFragment :
    BaseFragment<FragmentDaoDefaultBinding, DaoDefaultViewModel>() {

    override fun beforeLayout(savedInstanceState: Bundle?) {
        super.beforeLayout(savedInstanceState)
        if (arguments?.getString(EXTRA_SCREEN) == SCREEN_REFERENCE_NUMBER) {
            navigateEnterReferenceNumber()
        } else if (arguments?.getString(EXTRA_SCREEN) == SCREEN_PERSONAL_INFORMATION) {
            navigatePersonalInformation()
        }
    }

    private fun navigatePersonalInformation() {
        findNavController().navigate(R.id.action_personal_information_step_one)
    }

    private fun navigateEnterReferenceNumber() {
        findNavController().navigate(R.id.action_welcome_fragment_enter)
    }

    companion object {
        const val EXTRA_SCREEN = "screen"

        const val SCREEN_REFERENCE_NUMBER = "reference_number"
        const val SCREEN_PERSONAL_INFORMATION = "personal_information"
    }

    override val viewModelClassType: Class<DaoDefaultViewModel>
        get() = DaoDefaultViewModel::class.java

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentDaoDefaultBinding
        get() = FragmentDaoDefaultBinding::inflate
}
