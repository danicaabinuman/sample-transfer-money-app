package com.unionbankph.corporate.settings.presentation

import android.os.Bundle
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.platform.bus.event.FragmentSettingsSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.SettingsSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.settings.presentation.display.SettingsDisplayFragment
import com.unionbankph.corporate.settings.presentation.general.GeneralSettingsFragment
import com.unionbankph.corporate.settings.presentation.notification.NotificationDetailFragment
import com.unionbankph.corporate.settings.presentation.notification.NotificationFragment
import com.unionbankph.corporate.settings.presentation.profile.ProfileSettingsFragment
import com.unionbankph.corporate.settings.presentation.security.SecurityFragment
import com.unionbankph.corporate.settings.presentation.security.device.ManageDeviceDetailFragment
import com.unionbankph.corporate.settings.presentation.security.device.ManageDevicesFragment
import com.unionbankph.corporate.settings.presentation.security.otp.SecurityEnableOTPFragment
import com.unionbankph.corporate.settings.presentation.security.otp.SecurityManageOTPFragment
import com.unionbankph.corporate.settings.presentation.security.otp.SecurityReceiveOTPFragment
import com.unionbankph.corporate.settings.presentation.totp.TOTPActivity
import io.reactivex.rxkotlin.addTo
import timber.log.Timber

class SettingsFragment : BaseFragment<SettingsViewModel>(R.layout.fragment_settings) {

    override fun onViewsBound() {
        super.onViewsBound()
        init()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initListener()
    }

    override fun onResume() {
        super.onResume()
        if (hasInitialLoad) {
            hasInitialLoad = false
        }
    }

    private fun init() {
        navigator.addFragmentWithAnimation(
            R.id.frameLayoutSettings,
            GeneralSettingsFragment(),
            null,
            childFragmentManager,
            FRAGMENT_GENERAL_SETTINGS
        )
    }

    private fun initListener() {
        initEventBus()
    }

    private fun initEventBus() {
        eventBus.fragmentSettingsSyncEvent.flowable.subscribe(
            {
                when (it.eventType) {
                    FragmentSettingsSyncEvent.ACTION_CLICK_PROFILE ->
                        navigator.addFragmentWithAnimation(
                            R.id.frameLayoutSettings,
                            ProfileSettingsFragment(),
                            null,
                            childFragmentManager,
                            FRAGMENT_PROFILE
                        )
                    FragmentSettingsSyncEvent.ACTION_CLICK_SECURITY ->
                        navigator.addFragmentWithAnimation(
                            R.id.frameLayoutSettings,
                            SecurityFragment(),
                            null,
                            childFragmentManager,
                            FRAGMENT_SECURITY
                        )
                    FragmentSettingsSyncEvent.ACTION_CLICK_NOTIFICATION ->
                        navigator.addFragmentWithAnimation(
                            R.id.frameLayoutSettings,
                            NotificationFragment(),
                            null,
                            childFragmentManager,
                            FRAGMENT_NOTIFICATION
                        )
                    FragmentSettingsSyncEvent.ACTION_CLICK_SECURITY_MANAGE_OTP ->
                        navigator.addFragmentWithAnimation(
                            R.id.frameLayoutSettings,
                            SecurityManageOTPFragment(),
                            null,
                            childFragmentManager,
                            FRAGMENT_SECURITY_MANAGE_OTP
                        )
                    FragmentSettingsSyncEvent.ACTION_CLICK_SECURITY_ENABLE_OTP ->
                        navigator.addFragmentWithAnimation(
                            R.id.frameLayoutSettings,
                            SecurityEnableOTPFragment(),
                            null,
                            childFragmentManager,
                            FRAGMENT_SECURITY_ENABLE_OTP
                        )
                    FragmentSettingsSyncEvent.ACTION_CLICK_SECURITY_RECEIVE_OTP ->
                        navigator.addFragmentWithAnimation(
                            R.id.frameLayoutSettings,
                            SecurityReceiveOTPFragment(),
                            null,
                            childFragmentManager,
                            FRAGMENT_SECURITY_RECEIVE_OTP
                        )
                    FragmentSettingsSyncEvent.ACTION_CLICK_DISPLAY ->
                        navigator.addFragmentWithAnimation(
                            R.id.frameLayoutSettings,
                            SettingsDisplayFragment(),
                            null,
                            childFragmentManager,
                            FRAGMENT_SETTINGS_DISPLAY
                        )
                    FragmentSettingsSyncEvent.ACTION_CLICK_SECURITY_GENERATE_OTP ->
                        navigator.navigate(
                            getAppCompatActivity(),
                            TOTPActivity::class.java,
                            null,
                            isClear = false,
                            isAnimated = true,
                            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_UP
                        )
                    FragmentSettingsSyncEvent.ACTION_CLICK_NOTIFICATION_DETAIL -> {
                        val bundle = Bundle()
                        bundle.putString(NotificationDetailFragment.EXTRA_ID, it.payload)
                        navigator.addFragmentWithAnimation(
                            R.id.frameLayoutSettings,
                            NotificationDetailFragment(),
                            bundle,
                            childFragmentManager,
                            FRAGMENT_NOTIFICATION_DETAIL
                        )
                    }
                    FragmentSettingsSyncEvent.ACTION_CLICK_MANAGE_DEVICES -> {
                        navigator.addFragmentWithAnimation(
                            R.id.frameLayoutSettings,
                            ManageDevicesFragment(),
                            null,
                            childFragmentManager,
                            FRAGMENT_MANAGE_DEVICES
                        )
                    }
                    FragmentSettingsSyncEvent.ACTION_CLICK_MANAGE_DEVICE_DETAIL -> {
                        val bundle = Bundle()
                        bundle.putString(ManageDeviceDetailFragment.EXTRA_ID, it.payload)
                        navigator.addFragmentWithAnimation(
                            R.id.frameLayoutSettings,
                            ManageDeviceDetailFragment(),
                            bundle,
                            childFragmentManager,
                            FRAGMENT_MANAGE_DEVICE_DETAIL
                        )
                    }
                }
            }, {
                Timber.e(it, "fragmentSettingsSyncEvent")
            }
        ).addTo(disposables)
        eventBus.settingsSyncEvent.flowable.subscribe {
            if (it.eventType == SettingsSyncEvent.ACTION_PUSH_TUTORIAL_SETTINGS) {
                val fragment = childFragmentManager.findFragmentById(R.id.frameLayoutSettings)
                if (fragment is GeneralSettingsFragment) {
                    eventBus.fragmentSettingsSyncEvent.emmit(
                        BaseEvent(FragmentSettingsSyncEvent.ACTION_CLICK_HELP_GENERAL)
                    )
                } else if (fragment is SecurityFragment) {
                    eventBus.fragmentSettingsSyncEvent.emmit(
                        BaseEvent(FragmentSettingsSyncEvent.ACTION_CLICK_HELP_SECURITY)
                    )
                }
            }
        }.addTo(disposables)
    }

    companion object {
        const val FRAGMENT_PROFILE = "profile"
        const val FRAGMENT_GENERAL_SETTINGS = "general_settings"
        const val FRAGMENT_SECURITY = "security"
        const val FRAGMENT_SECURITY_MANAGE_OTP = "security_manage_otp"
        const val FRAGMENT_SECURITY_ENABLE_OTP = "security_enable_otp"
        const val FRAGMENT_SECURITY_RECEIVE_OTP = "security_receive_otp"
        const val FRAGMENT_SETTINGS_DISPLAY = "settings_display"
        const val FRAGMENT_NOTIFICATION_DETAIL = "notification_detail"
        const val FRAGMENT_NOTIFICATION = "notification"
        const val FRAGMENT_MANAGE_DEVICES = "manage_devices"
        const val FRAGMENT_MANAGE_DEVICE_DETAIL = "manage_device_detail"
        const val FRAGMENT_NOTIFICATION_LOG = "notification_log"
    }
}
