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
}