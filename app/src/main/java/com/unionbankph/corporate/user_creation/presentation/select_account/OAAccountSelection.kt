package com.unionbankph.corporate.user_creation.presentation.select_account

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.user_creation.presentation.OpenAccountActivity
import com.unionbankph.corporate.user_creation.presentation.OpenAccountViewModel
import kotlinx.android.synthetic.main.fragment_oa_account_selection.*
import timber.log.Timber

class OAAccountSelection :
    BaseFragment<OpenAccountViewModel>(R.layout.fragment_oa_account_selection) {

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
        btn_open_account.setOnClickListener {
            findNavController().navigate(R.id.action_selection_to_reminder)
        }
    }
}