package com.unionbankph.corporate.payment_link.domain.model.gateway

import com.unionbankph.corporate.payment_link.domain.model.form.LinkDetailsForm
import com.unionbankph.corporate.payment_link.domain.model.response.LinkDetailsResponse
import io.reactivex.Single

interface LinkDetailsGateway {
    fun getPaymentLink(linkDetailsRes: LinkDetailsForm) : Single<LinkDetailsResponse>
}