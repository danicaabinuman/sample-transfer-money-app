package com.unionbankph.corporate.account_setup.presentation.terms_of_service

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account_setup.presentation.AccountSetupActivity
import com.unionbankph.corporate.account_setup.presentation.AccountSetupViewModel
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.enableButtonMSME
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.FragmentSettingsSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.InputSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.recyclerview.viewpager.ViewPagerAdapter
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.auth.data.model.CountryCode
import com.unionbankph.corporate.auth.presentation.policy.PrivacyPolicyFragment
import com.unionbankph.corporate.auth.presentation.policy.TermsAndConditionsFragment
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.databinding.FragmentAsTermsOfServiceBinding
import com.unionbankph.corporate.settings.presentation.SettingsFragment
import com.unionbankph.corporate.settings.presentation.display.SettingsDisplayFragment
import com.unionbankph.corporate.settings.presentation.form.Selector
import com.unionbankph.corporate.settings.presentation.notification.NotificationDetailFragment
import com.unionbankph.corporate.settings.presentation.notification.NotificationFragment
import com.unionbankph.corporate.settings.presentation.profile.ProfileSettingsFragment
import com.unionbankph.corporate.settings.presentation.security.SecurityFragment
import com.unionbankph.corporate.settings.presentation.security.device.ManageDeviceDetailFragment
import com.unionbankph.corporate.settings.presentation.security.device.ManageDevicesFragment
import com.unionbankph.corporate.settings.presentation.security.otp.SecurityEnableOTPFragment
import com.unionbankph.corporate.settings.presentation.security.otp.SecurityManageOTPFragment
import com.unionbankph.corporate.settings.presentation.security.otp.SecurityReceiveOTPFragment
import com.unionbankph.corporate.settings.presentation.single_selector.SingleSelectorTypeEnum
import com.unionbankph.corporate.settings.presentation.totp.TOTPActivity
import io.reactivex.rxkotlin.addTo
import timber.log.Timber

class AsTermsOfServiceFragment :
    BaseFragment<FragmentAsTermsOfServiceBinding, AccountSetupViewModel>() {

    private var adapter: ViewPagerAdapter? = null
    private val accountSetupActivity by lazyFast { getAppCompatActivity() as AccountSetupActivity }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)

        accountSetupActivity.setToolbarTitle(accountSetupActivity.binding.tvToolbar, getString(R.string.title_terms_of_service))
    }


    override fun onViewsBound() {
        super.onViewsBound()
        init()
    }

    private fun init() {
        setupViewPager()
    }

    private fun setupViewPager() {
        adapter = ViewPagerAdapter(
            childFragmentManager
        )
        adapter?.addFragment(
            AsTermsOfServiceViewPager.newInstance(),
            getString(R.string.title_terms_of_service_caps)
        )
        adapter?.addFragment(
            AsPrivacyPolicyViewPager.newInstance(),
            getString(R.string.title_privacy_policy_caps)
        )
        binding.viewPagerTermsOfService.adapter = adapter
        binding.viewPagerTermsOfService.offscreenPageLimit = 0
        binding.tlTermsOfService.setupWithViewPager(binding.viewPagerTermsOfService, false)
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initEventBus()

    }

    private fun initEventBus() {
        eventBus.actionSyncEvent.flowable.subscribe(
            {
            when (it.eventType) {
                ActionSyncEvent.ACTION_SCROLL_TERMS_SERVICE ->
                {
                    val isShowNextButton = it.payload == "1"
                    binding.buttonNext.enableButtonMSME(isShowNextButton)
                }
            }
        }).addTo(disposables)
    }



    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentAsTermsOfServiceBinding
        get() = FragmentAsTermsOfServiceBinding::inflate
    override val viewModelClassType: Class<AccountSetupViewModel>
        get() = AccountSetupViewModel::class.java

}