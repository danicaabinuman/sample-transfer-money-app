package com.unionbankph.corporate.app.common.platform.bus.event

import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEventBus
import com.unionbankph.corporate.auth.data.model.UserDetails

class SettingsSyncEvent : BaseEventBus<UserDetails>() {

    companion object {
        const val ACTION_UPDATE_MOBILE_NUMBER = "update_mobile_number"
        const val ACTION_UPDATE_EMAIL_ADDRESS = "update_email_address"
        const val ACTION_TUTORIAL_BOTTOM = "tutorial_bottom"
        const val ACTION_TUTORIAL_ALERTS = "tutorial_alerts"
        const val ACTION_TUTORIAL_ACCOUNT = "tutorial_account"
        const val ACTION_TUTORIAL_ACCOUNT_TAP = "tutorial_account_tap"
        const val ACTION_TUTORIAL_APPROVAL_TAP = "tutorial_approval_tap"
        const val ACTION_TUTORIAL_APPROVAL_SHOW_DATA = "tutorial_approval_show_data"
        const val ACTION_TUTORIAL_APPROVAL_CLEAR_DATA = "tutorial_approval_clear_data"
        const val ACTION_TUTORIAL_SETTINGS_DONE = "tutorial_settings_done"
        const val ACTION_TUTORIAL_LOGOUT_TAP = "tutorial_logout_tap"
        const val ACTION_RESET_TUTORIAL = "reset_tutorial"
        const val ACTION_SKIP_TUTORIAL = "skip_tutorial"
        const val ACTION_RESET_DEMO = "reset_demo"
        const val ACTION_SCROLL_TO_TOP = "scroll_to_top"
        const val ACTION_REFRESH_SCROLL_TO_TOP = "refresh_scroll_to_top"
        const val ACTION_PUSH_TUTORIAL_SCHEDULED_TRANSFER = "push_tutorial_scheduled_transfer"
        const val ACTION_PUSH_TUTORIAL_ACCOUNT = "push_tutorial_account"
        const val ACTION_PUSH_TUTORIAL_TRANSACT = "push_tutorial_transact"
        const val ACTION_PUSH_TUTORIAL_APPROVAL = "push_tutorial_approval"
        const val ACTION_PUSH_TUTORIAL_SETTINGS = "push_tutorial_settings"
        const val ACTION_PUSH_TUTORIAL_NOTIFICATIONS = "push_tutorial_notifications"
        const val ACTION_ENABLE_NAVIGATION_BOTTOM = "enable_navigation_bottom"
        const val ACTION_DISABLE_NAVIGATION_BOTTOM = "disable_navigation_bottom"
    }
}
