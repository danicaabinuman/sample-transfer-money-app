package com.unionbankph.corporate.payment_link.data.gateway

import com.unionbankph.corporate.auth.data.model.CorporateUser
import com.unionbankph.corporate.auth.data.model.Role
import com.unionbankph.corporate.common.data.source.local.cache.CacheManager
import com.unionbankph.corporate.common.domain.provider.SMEResponseProvider
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.payment_link.data.source.remote.PaymentLinkRemote
import com.unionbankph.corporate.payment_link.domain.model.form.CreateMerchantForm
import com.unionbankph.corporate.payment_link.domain.model.form.GeneratePaymentLinkForm
import com.unionbankph.corporate.payment_link.domain.model.form.PutPaymentLinkStatusForm
import com.unionbankph.corporate.payment_link.domain.model.rmo.RMOBusinessInformationForm
import com.unionbankph.corporate.payment_link.domain.model.form.*
import com.unionbankph.corporate.payment_link.domain.model.response.*
import com.unionbankph.corporate.payment_link.domain.model.rmo.GetRMOBusinessInformationForm
import com.unionbankph.corporate.payment_link.domain.model.rmo.GetRMOBusinessInformationResponse
import com.unionbankph.corporate.payment_link.domain.model.rmo.RMOBusinessInformationResponse
import com.unionbankph.corporate.settings.data.source.local.SettingsCache
import io.reactivex.Single
import retrofit2.Response
import javax.inject.Inject

class PaymentLinkGatewayImpl
@Inject constructor(
        private val smeResponseProvider: SMEResponseProvider,
        private val paymentLinkRemote: PaymentLinkRemote,
        private val settingsCache: SettingsCache,
        private val cacheManager: CacheManager
) : PaymentLinkGateway  {

    override fun generatePaymentLink(generatePaymentLinkForm: GeneratePaymentLinkForm): Single<GeneratePaymentLinkResponse> {

        val role = cacheManager.getObject(CacheManager.ROLE) as? Role
        generatePaymentLinkForm.organizationName = role?.organizationName

        val corporateUser = JsonHelper.fromJson<CorporateUser>(cacheManager.get(CacheManager.CORPORATE_USER))
        if(corporateUser.id != null){
            generatePaymentLinkForm.corporateId = corporateUser.id
        }

        return settingsCache.getAccessToken()
            .flatMap {
                paymentLinkRemote.generatePaymentLink(
                    it,
                    generatePaymentLinkForm
                )
            }
            .flatMap { smeResponseProvider.executeResponseSingle(it) }

    }

    override fun createMerchant(createMerchantForm: CreateMerchantForm): Single<CreateMerchantResponse> {
        
        return settingsCache.getAccessToken()
            .flatMap {
                paymentLinkRemote.createMerchant(
                    it,
                    createMerchantForm
                )
            }
            .flatMap { smeResponseProvider.executeResponseSingle(it) }

    }

    override fun updateSettlementOnRequestPayment(updateSettlementOnRequestPaymentForm: UpdateSettlementOnRequestPaymentForm): Single<UpdateSettlementOnRequestPaymentResponse> {
        return settingsCache.getAccessToken()
            .flatMap {
                paymentLinkRemote.updateSettlementOnRequestPayment(
                    it,
                    updateSettlementOnRequestPaymentForm
                )
            }
            .flatMap { smeResponseProvider.executeResponseSingle(it) }
    }



    override fun getPaymentLinkListPaginated(
            page: String,
            itemsPerPage: String
    ): Single<GetPaymentLinkListPaginatedResponse> {

        return settingsCache.getAccessToken()
                .flatMap {
                    paymentLinkRemote.getPaymentLinkListPaginated(
                            it,
                            page,
                            itemsPerPage
                    )
                }
                .flatMap { smeResponseProvider.executeResponseSingle(it) }
    }

    override fun getPaymentLinkListByReferenceNumber(
            page: String,
            itemsPerPage: String,
            referenceNumber: String
    ): Single<GetPaymentLinkListPaginatedResponse> {

        return settingsCache.getAccessToken()
                .flatMap {
                    paymentLinkRemote.getPaymentLinkListByReferenceNumber(
                            it,
                            page,
                            itemsPerPage,
                            referenceNumber
                    )
                }
                .flatMap { smeResponseProvider.executeResponseSingle(it) }
    }

    override fun getPaymentLinkByReferenceId(referenceId: String): Single<GetPaymentLinkByReferenceIdResponse> {

        return settingsCache.getAccessToken()
                .flatMap {
                    paymentLinkRemote.getPaymentLinkByReferenceId(
                            it,
                            referenceId
                    )
                }
                .flatMap { smeResponseProvider.executeResponseSingle(it) }
    }


    override fun putPaymentLinkStatus(
            transactionId: String,
            putPaymentLinkStatusForm: PutPaymentLinkStatusForm
    ): Single<PutPaymentLinkStatusResponse> {
        return settingsCache.getAccessToken()
                .flatMap {
                    paymentLinkRemote.putPaymentLinkStatus(
                            it,
                            transactionId,
                            putPaymentLinkStatusForm
                    )
                }
                .flatMap { smeResponseProvider.executeResponseSingle(it) }
    }

    override fun validateMerchantByOrganization(): Single<ValidateMerchantByOrganizationResponse> {
        return settingsCache.getAccessToken()
            .flatMap { paymentLinkRemote.validateMerchantByOrganization(it) }
            .flatMap { smeResponseProvider.executeResponseSingle(it) }
    }


    override fun validateApprover(): Single<ValidateApproverResponse> {
        val role = cacheManager.getObject(CacheManager.ROLE) as? Role
        var isApprover = false
        if(role!=null){
            isApprover = role?.isApprover
        }


        return Single.fromCallable { ValidateApproverResponse(isApprover) }

    }

    override fun submitBusinessInformation(rmoBusinessInformation: RMOBusinessInformationForm): Single<RMOBusinessInformationResponse> {

        return settingsCache.getAccessToken()
            .flatMap {
                paymentLinkRemote.putBusinessInformation(
                    it,
                    rmoBusinessInformation
                )
            }
            .flatMap { smeResponseProvider.executeResponseSingle(it) }
    }

    override fun getBusinessInformation(getRMOBusinessInformation: GetRMOBusinessInformationForm): Single<GetRMOBusinessInformationResponse> {

        return settingsCache.getAccessToken()
            .flatMap {
                paymentLinkRemote.getBusinessInformation(
                    it,
                    getRMOBusinessInformation
                )
            }
            .flatMap { smeResponseProvider.executeResponseSingle(it) }
    }

}