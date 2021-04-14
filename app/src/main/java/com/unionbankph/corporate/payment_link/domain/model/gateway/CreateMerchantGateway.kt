package com.unionbankph.corporate.payment_link.domain.model.gateway

import com.unionbankph.corporate.payment_link.domain.model.form.CreateMerchantForm
import com.unionbankph.corporate.payment_link.domain.model.response.CreateMerchantResponse
import io.reactivex.Single

interface CreateMerchantGateway {
    fun createMerchant(form: CreateMerchantForm) : Single<CreateMerchantResponse>
}