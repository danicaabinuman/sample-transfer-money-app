package com.unionbankph.corporate.account.data.source.remote.client

import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.account.data.model.AccountTransactionHistoryDetails
import com.unionbankph.corporate.account.data.model.RecentTransaction
import com.unionbankph.corporate.account.domain.form.GetAccountsBalances
import com.unionbankph.corporate.common.data.model.PagedDto
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.*

/**
 * Created by herald25santos on 2020-01-14
 */
interface AccountApiClient {

    @GET("api/{api_version}/corporate-user-profile/accounts")
    fun getAccounts(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String
    ): Single<Response<MutableList<Account>>>

    @GET("api/{api_version}/corporate-user-profile/accounts")
    fun getAccountsPermission(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Query("channel-id")
        channelId: String?,
        @Query("permission-id")
        permissionId: String?,
        @Query("except-currency")
        exceptCurrency: String?
    ): Single<Response<MutableList<Account>>>


    @GET("api/{api_version}/corporate-user-profile/accounts")
    fun getAccountsPaginated(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Query("channel-id")
        channelId: String?,
        @Query("permission-id")
        permissionId: String?,
        @Query("except-currency")
        exceptCurrency: String?,
        @Query("except-account")
        exceptAccount: String?,
        @Query("page")
        page: Int,
        @Query("size")
        size: Int,
        @Query("filter")
        filter: String?
    ): Single<Response<PagedDto<Account>>>

    @POST("api/{api_version}/corporate-user-profile/accounts/balance-inquiry")
    fun getAccountsBalances(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Body
        getAccountsBalances: GetAccountsBalances
    ): Single<Response<MutableList<Account>>>

    @GET("api/{api_version}/corporate-user-profile/accounts/{id}")
    fun getCorporateUserAccountDetail(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Path("id")
        id: String
    ): Single<Response<Account>>

    @GET("api/{api_version}/corporate-user-profile/accounts/{id}/transactions")
    fun getRecentTransactionsWithPagination(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Path("id")
        id: String,
        @Query("limit")
        limit: Int
    ): Single<Response<RecentTransaction>>

    @GET("api/{api_version}/corporate-user-profile/accounts/{id}/transactions")
    fun getRecentTransactions(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Path("id")
        id: String
    ): Single<Response<RecentTransaction>>

    @GET("api/{api_version}/corporate-user-profile/accounts/{id}/transactions")
    fun getRecentTransactions(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Path("id")
        id: String,
        @Query("last-tran-id")
        lastTranId: String?,
        @Query("running-balance")
        runningBalance: String?,
        @Query("last-posted-date")
        lastPostedDate: String?,
        @Query("last-serial-no")
        lastSerialNo: String?,
        @Query("page")
        page: Int,
        @Query("limit")
        size: Int
    ): Single<Response<RecentTransaction>>

    @GET("api/{api_version}/corporate-user-profile/accounts/{id}/transactions/{reference-number}")
    fun getAccountTransactionHistoryDetails(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Path("id")
        id: String,
        @Path("reference-number")
        referenceNumber: String,
        @Query("serialNo")
        serialNo: String?,
        @Query("transactionDate")
        transactionDate: String?
    ): Single<Response<AccountTransactionHistoryDetails>>

}
