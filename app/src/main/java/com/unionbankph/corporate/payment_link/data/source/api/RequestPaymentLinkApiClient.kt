package com.unionbankph.corporate.payment_link.data.source.api

import com.unionbankph.corporate.payment_link.domain.model.form.RequestPaymentForm
import com.unionbankph.corporate.payment_link.domain.model.response.RequestPaymentLinkResponse
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface RequestPaymentLinkApiClient {

    @POST("api/{api_version}/organizations/13b8b6af-7d34-487c-ad26-333b3fff3671/payment-links")
    fun generatePaymentLink(
//            @Header("Authorization")
//            accessToken: String,
            @Path("api_version")
            apiVersion: String,
            @Body
            requestPaymentForm: RequestPaymentForm
    ): Single<Response<RequestPaymentLinkResponse>>
}