package com.unionbankph.corporate.bills_payment.data.source.remote.client

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
import com.unionbankph.corporate.common.data.model.Message
import com.unionbankph.corporate.common.data.model.PagedDto
import com.unionbankph.corporate.fund_transfer.data.model.ActivityLogDto
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Created by herald25santos on 2020-01-16
 */
interface BillsPaymentApiClient {

    @POST("api/{api_version}/bills-payments/submit")
    fun submitBillsPayment(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Body
        billsPaymentForm: BillsPaymentForm
    ): Single<Response<BillsPaymentVerify>>

    @POST("api/{api_version}/bills-payments/verify")
    fun billsPaymentOTP(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Body
        otp: HashMap<String, String>
    ): Single<Response<BillsPaymentVerify>>

    @POST("api/{api_version}/bills-payments/verify/otp/resend")
    fun resendOTPBillsPayment(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Body
        resendOTPForm: ResendOTPForm
    ): Single<Response<Auth>>

    @GET("api/{api_version}/bills-payments/billers/{biller_id}/fields")
    fun getBillerFields(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Path("biller_id")
        billerId: String
    ): Single<Response<MutableList<BillerField>>>

    @GET("api/{api_version}/bills-payments/billers")
    fun getBillers(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Query("type")
        type: String
    ): Single<Response<MutableList<Biller>>>

    @GET("api/{api_version}/frequent-billers")
    fun getFrequentBillers(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Query("account-id")
        accountId: String?,
        @Query("page")
        page: Int,
        @Query("size")
        size: Int,
        @Query("filter")
        filter: String?
    ): Single<Response<PagedDto<FrequentBiller>>>

    @GET("api/{api_version}/transactions/bills-payments/{id}")
    fun getBillsPaymentDetail(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Path("id")
        id: String
    ): Single<Response<Transaction>>

    @GET("api/{api_version}/transactions/bills-payments")
    fun getOrganizationPayments(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Query("page")
        page: Int,
        @Query("size")
        size: Int,
        @Query("filter")
        filter: String?,
        @Query("start-date")
        startDate: String?,
        @Query("end-date")
        endDate: String?,
        @Query("status")
        status: String?,
        @Query("biller")
        biller: String?
    ): Single<Response<PagedDto<Transaction>>>

    @GET("api/{api_version}/transactions/bills-payments/{id}/logs")
    fun getBillsPaymentActivityLogs(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Path("id")
        id: String
    ): Single<Response<MutableList<ActivityLogDto>>>

    @POST("api/{api_version}/frequent-billers")
    fun createFrequentBiller(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Body
        frequentBillerForm: FrequentBillerForm
    ): Single<Response<CreationFrequentBillerDto>>

    @PUT("api/{api_version}/frequent-billers/{id}")
    fun updateFrequentBiller(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Path("id")
        id: String,
        @Body
        frequentBillerForm: FrequentBillerForm
    ): Single<Response<CreationFrequentBillerDto>>

    @GET("api/{api_version}/frequent-billers/{id}")
    fun getFrequentBillerDetail(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Path("id")
        id: String
    ): Single<Response<FrequentBiller>>

    @DELETE("api/{api_version}/frequent-billers/{id}")
    fun deleteFrequentBiller(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Path("id")
        id: String
    ): Single<Response<Message>>

    @GET("api/{api_version}/frequent-billers/{id}/logs")
    fun getFrequentBillerActivityLogs(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Path("id")
        id: String
    ): Single<Response<MutableList<ActivityLogDto>>>

    @POST("api/{api_version}/bills-payments/validate")
    fun validateBillsPayment(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Body
        billsPaymentForm: BillsPaymentForm
    ): Single<Response<BillsPaymentValidate>>

    @POST("api/{api_version}/bills-payments/cancel")
    fun cancelFundTransferTransaction(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Body
        cancelBillsPaymentTransactionForm: CancelBillsPaymentTransactionForm
    ): Single<Response<CancelBillsPaymentTransactionResponse>>
}
