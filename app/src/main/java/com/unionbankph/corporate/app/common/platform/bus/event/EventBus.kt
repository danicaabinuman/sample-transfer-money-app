package com.unionbankph.corporate.app.common.platform.bus.event

class EventBus {
    val accountSyncEvent = AccountSyncEvent()
    val proposedTransferDateSyncEvent = ProposedTransferDateSyncEvent()
    val notificationSyncEvent = NotificationSyncEvent()
    val settingsSyncEvent = SettingsSyncEvent()
    val biometricSyncEvent = BiometricSyncEvent()
    val inputSyncEvent = InputSyncEvent()
    val fragmentSettingsSyncEvent = FragmentSettingsSyncEvent()
    val resultSyncEvent = ResultSyncEvent()
    val actionSyncEvent = ActionSyncEvent()
    val transactSyncEvent = TransactSyncEvent()
}
