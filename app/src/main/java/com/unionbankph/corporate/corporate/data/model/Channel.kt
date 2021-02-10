package com.unionbankph.corporate.corporate.data.model

import com.unionbankph.corporate.common.data.model.ServiceFee
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Channel(

    @SerialName("id")
    var id: Int? = null,

    @SerialName("name")
    var name: String? = null,

    @SerialName("product")
    var product: Product? = null,

    @SerialName("permission")
    var permission: Permission? = null,

    @SerialName("context")
    var contextChannel: ContextChannel? = null,

    @SerialName("currencies")
    var currencies: MutableList<Currency> = mutableListOf(),

    @SerialName("orderNumber")
    var orderNumber: Int? = null,

    @SerialName("rules_allow_transaction")
    var hasRulesAllowTransaction: Boolean = true,

    @SerialName("has_source_account")
    var hasSourceAccount: Boolean = true,

    @SerialName("has_approval_rule")
    var hasApprovalRule: Boolean = true,

    @SerialName("hasPermission")
    var hasPermission: Boolean = true,

    @SerialName("service_fee")
    var serviceFee: ServiceFee? = null,

    @SerialName("custom_service_fee")
    var customServiceFee: ServiceFee? = null,

    @SerialName("reminders")
    var reminders: String? = null,

    @SerialName("formatted_reminders")
    var formattedReminders: MutableList<String> = mutableListOf()
)
