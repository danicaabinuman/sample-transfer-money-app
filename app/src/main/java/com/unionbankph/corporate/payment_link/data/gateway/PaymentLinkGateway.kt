package com.unionbankph.corporate.payment_link.data.gateway

import com.unionbankph.corporate.payment_link.domain.model.form.CreateMerchantForm
import com.unionbankph.corporate.payment_link.domain.model.form.GeneratePaymentLinkForm
import com.unionbankph.corporate.payment_link.domain.model.response.CreateMerchantResponse
import com.unionbankph.corporate.payment_link.domain.model.response.GeneratePaymentLinkResponse
import io.reactivex.Single

interface PaymentLinkGateway {
    fun generatePaymentLink(generatePaymentLinkForm: GeneratePaymentLinkForm) : Single<GeneratePaymentLinkResponse>

    fun createMerchant(createMerchantForm: CreateMerchantForm) : Single<CreateMerchantResponse>

    fun getPaymentLinkList(page: String, itemPerPage: String, referenceNumber: String) : Single<Any>

    fun getPaymentLinkByReferenceId(referenceId: String) : Single<Any>
}