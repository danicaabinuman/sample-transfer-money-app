package com.unionbankph.corporate.open_account.presentation.personalise_settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.jakewharton.rxbinding2.view.RxView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.databinding.FragmentOaPersonaliseSettingsBinding
import com.unionbankph.corporate.open_account.presentation.OpenAccountActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit


class OAPersonaliseSettingsFragment :
    BaseFragment<FragmentOaPersonaliseSettingsBinding, OAPersonaliseSettingsViewModel>()  {
    private val formDisposable = CompositeDisposable()

    private val openAccountActivity by lazyFast { getAppCompatActivity() as OpenAccountActivity }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        (activity as AppCompatActivity?)!!.supportActionBar!!.show()
        openAccountActivity.setIsScreenScrollable(false)
    }

    override fun onViewsBound() {
        super.onViewsBound()

        RxView.clicks(binding.buttonNext)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                findNavController().navigate(R.id.action_permission_settings_to_confirmation_message)
            }.addTo(formDisposable)
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentOaPersonaliseSettingsBinding
        get() = FragmentOaPersonaliseSettingsBinding::inflate

    override val viewModelClassType: Class<OAPersonaliseSettingsViewModel>
        get() = OAPersonaliseSettingsViewModel::class.java
}