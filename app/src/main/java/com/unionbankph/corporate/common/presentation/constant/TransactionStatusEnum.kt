package com.unionbankph.corporate.common.presentation.constant

enum class TransactionStatusEnum(val value: String) {
    FOR_RELEASE("For Release"),
    FOR_APPROVAL("Pending Approval"),
    DEBITED("Debited"),
    EXPIRED("Expired"),
    CREATED("Created"),
    PROCESSING("Processing"),
    REJECTED("Rejected"),
    REVERSED("Reversed"),
    CANCELLED("Cancelled"),
    SCHEDULE_CANCELLED("Schedule Cancelled"),
    SCHEDULED("Scheduled"),
    TRANSACTION_FAILED("Transaction Failed"),
    TRANSACTION_PARTIALLY_RELEASED("Transaction Partially Released"),
    TRANSACTION_PARTIALLY_SUCCESSFUL("Transaction Partially Successful"),
    TRANSACTION_RELEASED("Transaction Released"),
    TRANSACTION_SUCCESSFUL("Transaction Successful"),
    FOR_OTP_VERIFICATION("OTP Verification"),
    APPOINTMENT_SET("Appointment Set"),
    APPOINTMENT_REJECTED("Appointment Rejected"),
    APPOINTMENT_ELAPSED("Appointment Elapsed"),
    PENDING_APPROVAL("Pending Approval");

    companion object {
        fun from(search: String): TransactionStatusEnum =
            requireNotNull(values().find { it.value == search }) { "No TaskAction with value $search" }
    }
}
