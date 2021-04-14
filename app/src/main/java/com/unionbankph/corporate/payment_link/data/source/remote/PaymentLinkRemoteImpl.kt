package com.unionbankph.corporate.payment_link.data.source.remote

import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.payment_link.data.source.api.PaymentLinkApiClient
import com.unionbankph.corporate.payment_link.domain.model.form.CreateMerchantForm
import com.unionbankph.corporate.payment_link.domain.model.form.GeneratePaymentLinkForm
import com.unionbankph.corporate.payment_link.domain.model.response.CreateMerchantResponse
import com.unionbankph.corporate.payment_link.domain.model.response.GeneratePaymentLinkResponse
import io.reactivex.Single
import retrofit2.Response
import retrofit2.Retrofit
import javax.inject.Inject

class PaymentLinkRemoteImpl
@Inject constructor(
        retrofit: Retrofit
) : PaymentLinkRemote {
    private val paymentLinkApiClient: PaymentLinkApiClient =
            retrofit.create(PaymentLinkApiClient::class.java)

    override fun generatePaymentLink(
        accessToken: String,
        organizationId: String,
        generatePaymentLinkForm: GeneratePaymentLinkForm
    ): Single<Response<GeneratePaymentLinkResponse>> {
        return paymentLinkApiClient.generatePaymentLink(accessToken,
            BuildConfig.MSME_CLIENT_ID,
            BuildConfig.MSME_CLIENT_SECRET,
            BuildConfig.MSME_CLIENT_API_VERSION,
            organizationId,
            generatePaymentLinkForm
        )
    }

    override fun createMerchant(
        accessToken: String,
        createMerchantForm: CreateMerchantForm
    ): Single<Response<CreateMerchantResponse>> {
        return paymentLinkApiClient.createMerchant(
            accessToken,
            BuildConfig.MSME_CLIENT_ID,
            BuildConfig.MSME_CLIENT_SECRET,
            BuildConfig.MSME_CLIENT_API_VERSION,
            createMerchantForm
        )
    }

    override fun getPaymentLinkList(
        accessToken: String,
        organizationId: String,
        page: String,
        itemPerPage: String,
        referenceNumber: String
    ): Single<Response<Any>> {
        return paymentLinkApiClient.getPaymentLinkList(
            accessToken,
            BuildConfig.MSME_CLIENT_ID,
            BuildConfig.MSME_CLIENT_SECRET,
            BuildConfig.MSME_CLIENT_API_VERSION,
            organizationId,
            page,
            itemPerPage,
            referenceNumber
        )
    }

    override fun getPaymentLinkByReferenceId(
        accessToken: String,
        referenceId: String
    ): Single<Response<Any>> {
        return paymentLinkApiClient.getPaymentLinkByReferenceId(
            accessToken,
            BuildConfig.MSME_CLIENT_ID,
            BuildConfig.MSME_CLIENT_SECRET,
            BuildConfig.MSME_CLIENT_API_VERSION,
            referenceId
        )
    }
}