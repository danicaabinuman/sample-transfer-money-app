package com.unionbankph.corporate.app.common.platform.bus.event

import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEventBus

class TransactSyncEvent : BaseEventBus<Account>() {

    companion object {
        const val ACTION_GO_TO_PAYMENT_LINK_LIST = "go_to_payment_link_list"
        const val ACTION_VALIDATE_MERCHANT_EXIST = "validate_merchant_exist"
        const val ACTION_REDIRECT_TO_PAYMENT_LINK_LIST = "redirect_to_payment_link_list"
    }
}
