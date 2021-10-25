package com.unionbankph.corporate.instapay_qr.data.source.remote

import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.instapay_qr.data.source.api.InstapayQrApiClient
import com.unionbankph.corporate.instapay_qr.domain.model.form.GenerateFTForm
import com.unionbankph.corporate.instapay_qr.domain.model.response.GenerateFTResponse
import io.reactivex.Single
import retrofit2.Response
import retrofit2.Retrofit
import javax.inject.Inject

class GenerateFTRemoteImpl
@Inject constructor(
    retrofit: Retrofit
) : GenerateFTRemote{

    private val instapayQrApiClient: InstapayQrApiClient =
        retrofit.create(InstapayQrApiClient::class.java)

    override fun generateFT(
        accessToken: String,
        generateFTForm: GenerateFTForm
    ): Single<Response<GenerateFTResponse>> {
        return instapayQrApiClient.generateFT(accessToken,
            BuildConfig.MSME_CLIENT_ID,
            BuildConfig.MSME_CLIENT_SECRET,
            BuildConfig.MSME_CLIENT_API_VERSION,
            generateFTForm
        )
    }
}