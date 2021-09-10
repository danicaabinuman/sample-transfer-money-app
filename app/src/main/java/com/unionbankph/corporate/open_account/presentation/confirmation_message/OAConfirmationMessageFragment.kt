package com.unionbankph.corporate.open_account.presentation.confirmation_message

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.rxbinding2.view.RxView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.app.service.fcm.AutobahnFirebaseMessagingService
import com.unionbankph.corporate.databinding.FragmentOaConfirmationPageBinding
import com.unionbankph.corporate.open_account.presentation.OpenAccountActivity
import com.unionbankph.corporate.open_account.presentation.OpenAccountViewModel
import com.unionbankph.corporate.open_account.presentation.trial_account.TrialAccountActivity
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit

class OAConfirmationMessageFragment :
    BaseFragment<FragmentOaConfirmationPageBinding, OpenAccountViewModel>() {

    private val openAccountActivity by lazyFast { getAppCompatActivity() as OpenAccountActivity }

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

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentOaConfirmationPageBinding
        get() = FragmentOaConfirmationPageBinding::inflate

    override val viewModelClassType: Class<OpenAccountViewModel>
        get() = OpenAccountViewModel::class.java

}