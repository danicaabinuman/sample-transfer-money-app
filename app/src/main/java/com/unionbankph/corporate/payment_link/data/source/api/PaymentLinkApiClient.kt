package com.unionbankph.corporate.payment_link.data.source.api

import com.unionbankph.corporate.payment_link.domain.model.form.CreateMerchantForm
import com.unionbankph.corporate.payment_link.domain.model.form.GeneratePaymentLinkForm
import com.unionbankph.corporate.payment_link.domain.model.form.PutPaymentLinkStatusForm
import com.unionbankph.corporate.payment_link.domain.model.form.UpdateSettlementOnRequestPaymentForm
import com.unionbankph.corporate.payment_link.domain.model.response.*
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.*


interface PaymentLinkApiClient {

    @POST("msme/api/{api_version}/payment-links")
    fun generatePaymentLink(
        @Header("Authorization")
        accessToken: String,
        @Header("x-client-id")
        clientId: String,
        @Header("x-client-secret")
        clientSecret: String,
        @Path("api_version")
        apiVersion: String,
        @Body
        generatePaymentLinkForm: GeneratePaymentLinkForm
    ): Single<Response<GeneratePaymentLinkResponse>>

    @POST("msme/api/{api_version}/merchant")
    fun createMerchant(
        @Header("Authorization")
        accessToken: String,
        @Header("x-client-id")
        clientId: String,
        @Header("x-client-secret")
        clientSecret: String,
        @Path("api_version")
        apiVersion: String,
        @Body
        createMerchantForm: CreateMerchantForm
    ): Single<Response<CreateMerchantResponse>>

    @PUT("msme/api/{api_version}/merchant/accounts")
    fun updateSettlementOnRequestPayment(
        @Header("Authorization")
        accessToken: String,
        @Header("x-client-id")
        clientId: String,
        @Header("x-client-secret")
        clientSecret: String,
        @Path("api_version")
        apiVersion: String,
        @Body
        updateSettlementOnRequestPaymentForm: UpdateSettlementOnRequestPaymentForm
    ): Single<Response<UpdateSettlementOnRequestPaymentResponse>>

    @GET("msme/api/{api_version}/payment-links")
    fun getPaymentLinkListPaginated(
        @Header("Authorization")
        accessToken: String,
        @Header("x-client-id")
        clientId: String,
        @Header("x-client-secret")
        clientSecret: String,
        @Path("api_version")
        apiVersion: String,
        @Query("page")
        page: String,
        @Query("items-per-page")
        itemsPerPage: String
    ): Single<Response<GetPaymentLinkListPaginatedResponse>>

    @GET("msme/api/{api_version}/payment-links")
    fun getPaymentLinkByReferenceNumber(
            @Header("Authorization")
            accessToken: String,
            @Header("x-client-id")
            clientId: String,
            @Header("x-client-secret")
            clientSecret: String,
            @Path("api_version")
            apiVersion: String,
            @Query("page")
            page: String,
            @Query("items-per-page")
            itemsPerPage: String,
            @Query("reference-number")
            referenceNumber: String
    ): Single<Response<GetPaymentLinkListPaginatedResponse>>

    @GET("msme/api/{api_version}/payment-link/{reference_id}")
    fun getPaymentLinkByReferenceId(
        @Header("Authorization")
        accessToken: String,
        @Header("x-client-id")
        clientId: String,
        @Header("x-client-secret")
        clientSecret: String,
        @Path("api_version")
        apiVersion: String,
        @Path("reference_id")
        referenceId: String
    ): Single<Response<GetPaymentLinkByReferenceIdResponse>>

    @PUT("msme/api/{api_version}/payment/{transaction_id}/status")
    fun putPaymentLinkStatus(
            @Header("Authorization")
            accessToken: String,
            @Header("x-client-id")
            clientId: String,
            @Header("x-client-secret")
            clientSecret: String,
            @Path("api_version")
            apiVersion: String,
            @Path("transaction_id")
            transaction_id: String,
            @Body
            putPaymentLinkStatusForm: PutPaymentLinkStatusForm
    ): Single<Response<PutPaymentLinkStatusResponse>>

    @GET("msme/api/{api_version}/merchant")
    fun validateMerchantByOrganization(
        @Header("Authorization")
        accessToken: String,
        @Header("x-client-id")
        clientId: String,
        @Header("x-client-secret")
        clientSecret: String,
        @Path("api_version")
        apiVersion: String
    ): Single<Response<ValidateMerchantByOrganizationResponse>>

    @GET("msme/api/{api_version}/payment-logs/{reference_id}")
    fun getPaymentLogs(
        @Header("Authorization")
        accessToken: String,
        @Header("x-client-id")
        clientId: String,
        @Header("x-client-secret")
        clientSecret: String,
        @Path("api_version")
        apiVersion: String,
        @Path("reference_id")
        referenceId: String
    ): Single<Response<GetPaymentLogsResponse>>
}
