package com.unionbankph.corporate.account_setup.presentation.citizenship

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account_setup.presentation.AccountSetupActivity
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.databinding.FragmentAsCitizenshipBinding

class AsCitizenshipFragment :
    BaseFragment<FragmentAsCitizenshipBinding, AsCitizenshipViewModel>() {

    private val accountSetupActivity by lazyFast { getAppCompatActivity() as AccountSetupActivity }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)

        accountSetupActivity.apply {
            showProgress(false)
            showToolbarButton(false)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            accountSetupActivity.popBackStack()
        }
    }

    override fun onViewModelBound() {
        super.onViewModelBound()

        viewModel.state.observe(viewLifecycleOwner, EventObserver {
            accountSetupActivity.viewModel.setCitizenship(it)
            findNavController().navigate(R.id.action_sole_prop_personal_info)
        })
    }

    override fun onInitializeListener() {
        super.onInitializeListener()

        binding.btnAsCitizenshipYes.setOnClickListener {
            viewModel.onClickedNext(Constant.Citizenship.FILIPINO)
        }
    }


    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentAsCitizenshipBinding
        get() = FragmentAsCitizenshipBinding::inflate

    override val viewModelClassType: Class<AsCitizenshipViewModel>
        get() = AsCitizenshipViewModel::class.java
}