package com.unionbankph.corporate.branch.presentation.constant

/**
 * Created by herald25santos on 2019-12-17
 */
enum class BranchVisitStatusEnum(val value: String) {
    APPOINTMENT_SET("Appointment Set"),
    APPOINTMENT_REJECTED("Appointment Rejected"),
    PENDING_APPROVAL("Pending Approval"),
    APPOINTMENT_ELAPSED("Appointment Elapsed"),
    TRANSACTION_SUCCESSFUL("Transaction Successful"),
    TRANSACTION_FAILED("Transaction Failed"),
    EXPIRED("Expired"),
    UNKNOWN("-")
}
