package com.unionbankph.corporate.app.common.platform.bus.event

import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEventBus

class AccountSyncEvent : BaseEventBus<Account>() {

    companion object {
        const val ACTION_UPDATE_SELECTED_ACCOUNT = "update_selected_account"
        const val ACTION_UPDATE_CURRENT_BALANCE = "update_current_balance"
    }
}
