package com.unionbankph.corporate.payment_link.data.source.remote

import com.unionbankph.corporate.payment_link.domain.model.response.AllPaymentLinksResponse
import com.unionbankph.corporate.payment_link.domain.model.form.AllPaymentLinksForm
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