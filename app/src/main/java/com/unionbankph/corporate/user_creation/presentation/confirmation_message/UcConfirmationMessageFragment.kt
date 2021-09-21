package com.unionbankph.corporate.user_creation.presentation.confirmation_message

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.rxbinding2.view.RxView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.app.service.fcm.AutobahnFirebaseMessagingService
import com.unionbankph.corporate.databinding.FragmentUcConfirmationPageBinding
import com.unionbankph.corporate.user_creation.presentation.UserCreationActivity
import com.unionbankph.corporate.user_creation.presentation.UserCreationViewModel
import com.unionbankph.corporate.trial_account.presentation.TrialAccountActivity
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit

class UcConfirmationMessageFragment :
    BaseFragment<FragmentUcConfirmationPageBinding, UserCreationViewModel>() {

    private val openAccountActivity by lazyFast { getAppCompatActivity() as UserCreationActivity }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)

        initViews()
    }

    override fun onViewsBound() {
        super.onViewsBound()

        initOnClick()
    }

    private fun initViews(){
        openAccountActivity.setIsScreenScrollable(false)
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
        openAccountActivity.onBackPressedDispatcher.addCallback(viewLifecycleOwner) {}
    }

    private fun initOnClick(){
        RxView.clicks(binding.buttonNext)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                navigateTrialModeScreen()
            }.addTo(disposables)
    }

    private fun navigateTrialModeScreen() {
        val bundle = Bundle()
        bundle.putString(
            AutobahnFirebaseMessagingService.EXTRA_DATA,
            getAppCompatActivity().intent.getStringExtra(AutobahnFirebaseMessagingService.EXTRA_DATA)
        )
        navigator.navigateClearStacks(
            getAppCompatActivity(),
            TrialAccountActivity::class.java,
            bundle,
            true,
            Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun navigateDashboardScreen() {
        val bundle = Bundle().apply {
            putString(
                AutobahnFirebaseMessagingService.EXTRA_DATA,
                getAppCompatActivity().intent.getStringExtra(AutobahnFirebaseMessagingService.EXTRA_DATA)
            )
        }
        navigator.navigateClearStacks(
            getAppCompatActivity(),
            DashboardActivity::class.java,
            bundle,
            true,
            Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentUcConfirmationPageBinding
        get() = FragmentUcConfirmationPageBinding::inflate

    override val viewModelClassType: Class<UserCreationViewModel>
        get() = UserCreationViewModel::class.java

}