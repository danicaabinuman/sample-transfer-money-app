package com.unionbankph.corporate.user_creation.presentation.reminder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.jakewharton.rxbinding2.view.RxView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.databinding.FragmentUcReminderBinding
import com.unionbankph.corporate.user_creation.presentation.UserCreationActivity
import com.unionbankph.corporate.user_creation.presentation.UserCreationViewModel
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit

class UcReminderFragment :
    BaseFragment<FragmentUcReminderBinding, UserCreationViewModel>() {

    private val userCreationActivity by lazyFast { getAppCompatActivity() as UserCreationActivity }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)

        userCreationActivity.setIsScreenScrollable(false)
    }

    override fun onViewsBound() {
        super.onViewsBound()

        RxView.clicks(binding.btnOaReminderNext)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                findNavController().navigate(R.id.action_reminder_to_enter_name)
            }.addTo(disposables)
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentUcReminderBinding
        get() = FragmentUcReminderBinding::inflate

    override val viewModelClassType: Class<UserCreationViewModel>
        get() = UserCreationViewModel::class.java
}