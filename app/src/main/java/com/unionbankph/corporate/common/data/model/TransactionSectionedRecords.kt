package com.unionbankph.corporate.common.data.model

import com.unionbankph.corporate.account.data.model.Record

data class TransactionSectionedRecords(
    var record: Record? = null,
    var records: MutableList<Record> = mutableListOf()
)
