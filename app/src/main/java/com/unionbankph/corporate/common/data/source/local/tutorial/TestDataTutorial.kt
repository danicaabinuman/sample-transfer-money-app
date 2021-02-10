package com.unionbankph.corporate.common.data.source.local.tutorial

import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.approval.data.model.Transaction
import com.unionbankph.corporate.bills_payment.data.model.Amount
import com.unionbankph.corporate.bills_payment.data.model.Field
import com.unionbankph.corporate.bills_payment.data.model.FrequentBiller
import com.unionbankph.corporate.branch.data.model.BranchVisitDto
import com.unionbankph.corporate.mcd.data.model.CheckDeposit
import com.unionbankph.corporate.common.data.model.ContextualClassStatus
import com.unionbankph.corporate.common.data.model.PagedDto
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.fund_transfer.data.model.BankDetails
import com.unionbankph.corporate.fund_transfer.data.model.Beneficiary
import com.unionbankph.corporate.mcd.presentation.constant.CheckDepositStatusEnum
import java.util.*
import javax.inject.Inject

/**
 * Created by herald25santos on 11/04/2019
 */
class TestDataTutorial
@Inject constructor(
    private val viewUtil: ViewUtil
) {

    fun getFundTransferTransactions(): PagedDto<Transaction> {
        return PagedDto(
            results = mutableListOf(
                Transaction(
                    id = "0001",
                    channel = "PESONet",
                    remarks = "Supplier A Payment",
                    currency = "PHP",
                    immediate = false,
                    proposedTransactionDate = "2019-03-28 20:25:00.435",
                    startDate = "2019-03-28 20:25:00.435",
                    transactionStatus = ContextualClassStatus(
                        type = Constant.STATUS_FOR_APPROVAL,
                        description = Constant.PENDING_APPROVAL,
                        contextualClass = Constant.ContextualClass.WARNING
                    ),
                    approvalStatus = "IN_PROGRESS",
                    totalAmount = "50000.00",
                    createdBy = "Andrew Davis",
                    createdDate = viewUtil.getCurrentDateString(),
                    postedDate = viewUtil.getCurrentDateString(),
                    sourceAccountNumber = "000770008690",
                    beneficiaryName = "AAA Corporation",
                    batchType = Constant.TYPE_SINGLE,
                    numberOfTransactions = "1"
                )
            ),
            hasNextPage = false,
            totalElements = 3,
            currentPage = 0,
            pageSize = 1,
            totalPages = 1
        )
    }

    fun getBillsPaymentTransactions(): PagedDto<Transaction> {
        return PagedDto(
            results = mutableListOf(
                Transaction(
                    id = "0001",
                    channel = "Bills Payment",
                    remarks = "April 2019 Water Bill",
                    currency = "PHP",
                    immediate = false,
                    proposedTransactionDate = "2019-03-28 20:25:00.435",
                    startDate = "2019-03-28 20:25:00.435",
                    transactionStatus = ContextualClassStatus(
                        type = Constant.STATUS_FOR_APPROVAL,
                        description = Constant.PENDING_APPROVAL,
                        contextualClass = Constant.ContextualClass.WARNING
                    ),
                    approvalStatus = "IN_PROGRESS",
                    totalAmount = "250000.00",
                    createdBy = "Andrew Davis",
                    billerName = "ABC Water Corporation",
                    createdDate = getTransactionDate(ViewUtil.DATE_FORMAT_ISO_WITHOUT_T),
                    postedDate = getTransactionDate(ViewUtil.DATE_FORMAT_ISO_DATE),
                    sourceAccountNumber = "000770008690",
                    beneficiaryName = "AAA Corporation",
                    batchType = Constant.TYPE_SINGLE,
                    numberOfTransactions = "1"
                )
            ),
            hasNextPage = false,
            totalElements = 3,
            currentPage = 0,
            pageSize = 1,
            totalPages = 1
        )
    }

    fun getCheckDepositTransactions(): PagedDto<CheckDeposit> {
        return PagedDto(
            results = mutableListOf(
                CheckDeposit(
                    id = "0001",
                    issuer = "UnionBank of the Philippines",
                    sourceAccount = "000770008690",
                    sourceAccountName = "AAA Corporation",
                    targetAccount = "000770008690",
                    targetAccountName = "AAA Corporation",
                    checkNumber = "123457891",
                    currency = "PHP",
                    checkDate = "2019-03-28 20:25:00.435",
                    status = ContextualClassStatus(
                        type = CheckDepositStatusEnum.CLEARED.name,
                        description = CheckDepositStatusEnum.CLEARED.value,
                        contextualClass = Constant.ContextualClass.SUCCESS
                    ),
                    checkAmount = Amount(
                        "PHP",
                        250000.00
                    ),
                    createdBy = "Andrew Davis",
                    createdDate = getTransactionDate(ViewUtil.DATE_FORMAT_ISO_WITHOUT_T)
                )
            ),
            hasNextPage = false,
            totalElements = 3,
            currentPage = 0,
            pageSize = 1,
            totalPages = 1
        )
    }

    fun getBranchVisits(): PagedDto<BranchVisitDto> {
        return PagedDto(
            results = mutableListOf(),
            hasNextPage = false,
            totalElements = 3,
            currentPage = 0,
            pageSize = 1,
            totalPages = 1
        )
    }

    fun getScheduledTransferTutorialTestData(): PagedDto<Transaction> {
        return PagedDto(
            results = mutableListOf(
                Transaction(
                    id = "0001",
                    channel = "PESONet",
                    remarks = "Supplier A Payment",
                    currency = "PHP",
                    immediate = false,
                    proposedTransactionDate = "2019-03-28 20:25:00.435",
                    transactionStatus = ContextualClassStatus(
                        type = Constant.STATUS_FOR_APPROVAL,
                        description = Constant.PENDING_APPROVAL,
                        contextualClass = Constant.ContextualClass.WARNING
                    ),
                    startDate = "2018-08-27 08:59:14.435",
                    endDate = "2018-09-27 08:59:14.435",
                    frequency = "MONTHLY",
                    approvalStatus = "IN_PROGRESS",
                    totalAmount = "50000.00",
                    createdBy = "Andrew Davis",
                    createdDate = getTransactionDate(ViewUtil.DATE_FORMAT_ISO_WITHOUT_T),
                    postedDate = getTransactionDate(ViewUtil.DATE_FORMAT_ISO_DATE),
                    sourceAccountNumber = "000770008690",
                    beneficiaryName = "AAA Corporation",
                    batchType = Constant.TYPE_SINGLE,
                    numberOfTransactions = "1"
                )
            ),
            hasNextPage = false,
            totalElements = 3,
            currentPage = 0,
            pageSize = 1,
            totalPages = 1
        )
    }

    fun getBeneficiariesTestData(): PagedDto<Beneficiary> {
        return PagedDto(
            results = mutableListOf(
                Beneficiary(
                    id = 374140,
                    name = "Supplier X",
                    code = "SX01",
                    createdBy = "Glenn Dizon",
                    createdDate = getTransactionDate(ViewUtil.DATE_FORMAT_ISO_WITHOUT_T),
                    channelId = 4,
                    accountNumber = "123412341234",
                    bankDetails = BankDetails("UnionBank of the Philippines")
                )
            ),
            hasNextPage = false,
            totalElements = 3,
            currentPage = 0,
            pageSize = 1,
            totalPages = 1
        )
    }

    fun getfrequentBillersTestData(): PagedDto<FrequentBiller> {
        return PagedDto(
            results = mutableListOf(
                FrequentBiller(
                    id = "8b1e776c-deac-452c-9c27-409d7e0165e4",
                    name = "Electricity",
                    code = "G0544",
                    billerName = "XYZ Electricity Corporation",
                    createdDate = getTransactionDate(ViewUtil.DATE_FORMAT_ISO_WITHOUT_T),
                    createdBy = "Samantha Adams",
                    accountNumber = "123412341234",
                    serviceId = "7700",
                    fields = mutableListOf(
                        Field(
                            3758768,
                            1,
                            "Billing No.",
                            "UPD5525120A"
                        ),
                        Field(
                            3758769,
                            2,
                            "Customer ID",
                            "76238812"
                        ),
                        Field(
                            3758770,
                            3,
                            "Charge Code",
                            "CHARGECODE0001"
                        )
                    )
                )
            ),
            hasNextPage = false,
            totalElements = 3,
            currentPage = 0,
            pageSize = 1,
            totalPages = 1
        )
    }

    // generate transaction date
    private fun getTransactionDate(format: String): String {
        val calendarPosted = Calendar.getInstance()
        return viewUtil.getDateFormatByTimeMilliSeconds(calendarPosted.timeInMillis, format)
    }
}
