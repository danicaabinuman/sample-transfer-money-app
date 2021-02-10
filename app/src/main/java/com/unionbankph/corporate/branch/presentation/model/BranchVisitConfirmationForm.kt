package com.unionbankph.corporate.branch.presentation.model

import com.unionbankph.corporate.common.data.model.ServiceFee
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BranchVisitConfirmationForm(

    @SerialName("remarks")
    var remarks: String? = null,

    @SerialName("deposit_to")
    var depositTo: String? = null,

    @SerialName("branch_sol_id")
    var branchSolId: String? = null,

    @SerialName("branch_name")
    var branchName: String? = null,

    @SerialName("branch_address")
    var branchAddress: String? = null,

    @SerialName("number_of_transactions")
    var numberOfTransactions: Int = 0,

    @SerialName("cash_deposit_size")
    var cashDepositSize: Int = 0,

    @SerialName("check_deposit_off_size")
    var checkDepositOffSize: Int = 0,

    @SerialName("check_deposit_on_size")
    var checkDepositOnSize: Int = 0,

    @SerialName("is_batch")
    var isBatch: Boolean = false,

    @SerialName("service_fee")
    var serviceFee: ServiceFee? = null,

    @SerialName("total_amount_cash_deposit")
    var totalAmountCashDeposit: Double? = null,

    @SerialName("total_amount_check_deposit_off")
    var totalAmountCheckDepositOff: Double? = null,

    @SerialName("total_amount_check_deposit_on")
    var totalAmountCheckDepositOn: Double? = null,

    @SerialName("total_amount")
    var totalAmount: Double? = null,

    @SerialName("currency")
    var currency: String? = null,

    @SerialName("channel")
    var channel: String? = null,

    @SerialName("transaction_date")
    var transactionDate: String? = null
)
