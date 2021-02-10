package com.unionbankph.corporate.app.common.platform.bus.data

import com.unionbankph.corporate.account.data.model.Record
import com.unionbankph.corporate.app.common.platform.bus.base.BaseBus

class TransactionHistoryDataBus : BaseBus<MutableList<Record>>()
