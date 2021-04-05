package com.unionbankph.corporate.request_payment_link.data.source.remote.client

import com.unionbankph.corporate.link_details.data.LinkDetailsResponse
import com.unionbankph.corporate.link_details.data.form.LinkDetailsForm
import com.unionbankph.corporate.request_payment_link.data.form.CreateMerchantForm
import com.unionbankph.corporate.request_payment_link.data.model.CreateMerchantResponse
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface CreateMerchantApiClient {

    @POST("msme/api/{api_version}/merchant")
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
        createMerchantForm: CreateMerchantForm
    ): Single<Response<CreateMerchantResponse>>
}