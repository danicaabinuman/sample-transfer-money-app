package com.unionbankph.corporate.fund_transfer.data.source.remote.client

import com.unionbankph.corporate.approval.data.model.Transaction
import com.unionbankph.corporate.auth.data.form.ResendOTPForm
import com.unionbankph.corporate.auth.data.model.Auth
import com.unionbankph.corporate.common.data.model.Message
import com.unionbankph.corporate.common.data.model.PagedDto
import com.unionbankph.corporate.fund_transfer.data.form.BeneficiaryForm
import com.unionbankph.corporate.fund_transfer.data.form.CancelFundTransferTransactionForm
import com.unionbankph.corporate.fund_transfer.data.form.FundTransferInstaPayForm
import com.unionbankph.corporate.fund_transfer.data.form.FundTransferPesoNetForm
import com.unionbankph.corporate.fund_transfer.data.form.FundTransferSwiftForm
import com.unionbankph.corporate.fund_transfer.data.form.FundTransferUBPForm
import com.unionbankph.corporate.fund_transfer.data.model.ActivityLogDto
import com.unionbankph.corporate.fund_transfer.data.model.Bank
import com.unionbankph.corporate.fund_transfer.data.model.Batch
import com.unionbankph.corporate.fund_transfer.data.model.Beneficiary
import com.unionbankph.corporate.fund_transfer.data.model.BeneficiaryDetailDto
import com.unionbankph.corporate.fund_transfer.data.model.CWTDetail
import com.unionbankph.corporate.fund_transfer.data.model.CancelFundTransferTransactionResponse
import com.unionbankph.corporate.fund_transfer.data.model.CreationBeneficiaryDto
import com.unionbankph.corporate.fund_transfer.data.model.FundTransferVerify
import com.unionbankph.corporate.fund_transfer.data.model.Purpose
import com.unionbankph.corporate.fund_transfer.data.model.ScheduledTransferDeletionForm
import com.unionbankph.corporate.fund_transfer.data.model.SwiftBank
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
 * Created by herald25santos on 2020-01-15
 */
interface FundTransferApiClient {

    @POST("api/{api_version}/fund-transfers/verify")
    fun fundTransferOTP(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Body
        otp: HashMap<String, String>
    ): Single<Response<FundTransferVerify>>

    @POST("api/{api_version}/fund-transfers")
    fun fundTransferUBP(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Body
        fundTransferUBPForm: FundTransferUBPForm
    ): Single<Response<FundTransferVerify>>

    @POST("api/{api_version}/fund-transfers")
    fun fundTransferPesoNet(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Body
        fundTransferPesoNetForm: FundTransferPesoNetForm
    ): Single<Response<FundTransferVerify>>

    @POST("api/{api_version}/fund-transfers")
    fun fundTransferInstaPay(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Body
        fundTransferInstaPayForm: FundTransferInstaPayForm
    ): Single<Response<FundTransferVerify>>

    @POST("api/{api_version}/fund-transfers")
    fun fundTransferSwift(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Body
        fundTransferSwiftForm: FundTransferSwiftForm
    ): Single<Response<FundTransferVerify>>

    @POST("api/{api_version}/fund-transfers/verify/otp/resend")
    fun resendOTPFundTransfer(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Body
        resendOTPForm: ResendOTPForm
    ): Single<Response<Auth>>

    @GET("api/{api_version}/fund-transfer-library/purposes")
    fun getPurposes(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String
    ): Single<Response<MutableList<Purpose>>>

    @GET("api/{api_version}/fund-transfer-library/instapay/purposes")
    fun getInstaPayPurposes(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String
    ): Single<Response<MutableList<Purpose>>>

    @GET("api/{api_version}/fund-transfer-library/banks/epcs")
    fun getPesoNetBanks(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String
    ): Single<Response<MutableList<Bank>>>

    @GET("api/{api_version}/fund-transfer-library/banks/pddts")
    fun getPDDTSBanks(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String
    ): Single<Response<MutableList<Bank>>>

    @GET("api/{api_version}/fund-transfer-library/banks/instapay")
    fun getInstaPayBanks(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String
    ): Single<Response<MutableList<Bank>>>

    @GET("api/{api_version}/fund-transfer-library/swift-banks")
    fun getSwiftBanks(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Query("page")
        page: Int,
        @Query("size")
        size: Int,
        @Query("filter")
        filter: String?
    ): Single<Response<PagedDto<SwiftBank>>>

    @GET("api/{api_version}/transactions/fund-transfers/{id}")
    fun getFundTransferDetail(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Path("id")
        id: String
    ): Single<Response<Transaction>>

    @GET("api/{api_version}/transactions/fund-transfers/{id}/items")
    fun getBatchesTransaction(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Path("id")
        id: String,
        @Query("page")
        page: Int,
        @Query("size")
        size: Int
    ): Single<Response<PagedDto<Batch>>>

    @GET("api/{api_version}/transactions/fund-transfers")
    fun getOrganizationTransfers(
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
        @Query("channel-ids")
        channelIds: String?
    ): Single<Response<PagedDto<Transaction>>>

    @GET("api/{api_version}/transactions/fund-transfers/{id}/logs")
    fun getFundTransferActivityLogs(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Path("id")
        id: String
    ): Single<Response<MutableList<ActivityLogDto>>>

    @GET("api/{api_version}/transactions/batch-transfers/header/cwt")
    fun getFundTransferCWTHeader(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String
    ): Single<Response<MutableList<CWTDetail>>>

    @GET("api/{api_version}/transactions/batch-transfers/details/cwt/{id}")
    fun getFundTransferCWT(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Path("id")
        id: String,
        @Query("submitted")
        submitted: Boolean,
        @Query("page")
        page: Int?,
        @Query("size")
        size: Int?
    ): Single<Response<PagedDto<MutableList<CWTDetail>>>>

    @GET("api/{api_version}/transactions/batch-transfers/header/inv")
    fun getFundTransferINVHeader(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String
    ): Single<Response<MutableList<CWTDetail>>>

    @GET("api/{api_version}/transactions/batch-transfers/details/inv/{id}")
    fun getFundTransferINV(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Path("id")
        id: String,
        @Query("submitted")
        submitted: Boolean,
        @Query("page")
        page: Int?,
        @Query("size")
        size: Int?
    ): Single<Response<PagedDto<MutableList<CWTDetail>>>>

    @GET("api/{api_version}/beneficiary-masters/paginated")
    fun getBeneficiaries(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Query("channel-id")
        channelId: String?,
        @Query("account-id")
        accountId: String?,
        @Query("page")
        page: Int,
        @Query("size")
        size: Int,
        @Query("filter")
        filter: String?
    ): Single<Response<PagedDto<Beneficiary>>>

    @POST("api/{api_version}/beneficiary-masters")
    fun createBeneficiary(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Body
        beneficiaryForm: BeneficiaryForm
    ): Single<Response<CreationBeneficiaryDto>>

    @GET("api/{api_version}/beneficiary-masters/{id}")
    fun getBeneficiaryDetail(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Path("id")
        id: String
    ): Single<Response<BeneficiaryDetailDto>>

    @GET("api/{api_version}/beneficiary-masters/{id}/logs")
    fun getBeneficiaryDetailActivityLogs(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Path("id")
        id: String
    ): Single<Response<MutableList<ActivityLogDto>>>

    @DELETE("api/{api_version}/beneficiary-masters/{id}")
    fun deleteBeneficiary(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Path("id")
        id: String
    ): Single<Response<Message>>

    @PUT("api/{api_version}/beneficiary-masters/{id}")
    fun updateBeneficiary(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Path("id")
        id: String,
        @Body
        beneficiaryForm: BeneficiaryForm
    ): Single<Response<CreationBeneficiaryDto>>

    @GET("api/{api_version}/transactions/fund-transfers/scheduled")
    fun getManageScheduledTransfers(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Query("status")
        status: String,
        @Query("page")
        page: Int,
        @Query("size")
        size: Int,
        @Query("filter")
        filter: String?
    ): Single<Response<PagedDto<Transaction>>>

    @POST("api/{api_version}/fund-transfers/scheduled/cancel")
    fun deleteScheduledTransfer(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Body
        scheduledTransferDeletionForm: ScheduledTransferDeletionForm
    ): Single<Response<Message>>

    @POST("api/{api_version}/fund-transfers/cancel")
    fun cancelFundTransferTransaction(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Body
        cancelFundTransferTransactionForm: CancelFundTransferTransactionForm
    ): Single<Response<CancelFundTransferTransactionResponse>>
}
