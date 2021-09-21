package com.unionbankph.corporate.user_creation.presentation.personalise_settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.jakewharton.rxbinding2.view.RxView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.databinding.FragmentUcPersonaliseSettingsBinding
import com.unionbankph.corporate.user_creation.presentation.UserCreationActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit


class UcPersonaliseSettingsFragment :
    BaseFragment<FragmentUcPersonaliseSettingsBinding, UcPersonaliseSettingsViewModel>()  {
    private val formDisposable = CompositeDisposable()

    private val userCreationActivity by lazyFast { getAppCompatActivity() as UserCreationActivity }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        userCreationActivity.setIsScreenScrollable(false)
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

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentUcPersonaliseSettingsBinding
        get() = FragmentUcPersonaliseSettingsBinding::inflate

    override val viewModelClassType: Class<UcPersonaliseSettingsViewModel>
        get() = UcPersonaliseSettingsViewModel::class.java
}