package com.unionbankph.corporate.payment_link.data.source.remote

import com.unionbankph.corporate.payment_link.domain.model.form.CreateMerchantForm
import com.unionbankph.corporate.payment_link.domain.model.response.CreateMerchantResponse
import io.reactivex.Single
import retrofit2.Response

interface CreateMerchantRemote {

    fun createMerchant(accessToken: String, form: CreateMerchantForm) : Single<Response<CreateMerchantResponse>>
}