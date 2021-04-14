package com.unionbankph.corporate.payment_link.data.source.remote

import com.unionbankph.corporate.payment_link.domain.model.response.AllPaymentLinksResponse
import com.unionbankph.corporate.payment_link.domain.model.form.AllPaymentLinksForm
import io.reactivex.Single
import retrofit2.Response

interface AllPaymentLinksRemote {
    fun allPaymentLinksGateway(allPaymentLinksForm: AllPaymentLinksForm) : Single<Response<AllPaymentLinksResponse>>

}