package com.unionbankph.corporate.mcd.data.source.remote.client

import com.unionbankph.corporate.common.data.model.PagedDto
import com.unionbankph.corporate.fund_transfer.data.model.Bank
import com.unionbankph.corporate.mcd.data.form.CheckDepositForm
import com.unionbankph.corporate.mcd.data.model.CheckDeposit
import com.unionbankph.corporate.mcd.data.model.CheckDepositActivityLog
import com.unionbankph.corporate.mcd.data.model.CheckDepositUpload
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface CheckDepositApiClient {

    @POST("api/{api_version}/mobile-check-deposit/submit")
    fun checkDeposit(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Body
        checkDepositForm: CheckDepositForm
    ): Single<Response<CheckDeposit>>

    @POST("api/{api_version}/mobile-check-deposit/upload")
    fun checkDepositUploadFile(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Query("sideOfCheck")
        sideOfCheck: String,
        @Query("id")
        id: String?,
        @Body
        requestBody: RequestBody
    ): Single<Response<CheckDepositUpload>>

    @GET("api/{api_version}/mobile-check-deposit")
    fun getCheckDeposits(
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
        @Query("check-number")
        checkNumber: String?,
        @Query("amount")
        amount: String?,
        @Query("date-on-check-from")
        dateOnCheckFrom: String?,
        @Query("date-on-check-to")
        dateOnCheckTo: String?,
        @Query("deposit-account")
        depositAccount: String?,
        @Query("status")
        status: String?,
        @Query("date-created-from")
        dateCreatedFrom: String?,
        @Query("date-created-to")
        dateCreatedTo: String?
    ): Single<Response<PagedDto<CheckDeposit>>>

    @GET("api/{api_version}/mobile-check-deposit/{id}")
    fun getCheckDeposit(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Path("id")
        id: String
    ): Single<Response<CheckDeposit>>

    @GET("api/{api_version}/mobile-check-deposit/{id}/logs")
    fun getCheckDepositActivityLogs(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Path("id")
        id: String
    ): Single<Response<MutableList<CheckDepositActivityLog>>>

    @GET("api/{api_version}/fund-transfer-library/banks")
    fun getCheckDepositBanks(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Query("remitType")
        remitType: String?
    ): Single<Response<MutableList<Bank>>>

}
