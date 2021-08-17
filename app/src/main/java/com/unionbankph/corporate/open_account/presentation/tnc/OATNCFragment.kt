package com.unionbankph.corporate.open_account.presentation.tnc

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.open_account.presentation.OpenAccountActivity
import com.unionbankph.corporate.open_account.presentation.OpenAccountViewModel
import kotlinx.android.synthetic.main.fragment_oa_terms_and_condition.*

class OATNCFragment :
    BaseFragment<OpenAccountViewModel>(R.layout.fragment_oa_terms_and_condition) {

    private val openAccountActivity by lazyFast { getAppCompatActivity() as OpenAccountActivity }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)

        openAccountActivity.setIsScreenScrollable(false)
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initOnClicks()
    }

    private fun initOnClicks() {
        btn_proceed.setOnClickListener {
            findNavController().navigate(R.id.action_tnc_to_nominate_password)
        }
    }
}