package com.unionbankph.corporate.payment_link.data.source.remote

import com.unionbankph.corporate.payment_link.domain.model.form.CreateMerchantForm
import com.unionbankph.corporate.payment_link.domain.model.form.GeneratePaymentLinkForm
import com.unionbankph.corporate.payment_link.domain.model.form.PutPaymentLinkStatusForm
import com.unionbankph.corporate.payment_link.domain.model.response.*
import io.reactivex.Single
import retrofit2.Response

interface PaymentLinkRemote {

    fun generatePaymentLink(accessToken: String, organizationId: String, generatePaymentLinkForm: GeneratePaymentLinkForm) : Single<Response<GeneratePaymentLinkResponse>>

    fun createMerchant(accessToken: String, createMerchantForm: CreateMerchantForm) : Single<Response<CreateMerchantResponse>>

    fun getPaymentLinkListPaginated(accessToken: String, organizationId: String, page: String, itemsPerPage: String) : Single<Response<GetPaymentLinkListPaginatedResponse>>

    fun getPaymentLinkListByReferenceNumber(accessToken: String, organizationId: String, page: String, itemsPerPage: String, referenceNumber: String) : Single<Response<GetPaymentLinkListPaginatedResponse>>

    fun getPaymentLinkByReferenceId(accessToken: String, referenceId: String) : Single<Response<GetPaymentLinkByReferenceIdResponse>>

    fun putPaymentLinkStatus(accessToken: String, transactionId: String, putPaymentLinkStatusForm: PutPaymentLinkStatusForm) : Single<Response<PutPaymentLinkStatusResponse>>
}