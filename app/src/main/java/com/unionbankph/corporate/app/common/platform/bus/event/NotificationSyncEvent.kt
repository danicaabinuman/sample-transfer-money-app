package com.unionbankph.corporate.app.common.platform.bus.event

import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEventBus

class NotificationSyncEvent : BaseEventBus<Int>() {

    companion object {
        const val ACTION_UPDATE_NOTIFICATION_APPROVAL = "update_notification_approval"
        const val ACTION_UPDATE_NOTIFICATION_ALERTS = "update_notification_alerts"
    }
}
