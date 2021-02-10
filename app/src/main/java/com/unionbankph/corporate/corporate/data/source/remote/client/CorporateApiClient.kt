package com.unionbankph.corporate.corporate.data.source.remote.client

import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.auth.data.model.Role
import com.unionbankph.corporate.common.data.model.Message
import com.unionbankph.corporate.corporate.data.form.ValidateAccountNumberForm
import com.unionbankph.corporate.corporate.data.model.Channel
import com.unionbankph.corporate.corporate.data.model.ChannelDto
import com.unionbankph.corporate.corporate.data.model.CorporateUsers
import com.unionbankph.corporate.corporate.data.model.TransactionStatusDto
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CorporateApiClient {

    @GET("api/{api_version}/corporate-users/organizations")
    fun getCorporateUserOrganization(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String
    ): Single<Response<MutableList<CorporateUsers>>>

    @GET("api/{api_version}/corporate-user-profile/accounts/{id}")
    fun getCorporateUserAccountDetail(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Path("id")
        id: String
    ): Single<Response<Account>>

    @GET("api/{api_version}/organizations/{id}/channels")
    fun getChannels(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Path("id")
        id: String,
        @Query("product-id")
        productId: String
    ): Single<Response<MutableList<Channel>>>

    @POST("api/{api_version}/corporate-user-profile/permissions")
    fun switchOrganization(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Body
        switchOrgParams: HashMap<String, String>
    ): Single<Response<Role>>

    @POST("api/{api_version}/accounts/validate")
    fun validateAccountNumber(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Body
        validateAccountNumberForm: ValidateAccountNumberForm
    ): Single<Response<Message>>

    @GET("api/{api_version}/transaction-statuses")
    fun getTransactionStatuses(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Query("product-id")
        productId: String?
    ): Single<Response<MutableList<TransactionStatusDto>>>

    @GET("api/{api_version}/channels/lite")
    fun getChannelsLite(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Query("product-id")
        productId: String?
    ): Single<Response<MutableList<ChannelDto>>>
}
