package com.unionbankph.corporate.link_details.presentation

import com.unionbankph.corporate.link_details.data.LinkDetailsResponse
import com.unionbankph.corporate.link_details.data.form.LinkDetailsForm
import com.unionbankph.corporate.request_payment_link.data.form.RequestPaymentForm
import com.unionbankph.corporate.request_payment_link.data.model.RequestPaymentLinkResponse
import io.reactivex.Single

interface LinkDetailsGateway {
    fun linkGateway(linkDetailsRes: LinkDetailsForm) : Single<LinkDetailsResponse>
}