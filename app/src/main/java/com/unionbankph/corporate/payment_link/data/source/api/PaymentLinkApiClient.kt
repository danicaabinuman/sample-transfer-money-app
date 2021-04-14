package com.unionbankph.corporate.payment_link.data.source.api

import com.unionbankph.corporate.payment_link.domain.model.form.CreateMerchantForm
import com.unionbankph.corporate.payment_link.domain.model.form.LinkDetailsForm
import com.unionbankph.corporate.payment_link.domain.model.response.CreateMerchantResponse
import com.unionbankph.corporate.payment_link.domain.model.response.LinkDetailsResponse
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.*


interface PaymentLinkApiClient {

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


    @POST("msme/api/{api_version}/organizations/{organization_id}/payment-links")
    fun generatePaymentLink(
        @Header("Authorization")
        accessToken: String,
        @Header("x-client-id")
        clientId: String,
        @Header("x-client-secret")
        clientSecret: String,
        @Path("api_version")
        apiVersion: String,
        @Path("organization_id")
        organizationId: String,
        @Body
        linkDetailsForm: LinkDetailsForm
    ): Single<Response<LinkDetailsResponse>>


    @GET("msme/api/{api_version}/organizations/{organization_id}/payment-links")
    fun getPaymentLinkList(
        @Header("Authorization")
        accessToken: String,
        @Header("x-client-id")
        clientId: String,
        @Header("x-client-secret")
        clientSecret: String,
        @Path("api_version")
        apiVersion: String,
        @Path("organization_id")
        organizationId: String,
        @Query("page")
        page: String,
        @Query("items-per-page")
        itemsPerPage: String,
        @Query("reference-number")
        referenceNumber: String
    ): Single<Response<LinkDetailsResponse>>

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
    ): Single<Response<LinkDetailsResponse>>



}
