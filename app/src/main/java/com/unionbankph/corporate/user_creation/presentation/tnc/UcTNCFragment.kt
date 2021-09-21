package com.unionbankph.corporate.user_creation.presentation.tnc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.user_creation.presentation.UserCreationActivity
import com.unionbankph.corporate.user_creation.presentation.UserCreationViewModel
import com.jakewharton.rxbinding2.view.RxView
import com.unionbankph.corporate.databinding.FragmentUcTermsAndConditionBinding
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit

class UcTNCFragment :
    BaseFragment<FragmentUcTermsAndConditionBinding, UserCreationViewModel>() {

    private val userCreationActivity by lazyFast { getAppCompatActivity() as UserCreationActivity }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)

        userCreationActivity.setIsScreenScrollable(false)
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

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentUcTermsAndConditionBinding
        get() = FragmentUcTermsAndConditionBinding::inflate

    override val viewModelClassType: Class<UserCreationViewModel>
        get() = UserCreationViewModel::class.java
}


