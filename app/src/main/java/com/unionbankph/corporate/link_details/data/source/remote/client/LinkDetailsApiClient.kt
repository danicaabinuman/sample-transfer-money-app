package com.unionbankph.corporate.link_details.data.source.remote.client

import com.unionbankph.corporate.link_details.data.LinkDetailsResponse
import com.unionbankph.corporate.link_details.data.form.LinkDetailsForm
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface LinkDetailsApiClient {

    @POST("msme/api/{api_version}/organizations/6460b955-1a2e-4662-9bc3-b762c6726793/payment-links")
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
        linkDetailsForm: LinkDetailsForm
    ): Single<Response<LinkDetailsResponse>>
}