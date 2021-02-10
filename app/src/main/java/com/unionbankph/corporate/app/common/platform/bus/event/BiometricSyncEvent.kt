package com.unionbankph.corporate.app.common.platform.bus.event

import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEventBus

class BiometricSyncEvent : BaseEventBus<Boolean>() {

    companion object {
        const val ACTION_UPDATE_BIOMETRIC = "update_biometric"
    }
}
