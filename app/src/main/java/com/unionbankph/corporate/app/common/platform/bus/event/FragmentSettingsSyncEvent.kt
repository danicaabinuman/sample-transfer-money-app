package com.unionbankph.corporate.app.common.platform.bus.event

import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEventBus

class FragmentSettingsSyncEvent : BaseEventBus<String>() {

    companion object {
        const val ACTION_CLICK_PROFILE = "click_profile"
        const val ACTION_CLICK_SECURITY = "click_security"
        const val ACTION_CLICK_SECURITY_MANAGE_OTP = "click_security_manage_otp"
        const val ACTION_CLICK_SECURITY_ENABLE_OTP = "click_security_enable_otp"
        const val ACTION_CLICK_SECURITY_RECEIVE_OTP = "click_security_receive_otp"
        const val ACTION_CLICK_SECURITY_GENERATE_OTP = "click_security_generate_otp"
        const val ACTION_CLICK_NOTIFICATION = "click_notification"
        const val ACTION_CLICK_DISPLAY = "click_display"
        const val ACTION_CLICK_NOTIFICATION_DETAIL = "click_notification_detail"
        const val ACTION_CLICK_MANAGE_DEVICES = "click_manage_devices"
        const val ACTION_CLICK_MANAGE_DEVICE_DETAIL = "click_manage_device_detail"
        const val ACTION_CLICK_NOTIFICATION_LOG_ITEM = "click_manage_notification_log_item"

        const val ACTION_CLICK_HELP_GENERAL = "click_help_general"
        const val ACTION_CLICK_HELP_SECURITY = "click_help_security"

        const val ACTION_ON_BACK_PRESSED = "on_back_pressed"
    }
}
