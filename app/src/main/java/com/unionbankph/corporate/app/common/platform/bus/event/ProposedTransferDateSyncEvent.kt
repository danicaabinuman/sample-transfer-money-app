package com.unionbankph.corporate.app.common.platform.bus.event

import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEventBus
import com.unionbankph.corporate.common.data.model.ProposedTransferDate

class ProposedTransferDateSyncEvent : BaseEventBus<ProposedTransferDate>() {

    companion object {
        const val ACTION_SET_PROPOSED_TRANSFER_DATE = "set_proposed_transfer_date"
    }
}
