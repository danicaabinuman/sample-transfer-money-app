package com.unionbankph.corporate.open_account.presentation.select_account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.databinding.FragmentOaAccountSelectionBinding
import com.unionbankph.corporate.open_account.presentation.OpenAccountActivity
import com.unionbankph.corporate.open_account.presentation.OpenAccountViewModel

class OAAccountSelection :
    BaseFragment<FragmentOaAccountSelectionBinding, OpenAccountViewModel>() {

    private val openAccountActivity by lazyFast { getAppCompatActivity() as OpenAccountActivity }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)

        openAccountActivity.setIsScreenScrollable(false)
    }

    override fun onViewsBound() {
        super.onViewsBound()

        initClickListener()
    }

    private fun initClickListener() {
        binding.btnOpenAccount.setOnClickListener {
            findNavController().navigate(R.id.action_selection_to_reminder)
        }
        binding.btnContinueExistingAccountApplication.setOnClickListener {
            findNavController().navigate(R.id.action_dao_selection_activity)
        }
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentOaAccountSelectionBinding
        get() = FragmentOaAccountSelectionBinding::inflate

    override val viewModelClassType: Class<OpenAccountViewModel>
        get() = OpenAccountViewModel::class.java
}