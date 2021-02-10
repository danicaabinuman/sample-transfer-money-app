package com.unionbankph.corporate.bills_payment.data.source.remote

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
import retrofit2.Response

/**
 * Created by herald25santos on 2020-01-16
 */
interface BillsPaymentRemote {

    fun getOrganizationPayments(
        accessToken: String,
        startDate: String?,
        endDate: String?,
        status: String?,
        biller: String?,
        pageable: Pageable
    ): Single<Response<PagedDto<Transaction>>>

    fun getBillsPaymentDetail(accessToken: String, id: String): Single<Response<Transaction>>

    fun getBillsPaymentActivityLogs(
        accessToken: String,
        id: String
    ): Single<Response<MutableList<ActivityLogDto>>>

    fun validateBillsPayment(
        accessToken: String,
        billsPaymentForm: BillsPaymentForm
    ): Single<Response<BillsPaymentValidate>>

    fun billsPayment(
        accessToken: String,
        billsPaymentForm: BillsPaymentForm
    ): Single<Response<BillsPaymentVerify>>

    fun billsPaymentOTP(
        accessToken: String,
        otp: HashMap<String, String>
    ): Single<Response<BillsPaymentVerify>>

    fun resendOTPBillsPayment(
        accessToken: String,
        resendOTPForm: ResendOTPForm
    ): Single<Response<Auth>>

    /* BILLERS */
    fun getBillers(accessToken: String, type: String): Single<Response<MutableList<Biller>>>

    fun getBillerFields(
        accessToken: String,
        billerId: String
    ): Single<Response<MutableList<BillerField>>>

    /* FREQUENT BILLERS */
    fun getFrequentBillers(
        accessToken: String,
        accountId: String?,
        pageable: Pageable
    ): Single<Response<PagedDto<FrequentBiller>>>

    fun getFrequentBillerDetail(accessToken: String, id: String): Single<Response<FrequentBiller>>

    fun getFrequentBillerActivityLogs(
        accessToken: String,
        id: String
    ): Single<Response<MutableList<ActivityLogDto>>>

    fun createFrequentBiller(
        accessToken: String,
        frequentBillerForm: FrequentBillerForm
    ): Single<Response<CreationFrequentBillerDto>>

    fun updateFrequentBiller(
        accessToken: String,
        id: String,
        frequentBillerForm: FrequentBillerForm
    ): Single<Response<CreationFrequentBillerDto>>

    fun deleteFrequentBiller(accessToken: String, id: String): Single<Response<Message>>

    fun cancelBillsPaymentTransaction(
        accessToken: String,
        cancelBillsPaymentTransactionForm: CancelBillsPaymentTransactionForm
    ): Single<Response<CancelBillsPaymentTransactionResponse>>
}
