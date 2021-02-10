package com.unionbankph.corporate.mcd.presentation.constant

enum class CheckDepositStatusEnum(val value: String) {
    FOR_CLEARING("Pending Verification"),
    POSTED("For Clearing"),
    REJECTED("Transaction Rejected"),
    CLEARED("Transaction Cleared"),
    BOUNCED("Transaction Bounced");
}
