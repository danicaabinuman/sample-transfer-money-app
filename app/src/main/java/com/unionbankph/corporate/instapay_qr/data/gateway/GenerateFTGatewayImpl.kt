package com.unionbankph.corporate.instapay_qr.data.gateway

import com.unionbankph.corporate.common.data.source.local.cache.CacheManager
import com.unionbankph.corporate.common.domain.provider.SMEResponseProvider
import com.unionbankph.corporate.instapay_qr.data.source.remote.GenerateFTRemote
import com.unionbankph.corporate.instapay_qr.domain.model.form.GenerateFTForm
import com.unionbankph.corporate.instapay_qr.domain.model.response.GenerateFTResponse
import com.unionbankph.corporate.payment_link.data.source.remote.PaymentLinkRemote
import com.unionbankph.corporate.settings.data.source.local.SettingsCache
import io.reactivex.Single
import javax.inject.Inject

class GenerateFTGatewayImpl
@Inject constructor(
    private val smeResponseProvider: SMEResponseProvider,
    private val generateFTRemote: GenerateFTRemote,
    private val settingsCache: SettingsCache,
    private val cacheManager: CacheManager

    ) : GenerateFTGateway {
    override fun generateFT(generateFTForm: GenerateFTForm): Single<GenerateFTResponse> {

        return settingsCache.getAccessToken()
            .flatMap {
                generateFTRemote.generateFT(
                    it,
                    generateFTForm
                )
            }
            .flatMap { smeResponseProvider.executeResponseSingle(it) }
    }
}