package com.unionbankph.corporate.instapay_qr.data.source.api

import com.unionbankph.corporate.instapay_qr.domain.model.form.GenerateFTForm
import com.unionbankph.corporate.instapay_qr.domain.model.response.GenerateFTResponse
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.*

interface InstapayQrApiClient {

    @POST("/msme/api/{api_version}/qrcode/parse")
    fun generateFT(
        @Header("Authorization")
        accessToken: String,
        @Header("x-client-id")
        clientId: String,
        @Header("x-client-secret")
        clientSecret: String,
        @Path("api_version")
        apiVersion: String,
        @Body
        generateFTForm: GenerateFTForm
    ): Single<Response<GenerateFTResponse>>
}