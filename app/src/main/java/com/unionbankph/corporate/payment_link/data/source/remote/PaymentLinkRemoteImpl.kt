package com.unionbankph.corporate.payment_link.data.source.remote

import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.payment_link.domain.model.response.LinkDetailsResponse
import com.unionbankph.corporate.payment_link.domain.model.form.LinkDetailsForm
import com.unionbankph.corporate.payment_link.data.source.api.PaymentLinkApiClient
import io.reactivex.Single
import retrofit2.Response
import retrofit2.Retrofit
import javax.inject.Inject

class PaymentLinkDetailsRemoteImpl
@Inject constructor(
        retrofit: Retrofit
) : PaymentLinkDetailsRemote {
    private val paymentLinkApiClient: PaymentLinkApiClient =
            retrofit.create(PaymentLinkApiClient::class.java)

    override fun generateLink(
        accessToken: String,
        organizationId: String,
        linkDetails: LinkDetailsForm
    ) : Single<Response<LinkDetailsResponse>>{
        return paymentLinkApiClient.generatePaymentLink(accessToken,
            BuildConfig.MSME_CLIENT_ID,
            BuildConfig.MSME_CLIENT_SECRET,
            BuildConfig.MSME_CLIENT_API_VERSION,
            organizationId,
            linkDetails
        )
    }
}