package com.unionbankph.corporate.open_account.presentation.tnc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.open_account.presentation.OpenAccountActivity
import com.unionbankph.corporate.open_account.presentation.OpenAccountViewModel
import com.jakewharton.rxbinding2.view.RxView
import com.unionbankph.corporate.databinding.FragmentOaTermsAndConditionBinding
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit

class OATNCFragment :
    BaseFragment<FragmentOaTermsAndConditionBinding, OpenAccountViewModel>() {

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

        RxView.clicks(binding.btnProceed)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                findNavController().navigate(R.id.action_tnc_to_enter_contact_info)
            }.addTo(disposables)
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentOaTermsAndConditionBinding
        get() = FragmentOaTermsAndConditionBinding::inflate

    override val viewModelClassType: Class<OpenAccountViewModel>
        get() = OpenAccountViewModel::class.java
}


