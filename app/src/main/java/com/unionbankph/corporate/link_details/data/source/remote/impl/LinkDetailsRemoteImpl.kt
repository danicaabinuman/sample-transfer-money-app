package com.unionbankph.corporate.link_details.data.source.remote.impl

import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.link_details.data.LinkDetailsResponse
import com.unionbankph.corporate.link_details.data.form.LinkDetailsForm
import com.unionbankph.corporate.link_details.data.source.remote.LinkDetailsRemote
import com.unionbankph.corporate.link_details.data.source.remote.client.LinkDetailsApiClient
import io.reactivex.Single
import retrofit2.Response
import retrofit2.Retrofit
import javax.inject.Inject

class LinkDetailsRemoteImpl
@Inject constructor(
        retrofit: Retrofit
) : LinkDetailsRemote {
    private val linkDetailsApiClient: LinkDetailsApiClient =
            retrofit.create(LinkDetailsApiClient::class.java)

    override fun generateLink(
        accessToken: String,
        linkDetails: LinkDetailsForm
    ) : Single<Response<LinkDetailsResponse>>{
        return linkDetailsApiClient.generatePaymentLink(accessToken,
            BuildConfig.MSME_CLIENT_ID,
            BuildConfig.MSME_CLIENT_SECRET,
            BuildConfig.MSME_CLIENT_API_VERSION,
            linkDetails
        )
    }
}