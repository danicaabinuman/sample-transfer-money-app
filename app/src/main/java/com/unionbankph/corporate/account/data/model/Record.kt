package com.unionbankph.corporate.account.data.model

import com.unionbankph.corporate.common.data.model.ContextualClassStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Record(

    @SerialName("amount")
    var amount: String? = null,

    @SerialName("currency")
    var currency: String? = null,

    @SerialName("balance")
    var balance: String? = null,

    @SerialName("remarks")
    var remarks: String? = null,

    @SerialName("remarks2")
    var remarks2: String? = null,

    @SerialName("value_date")
    var valueDate: String? = null,

    @SerialName("serial_no")
    var serialNo: String? = null,

    @SerialName("tran_date")
    var tranDate: String? = null,

    @SerialName("tran_id")
    var tranId: String? = null,

    @SerialName("tran_type")
    var tranType: String? = null,

    @SerialName("tran_category")
    var tranCategory: String? = null,

    @SerialName("balance_currency")
    var balanceCurrency: String? = null,

    @SerialName("posted_date")
    var postedDate: String? = null,

    @SerialName("tran_description")
    var tranDescription: String? = null,

    @SerialName("sol_id")
    var solId: String? = null,

    @SerialName("record_number")
    var recordNumber: String? = null,

    @SerialName("tran_code")
    var tranCode: String? = null,

    @SerialName("ref_number")
    var refNumber: String? = null,

    @SerialName("instrument_id")
    var instrumentId: String? = null,

    @SerialName("ending_balance")
    var endingBalance: String? = null,

    @SerialName("transaction_class")
    var transactionClass: ContextualClassStatus? = null
)
