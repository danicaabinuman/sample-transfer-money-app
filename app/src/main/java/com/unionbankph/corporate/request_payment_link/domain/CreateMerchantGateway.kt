package com.unionbankph.corporate.request_payment_link.domain

import com.unionbankph.corporate.link_details.data.LinkDetailsResponse
import com.unionbankph.corporate.link_details.data.form.LinkDetailsForm
import com.unionbankph.corporate.request_payment_link.data.form.CreateMerchantForm
import com.unionbankph.corporate.request_payment_link.data.form.RequestPaymentForm
import com.unionbankph.corporate.request_payment_link.data.model.CreateMerchantResponse
import com.unionbankph.corporate.request_payment_link.data.model.RequestPaymentLinkResponse
import io.reactivex.Single

interface CreateMerchantGateway {
    fun createMerchant(form: CreateMerchantForm) : Single<CreateMerchantResponse>
}