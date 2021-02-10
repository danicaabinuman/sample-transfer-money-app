package com.unionbankph.corporate.bills_payment.data.form

import com.unionbankph.corporate.bills_payment.data.model.Amount
import com.unionbankph.corporate.bills_payment.data.model.Reference
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BillsPaymentForm(

    @SerialName("amount")
    var amount: Amount? = null,

    @SerialName("biller_id")
    var billerId: String? = null,

    @SerialName("biller_name")
    var billerName: String? = null,

    @SerialName("biller_service_id")
    var billerServiceId: String? = null,

    @SerialName("frequency")
    var frequency: String? = null,

    @SerialName("destination")
    var destination: String? = null,

    @SerialName("immediate")
    var immediate: Boolean? = null,

    @SerialName("organization_id")
    var organizationId: String? = null,

    @SerialName("payment_date")
    var paymentDate: String? = null,

    @SerialName("occurrences")
    var occurrences: Int? = null,

    @SerialName("recurrence_type_id")
    var recurrenceTypeId: String? = null,

    @SerialName("recurrence_end_date")
    var recurrenceEndDate: String? = null,

    @SerialName("references")
    var references: MutableList<Reference> = mutableListOf(),

    @SerialName("remarks")
    var remarks: String? = null,

    @SerialName("source")
    var source: String? = null,

    @SerialName("frequent_biller_version_id")
    var frequentBillerVersionId: Int? = null,

    @SerialName("reference_number")
    var referenceNumber: String? = null
)
