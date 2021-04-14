package com.unionbankph.corporate.payment_link.data.gateway

import com.unionbankph.corporate.auth.data.model.Role
import com.unionbankph.corporate.common.data.source.local.cache.CacheManager
import com.unionbankph.corporate.common.domain.provider.ResponseProvider
import com.unionbankph.corporate.payment_link.data.source.remote.PaymentLinkRemote
import com.unionbankph.corporate.payment_link.domain.model.form.CreateMerchantForm
import com.unionbankph.corporate.payment_link.domain.model.form.GeneratePaymentLinkForm
import com.unionbankph.corporate.payment_link.domain.model.response.CreateMerchantResponse
import com.unionbankph.corporate.payment_link.domain.model.response.GeneratePaymentLinkResponse
import com.unionbankph.corporate.settings.data.source.local.SettingsCache
import io.reactivex.Single
import javax.inject.Inject

class PaymentLinkGatewayImpl
@Inject constructor(
    private val responseProvider: ResponseProvider,
    private val paymentLinkRemote: PaymentLinkRemote,
    private val settingsCache: SettingsCache,
    private val cacheManager: CacheManager
) : PaymentLinkGateway  {

    override fun generatePaymentLink(generatePaymentLinkForm: GeneratePaymentLinkForm): Single<GeneratePaymentLinkResponse> {

        val role = cacheManager.getObject(CacheManager.ROLE) as? Role
        var orgId = "6460b955-1a2e-4662-9bc3-b762"
        var orgName = "Test Org 6247 2"
        if(role?.organizationId != null){
            orgId = role.organizationId!!
        }
        if(role?.organizationName != null){
            orgName = role.organizationName!!
        }
        generatePaymentLinkForm.organizationName = orgName
        return settingsCache.getAccessToken()
            .flatMap {
                paymentLinkRemote.generatePaymentLink(
                    it,
                    orgId,
                    generatePaymentLinkForm
                )
            }
            .flatMap { responseProvider.executeResponseSingle(it) }

    }

    override fun createMerchant(createMerchantForm: CreateMerchantForm): Single<CreateMerchantResponse> {

        val role = cacheManager.getObject(CacheManager.ROLE) as? Role
        var orgId = "6460b955-1a2e-4662-9bc3-b762"
        if(role?.organizationId != null){
            orgId = role.organizationId!!
        }
        createMerchantForm.organizationId = orgId
        return settingsCache.getAccessToken()
            .flatMap {
                paymentLinkRemote.createMerchant(
                    it,
                    createMerchantForm
                )
            }
            .flatMap { responseProvider.executeResponseSingle(it) }

    }

    override fun getPaymentLinkList(
        page: String,
        itemPerPage: String,
        referenceNumber: String
    ): Single<Any> {

        val role = cacheManager.getObject(CacheManager.ROLE) as? Role
        var orgId = "6460b955-1a2e-4662-9bc3-b762"
        if(role?.organizationId != null){
            orgId = role.organizationId!!
        }

        return settingsCache.getAccessToken()
            .flatMap {
                paymentLinkRemote.getPaymentLinkList(
                    it,
                    orgId,
                    page,
                    itemPerPage,
                    referenceNumber
                )
            }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getPaymentLinkByReferenceId(referenceId: String): Single<Any> {
        return settingsCache.getAccessToken()
            .flatMap {
                paymentLinkRemote.getPaymentLinkByReferenceId(
                    it,
                    referenceId
                )
            }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }
}