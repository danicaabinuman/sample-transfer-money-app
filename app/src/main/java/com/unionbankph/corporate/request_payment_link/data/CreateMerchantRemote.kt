package com.unionbankph.corporate.request_payment_link.data

import com.unionbankph.corporate.link_details.data.LinkDetailsResponse
import com.unionbankph.corporate.link_details.data.form.LinkDetailsForm
import com.unionbankph.corporate.request_payment_link.data.form.CreateMerchantForm
import com.unionbankph.corporate.request_payment_link.data.model.CreateMerchantResponse
import io.reactivex.Single
import retrofit2.Response

interface CreateMerchantRemote {

    fun createMerchant(accessToken: String, form: CreateMerchantForm) : Single<Response<CreateMerchantResponse>>
}