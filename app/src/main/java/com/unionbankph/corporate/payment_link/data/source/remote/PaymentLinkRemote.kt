package com.unionbankph.corporate.payment_link.data.source.remote

import com.unionbankph.corporate.payment_link.domain.model.response.LinkDetailsResponse
import com.unionbankph.corporate.payment_link.domain.model.form.LinkDetailsForm
import io.reactivex.Single
import retrofit2.Response

interface PaymentLinkDetailsRemote {

    fun generateLink(accessToken: String, organizationId: String, linkDetails: LinkDetailsForm) : Single<Response<LinkDetailsResponse>>
}