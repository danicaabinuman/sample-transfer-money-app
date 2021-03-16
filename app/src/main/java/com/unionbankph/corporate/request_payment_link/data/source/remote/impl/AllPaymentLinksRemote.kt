package com.unionbankph.corporate.request_payment_link.data.source.remote.impl

import com.unionbankph.corporate.request_payment_link.data.AllPaymentLinksResponse
import com.unionbankph.corporate.request_payment_link.data.form.AllPaymentLinksForm
import io.reactivex.Single
import retrofit2.Response

interface AllPaymentLinksRemote {
    fun allPaymentLinksGateway(allPaymentLinksForm: AllPaymentLinksForm) : Single<Response<AllPaymentLinksResponse>>

}