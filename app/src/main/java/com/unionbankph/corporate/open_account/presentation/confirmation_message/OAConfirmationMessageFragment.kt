package com.unionbankph.corporate.open_account.presentation.confirmation_message

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.rxbinding2.view.RxView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.databinding.FragmentOaConfirmationPageBinding
import com.unionbankph.corporate.open_account.presentation.OpenAccountActivity
import com.unionbankph.corporate.open_account.presentation.OpenAccountViewModel
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit

class OAConfirmationMessageFragment :
    BaseFragment<FragmentOaConfirmationPageBinding, OpenAccountViewModel>() {

    private val openAccountActivity by lazyFast { getAppCompatActivity() as OpenAccountActivity }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        openAccountActivity.setIsScreenScrollable(false)
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
    }

    override fun onViewsBound() {
        super.onViewsBound()

        RxView.clicks(binding.buttonNext)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {

            }.addTo(disposables)
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentOaConfirmationPageBinding
        get() = FragmentOaConfirmationPageBinding::inflate

    override val viewModelClassType: Class<OpenAccountViewModel>
        get() = OpenAccountViewModel::class.java
}