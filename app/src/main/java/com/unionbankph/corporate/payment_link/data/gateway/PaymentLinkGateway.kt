package com.unionbankph.corporate.payment_link.data.gateway

import com.unionbankph.corporate.payment_link.domain.model.form.CreateMerchantForm
import com.unionbankph.corporate.payment_link.domain.model.form.GeneratePaymentLinkForm
import com.unionbankph.corporate.payment_link.domain.model.form.PutPaymentLinkStatusForm
import com.unionbankph.corporate.payment_link.domain.model.rmo.RMOBusinessInformationForm
import com.unionbankph.corporate.payment_link.domain.model.form.*
import com.unionbankph.corporate.payment_link.domain.model.response.*
import com.unionbankph.corporate.payment_link.domain.model.rmo.GetRMOBusinessInformationForm
import com.unionbankph.corporate.payment_link.domain.model.rmo.GetRMOBusinessInformationResponse
import com.unionbankph.corporate.payment_link.domain.model.rmo.RMOBusinessInformationResponse
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

    fun validateApprover() : Single<ValidateApproverResponse>

    fun submitBusinessInformation(rmoBusinessInformation : RMOBusinessInformationForm) : Single<RMOBusinessInformationResponse>

    fun getBusinessInformation(getRMOBusinessInformation : GetRMOBusinessInformationForm) : Single<GetRMOBusinessInformationResponse>

    fun updateSettlementOnRequestPayment(updateSettlementOnRequestPaymentForm: UpdateSettlementOnRequestPaymentForm) : Single<UpdateSettlementOnRequestPaymentResponse>

}