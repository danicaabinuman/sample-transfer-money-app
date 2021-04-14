package com.unionbankph.corporate.payment_link.data.source.remote

import com.unionbankph.corporate.payment_link.domain.model.form.CreateMerchantForm
import com.unionbankph.corporate.payment_link.domain.model.form.GeneratePaymentLinkForm
import com.unionbankph.corporate.payment_link.domain.model.response.CreateMerchantResponse
import com.unionbankph.corporate.payment_link.domain.model.response.GeneratePaymentLinkResponse
import io.reactivex.Single
import retrofit2.Response

interface PaymentLinkRemote {

    fun generatePaymentLink(accessToken: String, organizationId: String, generatePaymentLinkForm: GeneratePaymentLinkForm) : Single<Response<GeneratePaymentLinkResponse>>

    fun createMerchant(accessToken: String, createMerchantForm: CreateMerchantForm) : Single<Response<CreateMerchantResponse>>

    fun getPaymentLinkList(accessToken: String, organizationId: String, page: String, itemPerPage: String, referenceNumber: String) : Single<Response<Any>>

    fun getPaymentLinkByReferenceId(accessToken: String, referenceId: String) : Single<Response<Any>>
}