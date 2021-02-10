package com.unionbankph.corporate.common.presentation.constant

/**
 * Created by herald25santos on 31/01/2019
 */
enum class ChannelBankEnum(val value: String) {
    TO_OWN_ACCOUNT("To Own UnionBank Account"),
    UBP_TO_UBP("UnionBank Account"),
    PESONET("PESONet"),
    PDDTS("PDDTS"),
    SWIFT("SWIFT"),
    OTHER_BANKS("Other Banks"),
    INSTAPAY("instaPay"),
    RTGS("RTGS"),
    CHECK_WRITER("CHECKWRITER"),
    CASH_WITHDRAWAL("Cash Withdrawal"),
    CASH_DEPOSIT("Cash Deposit"),
    BILLS_PAYMENT("Bills Payment"),
    SSS("SSS"),
    BIR("BIR");

    fun getChannelId(): Int {
        return when (this) {
            UBP_TO_UBP -> 3
            TO_OWN_ACCOUNT -> 8
            PESONET -> 4
            PDDTS -> 1
            SWIFT -> 2
            INSTAPAY -> 7
            BILLS_PAYMENT -> 5
            SSS -> 10
            BIR -> 9
            else -> 0
        }
    }

    companion object {
        fun from(search: String): ChannelBankEnum =
            requireNotNull(values().find { it.value == search }) { "No TaskAction with value $search" }
    }
}
