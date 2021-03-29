package com.unionbankph.corporate.request_payment_link.data.source.remote.impl

import com.unionbankph.corporate.request_payment_link.data.AllPaymentLinksResponse
import com.unionbankph.corporate.request_payment_link.data.form.AllPaymentLinksForm
import io.reactivex.Single
import retrofit2.Response
import retrofit2.Retrofit
import javax.inject.Inject

class AllPaymentLinksRemoteImpl
@Inject constructor(
    retrofit: Retrofit
) : AllPaymentLinksRemote {
    override fun allPaymentLinksGateway(
        allPaymentLinksForm: AllPaymentLinksForm
    ): Single<Response<AllPaymentLinksResponse>> {
        TODO("Not yet implemented")
    }

}