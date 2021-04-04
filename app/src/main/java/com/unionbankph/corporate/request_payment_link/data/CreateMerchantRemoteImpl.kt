package com.unionbankph.corporate.request_payment_link.data

import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.request_payment_link.data.form.CreateMerchantForm
import com.unionbankph.corporate.request_payment_link.data.model.CreateMerchantResponse
import com.unionbankph.corporate.request_payment_link.data.source.remote.client.CreateMerchantApiClient
import io.reactivex.Single
import retrofit2.Response
import retrofit2.Retrofit
import javax.inject.Inject

class CreateMerchantRemoteImpl
@Inject constructor(
        retrofit: Retrofit
) : CreateMerchantRemote {
    private val createMerchantApiClient: CreateMerchantApiClient =
            retrofit.create(CreateMerchantApiClient::class.java)

    override fun createMerchant(
        accessToken: String,
        form: CreateMerchantForm
    ): Single<Response<CreateMerchantResponse>> {
        return createMerchantApiClient.generatePaymentLink(accessToken,
            BuildConfig.MSME_CLIENT_ID,
            BuildConfig.MSME_CLIENT_SECRET,
            BuildConfig.MSME_CLIENT_API_VERSION,
            form
        )
    }

}