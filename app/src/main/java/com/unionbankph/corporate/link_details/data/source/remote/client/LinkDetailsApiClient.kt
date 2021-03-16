package com.unionbankph.corporate.link_details.data.source.remote.client

import com.unionbankph.corporate.bills_payment.data.model.BillsPaymentVerify
import com.unionbankph.corporate.link_details.data.LinkDetailsResponse
import com.unionbankph.corporate.link_details.data.form.LinkDetailsForm
import com.unionbankph.corporate.request_payment_link.data.form.RequestPaymentForm
import com.unionbankph.corporate.request_payment_link.data.model.RequestPaymentLinkResponse
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface LinkDetailsApiClient {

    @POST("api/{api_version}/organizations/13b8b6af-7d34-487c-ad26-333b3fff3671/payment-links")
    fun generatePaymentLink(
//            @Header("Authorization")
//            accessToken: String,
            @Path("api_version")
            apiVersion: String,
            @Body
            linkDetailsForm: LinkDetailsForm
    ): Single<Response<LinkDetailsResponse>>
}