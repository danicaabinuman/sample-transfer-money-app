package com.unionbankph.corporate.app.common.platform.bus.event

import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEventBus

class ResultSyncEvent : BaseEventBus<String>() {

    companion object {
        const val ACTION_SUCCESS_SECURITY = "success_security"
        const val ACTION_BACK_SECURITY = "action_back_security"
        const val ACTION_UPDATE_NOTIFICATION = "action_update_notification"
    }
}
