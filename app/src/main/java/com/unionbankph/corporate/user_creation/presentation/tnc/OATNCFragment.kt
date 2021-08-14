package com.unionbankph.corporate.user_creation.presentation.tnc

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.jakewharton.rxbinding2.view.RxView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.user_creation.presentation.OpenAccountActivity
import com.unionbankph.corporate.user_creation.presentation.OpenAccountViewModel
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


