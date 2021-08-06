package com.unionbankph.corporate.user_creation.presentation.tnc

import android.os.Bundle
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.user_creation.presentation.OpenAccountActivity
import com.unionbankph.corporate.user_creation.presentation.OpenAccountViewModel

class OATNCFragment :
    BaseFragment<OpenAccountViewModel>(R.layout.fragment_oa_terms_and_condition) {

    private val openAccountActivity by lazyFast { getAppCompatActivity() as OpenAccountActivity }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)

        openAccountActivity.setIsScreenScrollable(false)
    }

    override fun onViewsBound() {
        super.onViewsBound()
    }
}