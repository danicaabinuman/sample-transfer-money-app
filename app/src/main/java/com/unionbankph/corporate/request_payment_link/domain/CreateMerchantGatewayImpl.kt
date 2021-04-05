package com.unionbankph.corporate.request_payment_link.domain

import com.unionbankph.corporate.auth.data.model.Role
import com.unionbankph.corporate.common.data.source.local.cache.CacheManager
import com.unionbankph.corporate.common.domain.provider.ResponseProvider
import com.unionbankph.corporate.request_payment_link.data.CreateMerchantRemote
import com.unionbankph.corporate.request_payment_link.data.form.CreateMerchantForm
import com.unionbankph.corporate.request_payment_link.data.model.CreateMerchantResponse
import com.unionbankph.corporate.settings.data.source.local.SettingsCache
import io.reactivex.Single
import javax.inject.Inject

class CreateMerchantGatewayImpl
@Inject constructor(
    private val responseProvider: ResponseProvider,
    private val createMerchantRemote: CreateMerchantRemote,
    private val settingsCache: SettingsCache,
    private val cacheManager: CacheManager
) : CreateMerchantGateway {

    override fun createMerchant(form: CreateMerchantForm): Single<CreateMerchantResponse> {

        val role = cacheManager.getObject(CacheManager.ROLE) as? Role
        var orgId = "6460b955-1a2e-4662-9bc3-b762"
        if(role?.organizationId != null){
            orgId = role.organizationId!!
        }
        form.organizationId = orgId
        return settingsCache.getAccessToken()
            .flatMap {
                createMerchantRemote.createMerchant(
                    it,
                    form
                )
            }
            .flatMap { responseProvider.executeResponseSingle(it) }


    }


}