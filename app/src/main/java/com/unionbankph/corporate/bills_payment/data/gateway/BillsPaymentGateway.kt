package com.unionbankph.corporate.bills_payment.data.gateway

import com.unionbankph.corporate.approval.data.model.Transaction
import com.unionbankph.corporate.auth.data.form.ResendOTPForm
import com.unionbankph.corporate.auth.data.model.Auth
import com.unionbankph.corporate.bills_payment.data.form.BillsPaymentForm
import com.unionbankph.corporate.bills_payment.data.form.CancelBillsPaymentTransactionForm
import com.unionbankph.corporate.bills_payment.data.form.FrequentBillerForm
import com.unionbankph.corporate.bills_payment.data.model.Biller
import com.unionbankph.corporate.bills_payment.data.model.BillerField
import com.unionbankph.corporate.bills_payment.data.model.BillsPaymentValidate
import com.unionbankph.corporate.bills_payment.data.model.BillsPaymentVerify
import com.unionbankph.corporate.bills_payment.data.model.CancelBillsPaymentTransactionResponse
import com.unionbankph.corporate.bills_payment.data.model.CreationFrequentBillerDto
import com.unionbankph.corporate.bills_payment.data.model.FrequentBiller
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.Message
import com.unionbankph.corporate.common.data.model.PagedDto
import com.unionbankph.corporate.fund_transfer.data.model.ActivityLogDto
import io.reactivex.Single

/**
 * Created by herald25santos on 2020-01-16
 */
interface BillsPaymentGateway {

    fun getOrganizationPayments(
        startDate: String?,
        endDate: String?,
        status: String?,
        biller: String?,
        pageable: Pageable
    ): Single<PagedDto<Transaction>>

    fun getBillsPaymentDetail(id: String): Single<Transaction>

    fun getBillsPaymentActivityLogs(id: String): Single<MutableList<ActivityLogDto>>

    fun validateBillsPayment(billsPaymentForm: BillsPaymentForm): Single<BillsPaymentValidate>

    fun billsPayment(billsPaymentForm: BillsPaymentForm): Single<BillsPaymentVerify>

    fun billsPaymentOTP(otp: HashMap<String, String>): Single<BillsPaymentVerify>

    fun resendOTPBillsPayment(resendOTPForm: ResendOTPForm): Single<Auth>

    fun getOrganizationPaymentsTestData(): Single<PagedDto<Transaction>>

    fun cancelBillsPaymentTransaction(
        cancelBillsPaymentTransactionForm: CancelBillsPaymentTransactionForm
    ): Single<CancelBillsPaymentTransactionResponse>

    /* BILLERS */
    fun getBillers(type: String): Single<MutableList<Biller>>

    fun getBillerFields(billerId: String): Single<MutableList<BillerField>>

    /* FREQUENT BILLERS */
    fun getFrequentBillers(
        accountId: String?,
        pageable: Pageable
    ): Single<PagedDto<FrequentBiller>>

    fun getFrequentBillerDetail(id: String): Single<FrequentBiller>

    fun getFrequentBillerActivityLogs(id: String): Single<MutableList<ActivityLogDto>>

    fun getFrequentBillersTestData(): Single<PagedDto<FrequentBiller>>

    fun createFrequentBiller(frequentBillerForm: FrequentBillerForm): Single<CreationFrequentBillerDto>

    fun updateFrequentBiller(
        id: String,
        frequentBillerForm: FrequentBillerForm
    ): Single<CreationFrequentBillerDto>

    fun deleteFrequentBiller(id: String): Single<Message>
}
