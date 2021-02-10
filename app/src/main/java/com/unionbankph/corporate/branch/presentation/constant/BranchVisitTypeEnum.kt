package com.unionbankph.corporate.branch.presentation.constant

/**
 * Created by herald25santos on 2019-12-17
 */
enum class BranchVisitTypeEnum(val value: String) {
    CASH_DEPOSIT("cashdeposit"),
    CASH_WITHDRAW("cashwithdraw"),
    CHECK_ENCASHMENT("checkencashment"),
    CHECK_DEPOSIT_ON_US("checkdepositonus"),
    CHECK_DEPOSIT_OFF_US("checkdepositoffus"),
    BANK_CERTIFICATE("bankcertificate")
}
