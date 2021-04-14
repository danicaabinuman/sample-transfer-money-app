package com.unionbankph.corporate.payment_link.domain.model.gateway

import com.unionbankph.corporate.auth.data.model.Role
import com.unionbankph.corporate.common.data.source.local.cache.CacheManager
import com.unionbankph.corporate.common.domain.provider.ResponseProvider
import com.unionbankph.corporate.payment_link.data.source.remote.PaymentLinkRemote
import com.unionbankph.corporate.payment_link.domain.model.form.LinkDetailsForm
import com.unionbankph.corporate.payment_link.domain.model.response.LinkDetailsResponse
import com.unionbankph.corporate.settings.data.source.local.SettingsCache
import io.reactivex.Single
import javax.inject.Inject

class LinkDetailsGatewayImpl
@Inject constructor(
    private val responseProvider: ResponseProvider,
    private val paymentLinkRemote: PaymentLinkRemote,
    private val settingsCache: SettingsCache,
    private val cacheManager: CacheManager
) : LinkDetailsGateway {

    override fun getPaymentLink(linkDetailsRes: LinkDetailsForm): Single<LinkDetailsResponse> {

        val role = cacheManager.getObject(CacheManager.ROLE) as? Role
        var orgId = "6460b955-1a2e-4662-9bc3-b762"
        var orgName = "Test Org 6247 2"
        if(role?.organizationId != null){
            orgId = role.organizationId!!
        }
        if(role?.organizationName != null){
            orgName = role.organizationName!!
        }
        linkDetailsRes.organizationName = orgName
        return settingsCache.getAccessToken()
            .flatMap {
                paymentLinkRemote.generateLink(
                    it,
                    orgId,
                    linkDetailsRes
                )
            }
            .flatMap { responseProvider.executeResponseSingle(it) }


    }


}