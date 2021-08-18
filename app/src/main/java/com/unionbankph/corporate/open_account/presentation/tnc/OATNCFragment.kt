package com.unionbankph.corporate.open_account.presentation.tnc

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.open_account.presentation.OpenAccountActivity
import com.unionbankph.corporate.open_account.presentation.OpenAccountViewModel
import kotlinx.android.synthetic.main.fragment_oa_terms_and_condition.*
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_oa_terms_and_condition.*
import java.util.concurrent.TimeUnit

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

        RxView.clicks(btn_proceed)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                findNavController().navigate(R.id.action_tnc_to_enter_contact_info)
            }.addTo(disposables)
    }
}


