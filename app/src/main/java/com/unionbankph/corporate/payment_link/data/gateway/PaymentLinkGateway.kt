package com.unionbankph.corporate.payment_link.data.gateway

import com.unionbankph.corporate.payment_link.domain.model.form.CreateMerchantForm
import com.unionbankph.corporate.payment_link.domain.model.form.GeneratePaymentLinkForm
import com.unionbankph.corporate.payment_link.domain.model.form.PutPaymentLinkStatusForm
import com.unionbankph.corporate.payment_link.domain.model.response.*
import io.reactivex.Single
import retrofit2.Response

interface PaymentLinkGateway {

    fun generatePaymentLink(generatePaymentLinkForm: GeneratePaymentLinkForm) : Single<GeneratePaymentLinkResponse>

    fun createMerchant(createMerchantForm: CreateMerchantForm) : Single<CreateMerchantResponse>

    fun getPaymentLinkListPaginated(page: String, itemsPerPage: String) : Single<GetPaymentLinkListPaginatedResponse>

    fun getPaymentLinkListByReferenceNumber(page: String, itemsPerPage: String, referenceNumber: String) : Single<GetPaymentLinkListPaginatedResponse>

    fun getPaymentLinkByReferenceId(referenceId: String) : Single<GetPaymentLinkByReferenceIdResponse>

    fun putPaymentLinkStatus(transactionId: String, putPaymentLinkStatusForm: PutPaymentLinkStatusForm) : Single<PutPaymentLinkStatusResponse>

    fun validateMerchantByOrganization() : Single<ValidateMerchantByOrganizationResponse>
}